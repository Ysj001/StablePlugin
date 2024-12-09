package com.ysj.lib.android.stable.plugin.config

/**
 * 用于控制插件中类加载的钩子。
 *
 * @author Ysj
 * Create time: 2024/12/4
 */
interface PluginClassLoadHook {

    /**
     * @return 返回 true 表示该类优先从 host 加载。
     */
    fun loadFromHost(name: String): Boolean

}