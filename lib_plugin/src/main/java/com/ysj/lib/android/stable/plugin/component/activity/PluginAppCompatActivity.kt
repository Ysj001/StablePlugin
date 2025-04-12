package com.ysj.lib.android.stable.plugin.component.activity

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * 插件 [AppCompatActivity]。
 *
 * @author Ysj
 * Create time: 2024/9/23
 */
abstract class PluginAppCompatActivity : AppCompatActivity() {

    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    override fun getLastNonConfigurationInstance(): Any? {
        // plugin 更新后 classloader 会变更，这里需要判断
        return super.getLastNonConfigurationInstance().takeIf {
            it is NonConfigurationInstances
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

    override fun getClassLoader(): ClassLoader {
        return baseContext.classLoader
    }

    override fun getAssets(): AssetManager {
        return baseContext.resources.assets
    }

    override fun getResources(): Resources {
        return baseContext.resources
    }

}