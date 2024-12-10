package com.ysj.lib.android.stable.plugin.component.service

import android.app.Service
import android.content.Context
import android.content.ContextWrapper

/**
 * 插件 [Service]。
 *
 * @author Ysj
 * Create time: 2024/12/6
 */
internal abstract class PluginService : Service() {

    override fun attachBaseContext(newBase: Context) {
        val context = when (newBase) {
            is PluginServiceContext -> newBase
            is ContextWrapper -> {
                var curr = newBase
                while (curr is ContextWrapper && curr !is PluginServiceContext) {
                    curr = curr.baseContext
                }
                if (curr is PluginServiceContext) {
                    newBase
                } else {
                    PluginServiceContext(newBase, javaClass)
                }
            }
            else -> PluginServiceContext(newBase, javaClass)
        }
        super.attachBaseContext(context)
    }

}