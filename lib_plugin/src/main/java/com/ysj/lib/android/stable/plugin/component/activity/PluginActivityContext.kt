package com.ysj.lib.android.stable.plugin.component.activity

import android.app.Activity
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
internal class PluginActivityContext(
    base: Context,
    clazz: Class<out Activity>,
) : ContextWrapper(base) {

    private val plugin = checkNotNull(StablePlugin.findPluginByClassLoader(clazz.classLoader!!)) {
        "plugin not install."
    }
    private val activityInfo = plugin.packageInfo.activities.find { it.name == clazz.name }!!

    private val resources = PluginResourceCompat.getResourceFromPlugin(plugin, base)

    private var theme: Resources.Theme? = null
    private var layoutInflater: LayoutInflater? = null

    override fun getTheme(): Resources.Theme {
        var theme = this.theme
        if (theme == null) {
            theme = requireNotNull(resources.newTheme())
            theme.applyStyle(activityInfo.themeResource, false)
            this.theme = theme
        }
        return theme
    }

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