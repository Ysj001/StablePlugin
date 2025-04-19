package com.ysj.lib.android.stable.plugin.component.activity

import android.app.Activity
import android.app.Application
import com.ysj.lib.android.stable.plugin.StablePlugin
import com.ysj.lib.android.stable.plugin.component.PluginApplication

/**
 * 对插件 [Activity] 中的一些行为做兼容。
 *
 * @author Ysj
 * Create time: 2025/4/20
 */
internal object PluginActivityCompat {

    /**
     * 代理 [Activity.getApplication] 使其优先返回插件的 [Application]。
     */
    @JvmStatic
    fun getApplication(activity: Activity): Application {
        if (activity.application is PluginApplication) {
            return activity.application
        }
        return StablePlugin.findPluginByClassLoader(activity.classLoader)
            ?.application
            ?: activity.application
    }

}