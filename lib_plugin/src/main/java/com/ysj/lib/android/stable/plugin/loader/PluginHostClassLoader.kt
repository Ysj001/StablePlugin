package com.ysj.lib.android.stable.plugin.loader

import android.util.Log
import androidx.collection.ArraySet
import java.net.URL
import java.util.Enumeration

/**
 * 插件的宿主类加载器。
 *
 * @author Ysj
 * Create time: 2024/9/15
 */
internal class PluginHostClassLoader(parent: ClassLoader) : ClassLoader(parent) {

    companion object {
        private const val TAG = "PluginHostClassLoader"
    }

    private val pluginClassLoader = ArrayList<PluginClassLoader>()

    init {
        Log.i(TAG, "init.")
    }

    override fun findClass(name: String?): Class<*> {
        for (i in pluginClassLoader.indices) {
            try {
                return pluginClassLoader[i].loadClass(name)
            } catch (_: ClassNotFoundException) {
            }
        }
        return super.findClass(name)
    }

    override fun findResource(name: String?): URL? {
        for (index in pluginClassLoader.indices) {
            val result = pluginClassLoader[index].findResource(name)
            if (result != null) {
                return result
            }
        }
        return super.findResource(name)
    }

    override fun findResources(name: String?): Enumeration<URL> {
        for (index in pluginClassLoader.indices) {
            val result = pluginClassLoader[index].findResources(name)
            if (result.hasMoreElements()) {
                return result
            }
        }
        return super.findResources(name)
    }

    override fun findLibrary(libname: String?): String? {
        for (index in pluginClassLoader.indices) {
            val result = pluginClassLoader[index].findLibrary(libname)
            if (result != null) {
                return result
            }
        }
        return super.findLibrary(libname)
    }

    override fun getPackage(name: String?): Package? {
        for (index in pluginClassLoader.indices) {
            val result = pluginClassLoader[index].getPackage(name)
            if (result != null) {
                return result
            }
        }
        return super.getPackage(name)
    }

    fun addPluginClassLoader(classLoader: PluginClassLoader) {
        pluginClassLoader.add(classLoader)
    }

    fun removePluginClassLoader(classLoader: PluginClassLoader) {
        pluginClassLoader.remove(classLoader)
    }

}