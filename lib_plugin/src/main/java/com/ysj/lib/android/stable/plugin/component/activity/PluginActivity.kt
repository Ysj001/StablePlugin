package com.ysj.lib.android.stable.plugin.component.activity

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.util.Log
import androidx.core.content.res.ResourcesCompat
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

        private var hostAppResTmp: Resources? = null

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
        if (options != null) {
            val enterId = options.getInt("android:activity.animEnterRes", ResourcesCompat.ID_NULL)
            val exitId = options.getInt("android:activity.animExitRes", ResourcesCompat.ID_NULL)
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    if (enterId != ResourcesCompat.ID_NULL) {
                        requireNotNull(obtainOnlyHostAppRes().getAnimation(enterId))
                    }
                    if (exitId != ResourcesCompat.ID_NULL) {
                        requireNotNull(obtainOnlyHostAppRes().getAnimation(exitId))
                    }
                } else {
                    if (enterId != ResourcesCompat.ID_NULL) {
                        requireNotNull(application.resources.getAnimation(enterId))
                    }
                    if (exitId != ResourcesCompat.ID_NULL) {
                        requireNotNull(application.resources.getAnimation(exitId))
                    }
                }
            } catch (_: Exception) {
                /*
                    说明转场资源不在宿主中
                    由于转场资源的解析在 AMS 中，无法解析到插件中的资源，
                    会导致无转场效果甚至在部分低版本机型上会黑屏，
                    因此这里兼容处理，不使用转场。
                    你可以将该资源放到宿主中，并使用 public.xml 固定资源 id 来解决。
                 */
                Log.w(TAG, "load anim failure. try use public.xml fixed. enterId=0x${Integer.toHexString(enterId)} , exitId=0x${Integer.toHexString(exitId)}")
                super.startActivityForResult(intent, requestCode, null)
                return
            }
        }
        super.startActivityForResult(intent, requestCode, options)
    }

    private fun obtainOnlyHostAppRes(): Resources {
        var resources = hostAppResTmp
        if (resources != null) {
            return resources
        }
        // 由于低版通过 package 查找时会复用 resource 和其中的 assets，
        // 会导致 host 的 resource 会被添加插件的资源路径，因此这里重新创建独立的
        val assetManagerClass = AssetManager::class.java
        val assetManager = assetManagerClass
            .getDeclaredConstructor()
            .apply { isAccessible = true }
            .newInstance()
        assetManagerClass
            .getMethod("addAssetPath", String::class.java)
            .apply { isAccessible = true }
            .invoke(assetManager, application.applicationInfo.sourceDir)
        @Suppress("DEPRECATION")
        resources = Resources(assetManager, this.resources.displayMetrics, this.resources.configuration)
        hostAppResTmp = resources
        return resources
    }

}