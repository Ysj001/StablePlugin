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
import android.util.AttributeSet
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentContainerView
import com.ysj.lib.android.stable.plugin.StablePlugin
import com.ysj.lib.android.stable.plugin.StablePlugin.pluginInstalledFile


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
                layoutInflater.setFactory(PluginViewFactory())
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

    private inner class PluginViewFactory : LayoutInflater.Factory {
        override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
            if ("." !in name) {
                // system view 不处理
                return null
            }
            if (name == FragmentContainerView::class.java.name) {
                // FragmentContainerView 不处理
                return null
            }
            /*
                像 androidx 中的 view 可能同时被 host 和插件依赖，
                但是打包 apk 后编译的 R 文件资源不是一样的，
                因此必须从插件的 classLoader 去加载 view，避免内部的资源属性对不上。
             */
            val clazz = classLoader
                .runCatching { loadClass(name) }
                .getOrNull()
                ?: return null
            try {
                return clazz
                    .getConstructor(Context::class.java, AttributeSet::class.java)
                    .newInstance(context, attrs) as View
            } catch (e: Exception) {
                throw InflateException(
                    "plugin create view failure. name=$name , desc=${attrs.positionDescription}",
                    e
                )
            }
        }
    }

}