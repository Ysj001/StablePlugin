package com.ysj.lib.android.stable.plugin.config

import com.ysj.lib.android.stable.plugin.Plugin

/**
 * 定义事件回调接口。
 *
 * @author Ysj
 * Create time: 2024/9/18
 */
interface EventCallback {

    /**
     * 当初始化成功时回调。
     */
    fun onInitialized()

    /**
     * 当有插件被成功安装时回调。
     */
    fun onPluginInstalled(plugin: Plugin)

    /**
     * 当有插件被卸载时回调。
     */
    fun onPluginUninstalled(plugin: Plugin)

}