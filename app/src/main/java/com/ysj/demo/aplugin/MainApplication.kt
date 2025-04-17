package com.ysj.demo.aplugin

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.collection.ArraySet
import com.ysj.lib.android.stable.plugin.Plugin
import com.ysj.lib.android.stable.plugin.StablePlugin
import com.ysj.lib.android.stable.plugin.config.EventCallback
import com.ysj.lib.android.stable.plugin.config.PluginClassLoadHook
import com.ysj.lib.android.stable.plugin.config.StablePluginConfig
import java.io.File

/**
 * APP 入口。
 *
 * @author Ysj
 * Create time: 2024/9/14
 */
class MainApplication : Application() {

    companion object {
        private const val TAG = "MainApplication"

        lateinit var pluginFileStorageDir: File
    }

    private val pluginEventCallback = StablePluginEventCallbackImpl()

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        pluginFileStorageDir = getExternalFilesDir(null) ?: filesDir
        if (!pluginFileStorageDir.isDirectory) {
            pluginFileStorageDir.mkdirs()
        }
        val config = StablePluginConfig
            .Builder()
            .debugEnable(true)
            .eventCallback(pluginEventCallback)
            .pluginClassLoadHook(PluginClassLoadHookImpl())
            .exceptionHandlerActivity(PluginExceptionHandlerImplActivity::class.java)
            .build()
        StablePlugin.init(this, config)
        PluginApiProxy.init(this)
    }

    override fun onCreate() {
        super.onCreate()
        pluginEventCallback.callOnCreate()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        pluginEventCallback.callOnTrimMemory(level)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        pluginEventCallback.callOnLowMemory()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        pluginEventCallback.callOnConfigurationChanged(newConfig)
    }

    private inner class PluginClassLoadHookImpl : PluginClassLoadHook {
        override fun loadFromHost(name: String): Boolean {
            return PluginApiProxy.PluginApiClassLoad.loadFromHost(name)
                // 兼容 api<29 时插件中的 ReportFragment 类加载问题
                || name.startsWith("androidx.lifecycle.ReportFragment")
        }
    }

    private inner class StablePluginEventCallbackImpl : EventCallback {

        private val pluginSet = ArraySet<Plugin>()

        private var isApplicationCreated = false

        override fun onInitialized() {
            Log.d(TAG, "onInitialized.")
        }

        override fun onPluginInstalled(plugin: Plugin) {
            Log.d(TAG, "onPluginInstalled: $plugin")
            pluginSet.add(plugin)
            plugin.installProviders()
            if (isApplicationCreated) {
                plugin.pluginApplication.onCreate()
            }
        }

        override fun onPluginUninstalled(plugin: Plugin) {
            Log.d(TAG, "onPluginUninstalled: $plugin")
            pluginSet.remove(plugin)
            PluginApiProxy.onPluginUninstalled(plugin)
        }

        fun callOnCreate() {
            isApplicationCreated = true
            pluginSet.forEach {
                it.pluginApplication.onCreate()
            }
        }

        fun callOnLowMemory() {
            pluginSet.forEach {
                it.pluginApplication.onLowMemory()
                it.callProviderOnLowMemory()
            }
        }

        fun callOnTrimMemory(level: Int) {
            pluginSet.forEach {
                it.pluginApplication.onTrimMemory(level)
                it.callProviderOnTrimMemory(level)
            }
        }

        fun callOnConfigurationChanged(newConfig: Configuration) {
            pluginSet.forEach {
                it.pluginApplication.onConfigurationChanged(newConfig)
                it.callProviderOnConfigurationChanged(newConfig)
            }
        }

    }

}