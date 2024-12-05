package com.ysj.lib.android.stable.plugin.config

import com.ysj.lib.android.stable.plugin.StablePlugin

/**
 * 用于构建 [StablePlugin] 所需的配置。
 *
 * @author Ysj
 * Create time: 2024/9/18
 */
class StablePluginConfig private constructor(builder: Builder) {

    internal val isDebugEnable = builder.isDebugEnable

    internal val eventCallback = builder.eventCallback

    internal val pluginClassLoadFilter = builder.pluginClassLoadFilter

    class Builder {

        internal var isDebugEnable = false

        internal var eventCallback: EventCallback? = null

        internal var pluginClassLoadFilter: PluginClassLoadFilter? = null

        fun debugEnable(enable: Boolean) = apply {
            this.isDebugEnable = enable
        }

        fun eventCallback(callback: EventCallback) = apply {
            this.eventCallback = callback
        }

        fun pluginClassLoadFilter(filter: PluginClassLoadFilter) = apply {
            this.pluginClassLoadFilter = filter
        }

        fun build(): StablePluginConfig {
            return StablePluginConfig(this)
        }
    }

}