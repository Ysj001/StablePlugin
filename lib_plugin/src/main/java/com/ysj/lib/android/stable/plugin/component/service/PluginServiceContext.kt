package com.ysj.lib.android.stable.plugin.component.service

import android.app.Service
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import android.content.res.AssetManager
import android.content.res.Resources
import android.content.res.loader.ResourcesLoader
import android.content.res.loader.ResourcesProvider
import android.os.Build
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import com.ysj.lib.android.stable.plugin.StablePlugin
import com.ysj.lib.android.stable.plugin.StablePlugin.pluginInstalledFile
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

    private val resources = base.packageManager.getResourcesForApplication(plugin.packageInfo.applicationInfo)

    private var layoutInflater: LayoutInflater? = null

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