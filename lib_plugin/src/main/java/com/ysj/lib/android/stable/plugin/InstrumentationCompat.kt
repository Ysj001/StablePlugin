package com.ysj.lib.android.stable.plugin

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.ysj.lib.android.stable.plugin.component.activity.PluginActivity
import com.ysj.lib.android.stable.plugin.component.activity.PluginExceptionHandlerActivity
import java.lang.reflect.Modifier

/**
 * 仅用于兼容 api<28 的场景。
 *
 * @author Ysj
 * Create time: 2024/12/9
 */
class InstrumentationCompat(
    private val hostClassLoader: ClassLoader,
) : Instrumentation() {

    companion object {
        private const val TAG = "InstrumentationCompat"
    }

    override fun callActivityOnCreate(activity: Activity, icicle: Bundle?) {
        try {
            super.callActivityOnCreate(activity, icicle)
        } catch (e: Exception) {
            val activityClazz = StablePlugin.config.exceptionHandlerActivity
            if (activityClazz == null) {
                throw e
            } else {
                try {
                    val intent = Intent(activity, activityClazz)
                    PluginExceptionHandlerActivity.applyReason(
                        intent,
                        PluginExceptionHandlerActivity.FROM_ACTIVITY_ON_CREATE,
                        e,
                    )
                    activity.startActivity(intent)
                    activity.finish()
                } catch (_: Exception) {
                    throw e
                }
            }
        }
    }

    override fun newActivity(cl: ClassLoader?, className: String?, intent: Intent?): Activity {
        if (cl != hostClassLoader) {
            return newActivity(hostClassLoader, className, intent)
        }
        if (intent == null) {
            return super.newActivity(hostClassLoader, className, null)
        }
        val pluginName = intent.extras
            ?.keySet()
            ?.find { it.startsWith(PluginActivity.KEY_FROM_PLUGIN_PREFIX) }
            ?.substring(PluginActivity.KEY_FROM_PLUGIN_PREFIX.length)
        val plugin = if (pluginName == null) null else StablePlugin.findPluginByName(pluginName)
        if (plugin != null) {
            try {
                // 如果该 Activity 在宿主和插件中都有，则优先从插件 classloader 加载
                return super.newActivity(plugin.classLoader, className, intent)
            } catch (_: ClassNotFoundException) {
                // 说明该 Activity 在启动方插件中不存在
            } catch (e: Exception) {
                return tryNewActivity(e, intent)
            }
        }
        return try {
            super.newActivity(cl, className, intent)
        } catch (e: Exception) {
            return tryNewActivity(e, intent)
        }
    }

    private fun tryNewActivity(e: Exception, intent: Intent?): Activity {
        val activityClazz = StablePlugin.config.exceptionHandlerActivity
        if (activityClazz == null || intent == null) {
            throw e
        } else {
            try {
                PluginExceptionHandlerActivity.applyReason(
                    intent,
                    PluginExceptionHandlerActivity.FROM_ACTIVITY_NEW,
                    e,
                )
                return activityClazz.getDeclaredConstructor().newInstance()
            } catch (_: Exception) {
                throw e
            }
        }
    }

    fun init(org: Instrumentation) {
        val orgFields = org.javaClass.declaredFields
        for (index in orgFields.indices) {
            val field = orgFields[index]
            if (Modifier.isFinal(field.modifiers) && Modifier.isStatic(field.modifiers)) {
                continue
            }
            field.isAccessible = true
            field.set(this, field.get(org))
            Log.d(TAG, "replace field: ${field.name}")
        }
    }

}