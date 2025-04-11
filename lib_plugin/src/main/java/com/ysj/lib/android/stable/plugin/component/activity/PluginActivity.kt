package com.ysj.lib.android.stable.plugin.component.activity

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import com.ysj.lib.android.stable.plugin.StablePlugin

/**
 * 插件 [Activity]。
 *
 * @author Ysj
 * Create time: 2024/9/23
 */
internal abstract class PluginActivity : Activity() {

    companion object {
        internal const val KEY_FROM_PLUGIN = "KEY_FROM_PLUGIN"
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
        intent.setExtrasClassLoader(classLoader)
        super.onCreate(savedInstanceState)
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
            intent.putExtra(KEY_FROM_PLUGIN, plugin.name)
        }
        super.startActivityForResult(intent, requestCode, options)
    }

}