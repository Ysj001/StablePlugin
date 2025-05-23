package com.ysj.lib.android.stable.plugin

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.Keep
import com.ysj.lib.android.stable.plugin.component.activity.PluginActivity
import com.ysj.lib.android.stable.plugin.component.activity.PluginExceptionHandlerActivity
import com.ysj.lib.android.stable.plugin.loader.PluginClassLoader
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * 仅用于兼容 api<28 的场景。
 *
 * @author Ysj
 * Create time: 2024/12/9
 */
@Keep
internal class InstrumentationCompat(
    private val hostClassLoader: ClassLoader,
) : Instrumentation() {

    companion object {
        private const val TAG = "InstrumentationCompat"
    }

    init {
        Log.i(TAG, "init.")
    }

    override fun callActivityOnCreate(activity: Activity, icicle: Bundle?) {
        if (activity.classLoader is PluginClassLoader) {
            icicle?.classLoader = activity.classLoader
            activity.intent.setExtrasClassLoader(activity.classLoader)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            super.callActivityOnCreate(activity, icicle)
            return
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return super.newActivity(cl, className, intent)
        }
        if (cl != hostClassLoader) {
            return newActivity(hostClassLoader, className, intent)
        }
        if (intent == null) {
            try {
                return super.newActivity(cl, className, null)
            } catch (e: ClassNotFoundException) {
                if (StablePlugin.recoverInstalledPlugins()) {
                    return newActivity(cl, className, null)
                }
                throw e
            }
        }
        val plugin = PluginActivity.findFromPlugin(cl, intent.extras)
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
            if (StablePlugin.recoverInstalledPlugins()) {
                newActivity(cl, className, intent)
            } else {
                tryNewActivity(e, intent)
            }
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
        require(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            "maximum sdk must be less than Q"
        }
        // 这里用元反射兼容 android P，否则在 P 上拿不到所有 field
        val getDeclaredFieldsMethod = Class::class.java.getDeclaredMethod("getDeclaredFields")
        val orgFields = getDeclaredFieldsMethod.invoke(org.javaClass) as Array<*>
        for (index in orgFields.indices) {
            val field = orgFields[index] as Field
            if (Modifier.isFinal(field.modifiers) && Modifier.isStatic(field.modifiers)) {
                continue
            }
            field.isAccessible = true
            field.set(this, field.get(org))
            Log.d(TAG, "replace field: ${field.name}")
        }
    }

}