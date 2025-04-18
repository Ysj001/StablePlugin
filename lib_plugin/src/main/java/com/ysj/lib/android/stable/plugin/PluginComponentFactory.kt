package com.ysj.lib.android.stable.plugin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AppComponentFactory
import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.core.app.CoreComponentFactory
import androidx.core.content.IntentCompat
import com.ysj.lib.android.stable.plugin.component.activity.PluginActivity
import com.ysj.lib.android.stable.plugin.component.activity.PluginExceptionHandlerActivity
import com.ysj.lib.android.stable.plugin.loader.PluginHostClassLoader

/**
 * 插件用的 [AppComponentFactory]。
 *
 * - 注册在：
 * ```
 * <manifest>
 *     <application
 *         android:appComponentFactory="com.ysj.lib.android.stable.plugin.PluginComponentFactory"
 *     />
 * </manifest>
 * ```
 *
 * @author Ysj
 * Create time: 2024/9/16
 */
@Keep
@RequiresApi(Build.VERSION_CODES.P)
@SuppressLint("RestrictedApi")
internal class PluginComponentFactory : CoreComponentFactory() {

    companion object {
        private const val TAG = "PluginComponentFactory"
    }

    private lateinit var application: Application

    init {
        Log.i(TAG, "init.")
    }

    override fun instantiateClassLoader(cl: ClassLoader, aInfo: ApplicationInfo): ClassLoader {
        return PluginHostClassLoader(cl)
    }

    override fun instantiateApplication(cl: ClassLoader, className: String): Application {
        val instantiateApplication = super.instantiateApplication(cl, className)
        this.application = instantiateApplication
        return instantiateApplication
    }

    override fun instantiateActivity(cl: ClassLoader, className: String, intent: Intent?): Activity {
        if (className == StablePlugin.STANDARD_ACTIVITY && intent != null) {
            val wrapped = IntentCompat.getParcelableExtra(
                intent,
                StablePlugin.INTENT_KEY_PLUGIN_WRAPPED,
                Intent::class.java,
            )
            if (wrapped != null) {
                val component = requireNotNull(wrapped.component) {
                    "not found target activity."
                }
                return super.instantiateActivity(cl, component.className, wrapped)
            }
        }
        if (intent == null) {
            return super.instantiateActivity(cl, className, null)
        }
        intent.setExtrasClassLoader(cl)
        val pluginName = intent.extras
            ?.keySet()
            ?.find { it.startsWith(PluginActivity.KEY_FROM_PLUGIN_PREFIX) }
            ?.substring(PluginActivity.KEY_FROM_PLUGIN_PREFIX.length)
        intent.setExtrasClassLoader(null)
        val plugin = if (pluginName == null) null else StablePlugin.findPluginByName(pluginName)
        if (plugin != null) {
            try {
                // 如果该 Activity 在宿主和插件中都有，则优先从插件 classloader 加载
                return super.instantiateActivity(plugin.classLoader, className, intent)
            } catch (_: ClassNotFoundException) {
                // 说明该 Activity 在启动方插件中不存在
            } catch (e: Exception) {
                return tryNewActivity(e, intent)
            }
        }
        return try {
            super.instantiateActivity(cl, className, intent)
        } catch (e: Exception) {
            tryNewActivity(e, intent)
        }
    }

    private fun tryNewActivity(e: Exception, intent: Intent): Activity {
        val activityClazz = StablePlugin.config.exceptionHandlerActivity
        if (activityClazz == null) {
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
}