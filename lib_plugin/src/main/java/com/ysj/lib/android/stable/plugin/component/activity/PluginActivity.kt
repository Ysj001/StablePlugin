package com.ysj.lib.android.stable.plugin.component.activity

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import android.os.Parcel
import com.ysj.lib.android.stable.plugin.Plugin
import com.ysj.lib.android.stable.plugin.StablePlugin

/**
 * 插件 [Activity]。
 *
 * @author Ysj
 * Create time: 2024/9/23
 */
internal abstract class PluginActivity : Activity() {

    companion object {

        private const val TAG = "PluginActivity"

        private const val KEY_FROM_PLUGIN_PREFIX = "KEY_FROM_PLUGIN_"

        fun findFromPlugin(cl: ClassLoader, bundle: Bundle?): Plugin? {
            bundle ?: return null
            val tmp = Parcel.obtain()
            try {
                // 这里读到新的 Parcel 中，不会影响原始的 bundle 加载
                bundle.writeToParcel(tmp, 0)
                tmp.setDataPosition(0)
                try {
                    return tmp.readBundle(cl)
                        ?.keySet()
                        ?.find { it.startsWith(KEY_FROM_PLUGIN_PREFIX) }
                        ?.substring(KEY_FROM_PLUGIN_PREFIX.length)
                        ?.let { StablePlugin.findPluginByName(it) }
                } catch (_: Exception) {
                    for (plugin in StablePlugin.allInstalledPlugins()) {
                        tmp.setDataPosition(0)
                        val keySet: Set<String>
                        try {
                            keySet = tmp.readBundle(plugin.classLoader)
                                ?.keySet()
                                ?: continue
                        } catch (_: Exception) {
                            continue
                        }
                        return keySet
                            .find { it.startsWith(KEY_FROM_PLUGIN_PREFIX) }
                            ?.substring(KEY_FROM_PLUGIN_PREFIX.length)
                            ?.let { StablePlugin.findPluginByName(it) }
                    }
                }
            } finally {
                tmp.recycle()
            }
            return null
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val context = when (newBase) {
            is PluginActivityContext -> newBase
            is ContextWrapper -> {
                var curr = newBase
                while (curr is ContextWrapper && curr !is PluginActivityContext) {
                    curr = curr.baseContext
                }
                if (curr is PluginActivityContext) {
                    newBase
                } else {
                    PluginActivityContext(newBase, javaClass)
                }
            }
            else -> PluginActivityContext(newBase, javaClass)
        }
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.classLoader = classLoader
        intent.setExtrasClassLoader(classLoader)
        super.onCreate(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.setExtrasClassLoader(classLoader)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun getClassLoader(): ClassLoader {
        return baseContext.classLoader
    }

    override fun getAssets(): AssetManager {
        return baseContext.resources.assets
    }

    override fun getResources(): Resources {
        return baseContext.resources
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
        val plugin = StablePlugin.findPluginByClassLoader(classLoader)
        if (plugin != null && intent != null) {
            intent.putExtra("${KEY_FROM_PLUGIN_PREFIX}${plugin.name}", false)
        }
        super.startActivityForResult(intent, requestCode, options)
    }

}