package com.ysj.lib.android.stable.plugin.component.provider

import android.content.pm.PackageManager
import android.os.Bundle
import com.ysj.lib.android.stable.plugin.StablePlugin

/**
 * 兼用 AppInitializer 中 discoverAndInitialize 获取 metaDate 逻辑。
 *
 * @author Ysj
 * Create time: 2024/12/5
 */
internal object InitializationProviderCompat {

    private const val INITIALIZATION_PROVIDER_NAME = "androidx.startup.InitializationProvider"

    @JvmStatic
    fun getInitializationProviderMetaData(): Bundle {
        val providers = StablePlugin
            .findPluginByClassLoader(requireNotNull(javaClass.classLoader))
            ?.packageInfo
            ?.providers
            ?: throw PackageManager.NameNotFoundException(INITIALIZATION_PROVIDER_NAME)
        val provider = providers.find {
            it.name == INITIALIZATION_PROVIDER_NAME
        } ?: throw PackageManager.NameNotFoundException(INITIALIZATION_PROVIDER_NAME)
        val metaData = provider.metaData
        return if (metaData == null) {
            Bundle.EMPTY
        } else {
            Bundle(metaData).apply {
                remove("androidx.lifecycle.ProcessLifecycleInitializer")
                remove("androidx.profileinstaller.ProfileInstallerInitializer")
                remove("androidx.emoji2.text.EmojiCompatInitializer")
            }
        }
    }

}