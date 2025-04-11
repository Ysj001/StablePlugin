package com.ysj.lib.android.stable.plugin

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ysj.lib.android.stable.plugin.component.activity.PluginActivity
import com.ysj.lib.android.stable.plugin.loader.PluginClassLoader
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

    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
        return StablePlugin.application
    }

    override fun callApplicationOnCreate(app: Application) {
        if (app != StablePlugin.application) {
            super.callApplicationOnCreate(app)
        }
    }

    override fun newActivity(cl: ClassLoader?, className: String?, intent: Intent?): Activity {
        if (cl != hostClassLoader && cl !is PluginClassLoader) {
            if (intent != null) {
                intent.setExtrasClassLoader(cl)
                val pluginName = intent.getStringExtra(PluginActivity.KEY_FROM_PLUGIN)
                if (pluginName != null) {
                    val plugin = StablePlugin.findPluginByName(pluginName)
                    if (plugin != null) {
                        try {
                            // 如果该 Activity 在宿主和插件中都有，则优先从插件 classloader 加载
                            return super.newActivity(plugin.classLoader, className, intent)
                        } catch (_: ClassNotFoundException) {
                            // 说明该 Activity 在启动方插件中不存在
                        }
                    }
                }
            }
            return super.newActivity(hostClassLoader, className, intent)
        }
        return super.newActivity(cl, className, intent)
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