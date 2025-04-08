package com.ysj.lib.android.stable.plugin.component

import android.app.Application
import android.content.ComponentCallbacks
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.AssetManager
import android.content.res.Resources
import android.content.res.loader.ResourcesLoader
import android.content.res.loader.ResourcesProvider
import android.os.Build
import android.os.ParcelFileDescriptor
import com.ysj.lib.android.stable.plugin.Plugin
import com.ysj.lib.android.stable.plugin.StablePlugin
import com.ysj.lib.android.stable.plugin.StablePlugin.pluginInstalledFile

/**
 * 插件的 [Application]。
 *
 * @author Ysj
 * Create time: 2024/9/23
 */
internal open class PluginApplication : Application() {

    lateinit var plugin: Plugin

    private lateinit var resources: Resources

    public override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        resources = base.packageManager.getResourcesForApplication(plugin.packageInfo.applicationInfo)
        val pluginInstalledFile = plugin.name.pluginInstalledFile
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            AssetManager::class.java
                .getMethod("addAssetPath", String::class.java)
                .apply { isAccessible = true }
                .invoke(resources.assets, pluginInstalledFile.absolutePath)
        } else {
            val resourcesProvider = ParcelFileDescriptor
                .open(pluginInstalledFile, ParcelFileDescriptor.MODE_READ_ONLY)
                .use { ResourcesProvider.loadFromApk(it) }
            val resourcesLoader = ResourcesLoader()
            resourcesLoader.addProvider(resourcesProvider)
            resources.addLoaders(resourcesLoader)
        }
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

    override fun getResources(): Resources {
        return resources
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