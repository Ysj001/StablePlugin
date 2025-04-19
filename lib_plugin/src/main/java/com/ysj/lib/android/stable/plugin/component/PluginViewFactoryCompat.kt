package com.ysj.lib.android.stable.plugin.component

import android.content.Context
import android.util.AttributeSet
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentContainerView

/**
 * 用于兼容插件中的 [View] 加载。
 *
 * @author Ysj
 * Create time: 2025/4/19
 */
class PluginViewFactoryCompat(
    private val classLoader: ClassLoader,
) : LayoutInflater.Factory {

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