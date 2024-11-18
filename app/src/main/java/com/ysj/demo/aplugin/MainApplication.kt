package com.ysj.demo.aplugin

import android.app.Application
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.collection.ArraySet
import com.ysj.lib.android.stable.plugin.Plugin
import com.ysj.lib.android.stable.plugin.StablePlugin
import com.ysj.lib.android.stable.plugin.config.EventCallback
import com.ysj.lib.android.stable.plugin.config.StablePluginConfig

/**
 * APP 入口。
 *
 * @author Ysj
 * Create time: 2024/9/14
 */
class MainApplication : Application() {

    companion object {
        private const val TAG = "MainApplication"
    }

    private val pluginEventCallback = EventCallbackImpl()

    override fun onCreate() {
        super.onCreate()
        val config = StablePluginConfig
            .Builder()
            .eventCallback(pluginEventCallback)
            .build()
        StablePlugin.init(this, config)
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

    private inner class EventCallbackImpl : EventCallback {

        private val pluginSet = ArraySet<Plugin>()

        override fun onInitialized() {
            Log.d(TAG, "onInitialized.")
        }

        override fun onPluginInstalled(plugin: Plugin) {
            Log.d(TAG, "onPluginInstalled: $plugin")
            plugin.callApplicationCreate(this@MainApplication)
            plugin.installProviders(this@MainApplication)
            pluginSet.add(plugin)
        }

        override fun onPluginUninstalled(plugin: Plugin) {
            Log.d(TAG, "onPluginUninstalled: $plugin")
            pluginSet.remove(plugin)
        }

        fun callOnLowMemory() {
            pluginSet.forEach {
                it.callApplicationOnLowMemory()
                it.callProviderOnLowMemory()
            }
        }

        fun callOnTrimMemory(level: Int) {
            pluginSet.forEach {
                it.callApplicationOnTrimMemory(level)
                it.callProviderOnTrimMemory(level)
            }
        }

        fun callOnConfigurationChanged(newConfig: Configuration) {
            pluginSet.forEach {
                it.callApplicationOnConfigurationChanged(newConfig)
                it.callProviderOnConfigurationChanged(newConfig)
            }
        }

    }

}