package com.ysj.lib.android.stable.plugin.component

import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo
import com.ysj.lib.android.stable.plugin.StablePlugin

/**
 * 用于兼用插件使用 [PackageManager]，使其相关调用返回的信息都优先返回插件的。
 *
 * - 注意：该类在编译时被使用，编译后自动代理相关调用。
 *
 * @author Ysj
 * Create time: 2025/4/20
 */
internal object PluginPackageManagerCompat {

    @JvmStatic
    fun getPackageInfo(pm: PackageManager, packageName: String, flags: Int): PackageInfo {
        return StablePlugin
            .findPluginByClassLoader(requireNotNull(javaClass.classLoader))
            ?.packageInfo
            ?.takeIf { it.packageName == packageName }
            ?: return pm.getPackageInfo(packageName, flags)
    }

    @JvmStatic
    fun getApplicationInfo(pm: PackageManager, packageName: String, flags: Int): ApplicationInfo {
        return StablePlugin
            .findPluginByClassLoader(requireNotNull(javaClass.classLoader))
            ?.packageInfo?.applicationInfo
            ?.takeIf { it.packageName == packageName }
            ?: return pm.getApplicationInfo(packageName, flags)
    }

    @JvmStatic
    fun getActivityInfo(pm: PackageManager, component: ComponentName, flags: Int): ActivityInfo {
        return StablePlugin
            .findPluginByClassLoader(requireNotNull(javaClass.classLoader))
            ?.packageInfo?.activities
            ?.find { it.packageName == component.packageName && it.name == component.className }
            ?: return pm.getActivityInfo(component, flags)
    }

    @JvmStatic
    fun getProviderInfo(pm: PackageManager, component: ComponentName, flags: Int): ProviderInfo {
        return StablePlugin
            .findPluginByClassLoader(requireNotNull(javaClass.classLoader))
            ?.packageInfo?.providers
            ?.find { it.packageName == component.packageName && it.name == component.className }
            ?: return pm.getProviderInfo(component, flags)
    }

    @JvmStatic
    fun getServiceInfo(pm: PackageManager, component: ComponentName, flags: Int): ServiceInfo {
        return StablePlugin
            .findPluginByClassLoader(requireNotNull(javaClass.classLoader))
            ?.packageInfo?.services
            ?.find { it.packageName == component.packageName && it.name == component.className }
            ?: return pm.getServiceInfo(component, flags)
    }

}