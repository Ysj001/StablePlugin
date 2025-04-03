package com.ysj.demo.aplugin

import android.app.Application
import android.util.Log
import androidx.collection.ArrayMap
import com.ysj.demo.aplugin.demo1.api.Demo1Component
import com.ysj.lib.android.stable.plugin.Plugin
import com.ysj.lib.android.stable.plugin.config.PluginClassLoadHook
import com.ysj.lib.bcu.modifier.component.di.api.Component
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * - 演示对 [component-di](https://github.com/Ysj001/bcu-modifier-component-di) 的支持。
 * - 兼容并代理 [Component] 注解的接口。
 *
 * @author Ysj
 * Create time: 2024/12/4
 */
object PluginApiProxy {

    private const val TAG = "PluginApiProxy"

    private lateinit var application: Application

    private val apiImplMap = ArrayMap<Class<*>, Any>()

    private object InvocationHandlerImpl : InvocationHandler {

        override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
            return when (val apiClazz = proxy.javaClass.interfaces.first()) {
                Demo1Component::class.java -> {
                    val apiImpl = loadApiImpl(
                        apiClazz,
                        "com.ysj.demo.aplugin.demo1.Demo1ComponentImpl",
                    )
                    if (args == null) method.invoke(apiImpl)
                    else method.invoke(apiImpl, *args)
                }
                else -> throw IllegalStateException("plugin not install.")
            }
        }
    }

    object PluginApiClassLoad : PluginClassLoadHook {
        override fun loadFromHost(name: String): Boolean {
            return name.startsWith("com.ysj.demo.aplugin.demo1.api")
        }
    }

    fun init(application: Application) {
        this.application = application
        Log.i(TAG, "init.")
    }

    fun onPluginUninstalled(plugin: Plugin) {
        when (plugin.name) {
            "demo_plugin1" -> {
                removeApiImpl(Demo1Component::class.java)
            }
        }
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T> proxy(clazz: Class<T>): T {
        Log.i(TAG, "proxy: $clazz")
        return Proxy.newProxyInstance(
            application.classLoader,
            arrayOf(clazz),
            InvocationHandlerImpl
        ) as T
    }

    private fun removeApiImpl(apiClass: Class<*>) {
        synchronized(apiImplMap) {
            apiImplMap.remove(apiClass)
        }
        Log.i(TAG, "removeApiImpl: $apiClass")
    }

    private fun loadApiImpl(apiClass: Class<*>, apiImplClassName: String): Any {
        return synchronized(apiImplMap) {
            var impl = apiImplMap[apiClass]
            if (impl == null) {
                val clazz = application.classLoader.loadClass(apiImplClassName)
                impl = clazz.kotlin.objectInstance ?: clazz.getConstructor().newInstance()
                apiImplMap[apiClass] = requireNotNull(impl)
                Log.i(TAG, "loadApiImpl: $impl}")
            }
            impl
        }
    }

}