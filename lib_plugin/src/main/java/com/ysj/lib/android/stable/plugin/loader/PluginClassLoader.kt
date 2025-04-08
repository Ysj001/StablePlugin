package com.ysj.lib.android.stable.plugin.loader

import android.util.Log
import com.ysj.lib.android.stable.plugin.Plugin
import com.ysj.lib.android.stable.plugin.StablePlugin
import com.ysj.lib.android.stable.plugin.component.PluginApplication
import com.ysj.lib.android.stable.plugin.component.activity.PluginActivityContext
import com.ysj.lib.android.stable.plugin.component.service.PluginServiceContext
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

    fun loadClassFromHost(name: String?): Class<*> {
        log("loadClassFromHost: $name")
        return super.loadClass(name)
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        if (name.startsWith("kotlin") || StablePlugin.config.pluginClassLoadHook?.loadFromHost(name) == true) {
            try {
                return requireNotNull(javaClass.classLoader).loadClass(name)
            } catch (_: ClassNotFoundException) {
            }
        }
        log("loadClass: $name")
        return super.loadClass(name, resolve)
    }

    override fun findClass(name: String?): Class<*> {
        return when (name) {
            // ==== 确保一些类从同一个类加载器加载。 ====
            StablePlugin::class.java.name -> StablePlugin::class.java
            Plugin::class.java.name -> Plugin::class.java
            PluginApplication::class.java.name -> PluginApplication::class.java
            PluginActivityContext::class.java.name -> PluginActivityContext::class.java
            PluginServiceContext::class.java.name -> PluginServiceContext::class.java
            // ====================================
            else -> try {
                log("findClass: $name")
                super.findClass(name)
            } catch (e: ClassNotFoundException) {
                try {
                    // 该 class 可能在其它 classLoader 中，尝试加载
                    return requireNotNull(javaClass.classLoader).loadClass(name)
                } catch (_: ClassNotFoundException) {
                }
                Log.w(TAG, "findClass failure. $name")
                throw e
            }
        }
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