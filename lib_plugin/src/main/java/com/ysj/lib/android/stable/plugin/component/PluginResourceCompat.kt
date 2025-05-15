package com.ysj.lib.android.stable.plugin.component

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.content.res.loader.ResourcesLoader
import android.content.res.loader.ResourcesProvider
import android.os.Build
import android.os.ParcelFileDescriptor
import com.ysj.lib.android.stable.plugin.Plugin
import com.ysj.lib.android.stable.plugin.StablePlugin.pluginInstalledFile

/**
 * 融合插件的 Resource 和主包的 Resource。
 *
 * @author Ysj
 * Create time: 2025/5/6
 */
internal object PluginResourceCompat {

    fun getResourceFromPlugin(plugin: Plugin, base: Context): Resources {
        val resources = base.createConfigurationContext(base.resources.configuration).resources
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            /*
                这里低版本得反射进行融合。
                - 不能用 ResourceWrapper 方案，ResourceWrapper 方案不支持同时获取主包和插件包的 assets 资源。
                - 不能用 applicationInfo 方案，通过设置额外添加主包的 sourceDir 加载资源在一些机型上和版本上还有兼容问题。
             */
            AssetManager::class.java
                .getMethod("addAssetPath", String::class.java)
                .apply { isAccessible = true }
                .invoke(resources.assets, plugin.name.pluginInstalledFile.absolutePath)
        } else {
            val resourcesProvider = ParcelFileDescriptor
                .open(plugin.name.pluginInstalledFile, ParcelFileDescriptor.MODE_READ_ONLY)
                .use { ResourcesProvider.loadFromApk(it) }
            val resourcesLoader = ResourcesLoader()
            resourcesLoader.addProvider(resourcesProvider)
            resources.addLoaders(resourcesLoader)
        }
        return resources
    }
}