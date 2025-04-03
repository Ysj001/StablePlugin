package com.ysj.lib.android.stable.plugin.component.provider

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import android.content.res.AssetManager
import android.content.res.Resources
import android.content.res.loader.ResourcesLoader
import android.content.res.loader.ResourcesProvider
import android.os.Build
import android.os.ParcelFileDescriptor
import com.ysj.lib.android.stable.plugin.Plugin
import com.ysj.lib.android.stable.plugin.StablePlugin.pluginInstalledFile

/**
 * 插件用上下文。
 *
 * @author Ysj
 * Create time: 2025/4/3
 */
class PluginProviderContext(
    base: Context,
    private val plugin: Plugin,
) : ContextWrapper(base) {

    private val resources = base.packageManager.getResourcesForApplication(plugin.packageInfo.applicationInfo)

    init {
        val pluginFile = plugin.name.pluginInstalledFile
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            AssetManager::class.java
                .getMethod("addAssetPath", String::class.java)
                .apply { isAccessible = true }
                .invoke(resources.assets, pluginFile.absolutePath)
        } else {
            val resourcesProvider = ParcelFileDescriptor
                .open(pluginFile, ParcelFileDescriptor.MODE_READ_ONLY)
                .use { ResourcesProvider.loadFromApk(it) }
            val resourcesLoader = ResourcesLoader()
            resourcesLoader.addProvider(resourcesProvider)
            resources.addLoaders(resourcesLoader)
        }
    }

    override fun getClassLoader(): ClassLoader {
        return plugin.classLoader
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

}