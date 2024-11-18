package com.ysj.lib.android.stable.plugin

import android.app.Application
import android.content.ContentProvider
import android.content.pm.PackageInfo
import android.content.res.Configuration
import android.content.res.loader.ResourcesLoader
import androidx.collection.ArrayMap
import com.ysj.lib.android.stable.plugin.component.PluginApplication

/**
 * 封装 plugin 相关的数据。
 *
 * @author Ysj
 * Create time: 2024/9/15
 */
data class Plugin internal constructor(
    /**
     * 插件名。
     */
    val name: String,
    /**
     * 插件的类加载器。
     */
    val classLoader: ClassLoader,
    /**
     * 包含四大组件信息。
     */
    val packageInfo: PackageInfo,
) {

    internal var application: PluginApplication? = null
        private set

    internal var providerMap: ArrayMap<String, ContentProvider>? = null
        private set

    fun callApplicationCreate(hostApplication: Application) {
        var application = this.application
        if (application != null) {
            return
        }
        application = classLoader
            .loadClass(packageInfo.applicationInfo.name)
            .getDeclaredConstructor()
            .newInstance()
            as PluginApplication
        this.application = application
        application.plugin = this
        application.attachBaseContext(hostApplication.baseContext)
        application.onCreate()
    }

    fun callApplicationOnLowMemory() {
        this.application?.onLowMemory()
    }

    fun callApplicationOnTrimMemory(level: Int) {
        this.application?.onTrimMemory(level)
    }

    fun callApplicationOnConfigurationChanged(newConfig: Configuration) {
        this.application?.onConfigurationChanged(newConfig)
    }

    fun installProviders(hostApplication: Application) {
        if (packageInfo.providers.isNullOrEmpty()) {
            return
        }
        var providerMap = this.providerMap
        if (providerMap == null) {
            providerMap = ArrayMap()
            this.providerMap = providerMap
        }
        for (providerInfo in packageInfo.providers) {
            if (providerInfo.name.startsWith("com.ysj.lib.android.stable.plugin")) {
                continue
            }
            val provider = classLoader
                .loadClass(providerInfo.name)
                .getDeclaredConstructor()
                .newInstance()
                as ContentProvider
            provider.attachInfo(this.application ?: hostApplication, providerInfo)
            providerMap[providerInfo.authority] = provider
        }
    }

    fun callProviderOnLowMemory() {
        providerMap?.values?.forEach {
            it.onLowMemory()
        }
    }

    fun callProviderOnTrimMemory(level: Int) {
        providerMap?.values?.forEach {
            it.onTrimMemory(level)
        }
    }

    fun callProviderOnConfigurationChanged(newConfig: Configuration) {
        providerMap?.values?.forEach {
            it.onConfigurationChanged(newConfig)
        }
    }

}