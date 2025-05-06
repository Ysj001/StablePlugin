package com.ysj.lib.android.stable.plugin.component.service

import android.app.Service
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import android.view.LayoutInflater
import com.ysj.lib.android.stable.plugin.StablePlugin
import com.ysj.lib.android.stable.plugin.component.PluginResourceCompat
import com.ysj.lib.android.stable.plugin.component.PluginViewFactoryCompat


/**
 * 插件用上下文。
 *
 * @author Ysj
 * Create time: 2024/9/19
 */
internal class PluginServiceContext(
    base: Context,
    clazz: Class<out Service>,
) : ContextWrapper(base) {

    private val plugin = checkNotNull(StablePlugin.findPluginByClassLoader(clazz.classLoader!!)) {
        "plugin not install."
    }

    private val resources = PluginResourceCompat.getResourceFromPlugin(plugin, base)

    private var layoutInflater: LayoutInflater? = null

    override fun getResources(): Resources {
        return resources
    }

    override fun getApplicationContext(): Context {
        return plugin.application
    }

    override fun getApplicationInfo(): ApplicationInfo {
        return plugin.packageInfo.applicationInfo
    }

    override fun getSystemService(name: String): Any? {
        if (LAYOUT_INFLATER_SERVICE == name) {
            var layoutInflater = this.layoutInflater
            if (layoutInflater == null) {
                layoutInflater = super.getSystemService(name) as LayoutInflater
                layoutInflater = layoutInflater.cloneInContext(this)
                layoutInflater.setFactory(PluginViewFactoryCompat(classLoader))
                layoutInflater = layoutInflater.cloneInContext(this)
                this.layoutInflater = layoutInflater
            }
            return layoutInflater
        }
        return super.getSystemService(name)
    }

    override fun getClassLoader(): ClassLoader {
        return plugin.classLoader
    }

}