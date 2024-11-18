package com.ysj.lib.android.stable.plugin.loader

import android.util.Log
import com.ysj.lib.android.stable.plugin.Plugin
import com.ysj.lib.android.stable.plugin.StablePlugin
import com.ysj.lib.android.stable.plugin.component.PluginApplication
import com.ysj.lib.android.stable.plugin.component.activity.PluginActivityContext
import dalvik.system.DexClassLoader
import java.net.URL
import java.util.Enumeration

/**
 * 插件的类加载器。
 *
 * @author Ysj
 * Create time: 2024/9/19
 */
internal class PluginClassLoader(
    pluginPath: String,
    optimizedDirectory: String?,
    librarySearchPath: String?,
) : DexClassLoader(
    pluginPath,
    optimizedDirectory,
    librarySearchPath,
    // BootClassLoader
    getSystemClassLoader().parent,
) {

    companion object {
        private const val TAG = "PluginClassLoader"
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        return when (name) {
            // ==== 确保一些类从同一个类加载器加载。 ====
            StablePlugin::class.java.name -> StablePlugin::class.java
            Plugin::class.java.name -> Plugin::class.java
            PluginApplication::class.java.name -> PluginApplication::class.java
            PluginActivityContext::class.java.name -> PluginActivityContext::class.java
            // ====================================
            else -> super.loadClass(name, resolve)
        }
    }

    override fun findClass(name: String?): Class<*> {
        log("findClass: $name")
        return super.findClass(name)
    }

    public override fun findResource(name: String?): URL? {
        log("findResource: $name")
        return super.findResource(name)
    }

    public override fun findResources(name: String?): Enumeration<URL> {
        log("findResources: $name")
        return super.findResources(name)
    }

    override fun findLibrary(name: String?): String? {
        log("findLibrary: $name")
        return super.findLibrary(name)
    }

    public override fun getPackage(name: String?): Package? {
        log("getPackage: $name")
        return super.getPackage(name)
    }

    private fun log(msg: String) {
        if (StablePlugin.config.isDebugEnable) {
            Log.d(TAG, msg)
        }
    }

}