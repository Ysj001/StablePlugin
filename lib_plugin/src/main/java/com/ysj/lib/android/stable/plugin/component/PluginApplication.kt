package com.ysj.lib.android.stable.plugin.component

import android.app.Application
import android.content.ComponentCallbacks
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.AssetManager
import android.content.res.Resources
import android.view.LayoutInflater
import com.ysj.lib.android.stable.plugin.Plugin
import com.ysj.lib.android.stable.plugin.StablePlugin

/**
 * 插件的 [Application]。
 *
 * @author Ysj
 * Create time: 2024/9/23
 */
internal open class PluginApplication : Application() {

    lateinit var plugin: Plugin

    private lateinit var resources: Resources

    private var theme: Resources.Theme? = null
    private var layoutInflater: LayoutInflater? = null

    public override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        resources = PluginResourceCompat.getResourceFromPlugin(plugin, base)
    }

    override fun registerComponentCallbacks(callback: ComponentCallbacks?) {
        StablePlugin.application.registerComponentCallbacks(callback)
    }

    override fun unregisterComponentCallbacks(callback: ComponentCallbacks?) {
        StablePlugin.application.unregisterComponentCallbacks(callback)
    }

    override fun registerActivityLifecycleCallbacks(callback: ActivityLifecycleCallbacks?) {
        StablePlugin.application.registerActivityLifecycleCallbacks(callback)
    }

    override fun unregisterActivityLifecycleCallbacks(callback: ActivityLifecycleCallbacks?) {
        StablePlugin.application.unregisterActivityLifecycleCallbacks(callback)
    }

    override fun registerOnProvideAssistDataListener(callback: OnProvideAssistDataListener?) {
        StablePlugin.application.registerOnProvideAssistDataListener(callback)
    }

    override fun unregisterOnProvideAssistDataListener(callback: OnProvideAssistDataListener?) {
        StablePlugin.application.unregisterOnProvideAssistDataListener(callback)
    }

    override fun getTheme(): Resources.Theme {
        var theme = this.theme
        if (theme == null) {
            theme = requireNotNull(resources.newTheme())
            theme.applyStyle(plugin.packageInfo.applicationInfo.theme, false)
            this.theme = theme
        }
        return theme
    }

    override fun getAssets(): AssetManager {
        return resources.assets
    }

    override fun getResources(): Resources {
        return resources
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

    override fun getApplicationContext(): Context {
        return this
    }

    override fun getApplicationInfo(): ApplicationInfo {
        return plugin.packageInfo.applicationInfo
    }

    override fun getClassLoader(): ClassLoader {
        return plugin.classLoader
    }

}