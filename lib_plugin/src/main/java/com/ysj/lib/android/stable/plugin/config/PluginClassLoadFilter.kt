package com.ysj.lib.android.stable.plugin.config

/**
 * 用于配置插件中的类是否使用插件的类加载器加载，可用于避免类加载器不一致造成的问题。
 *
 * @author Ysj
 * Create time: 2024/12/4
 */
interface PluginClassLoadFilter {

    /**
     * 如果返回 true 则该类不会优先使用插件的类加载器。
     */
    fun filter(name: String): Boolean

}