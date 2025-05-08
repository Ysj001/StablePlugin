package com.ysj.lib.android.stable.plugin.component.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
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
        super.attachBaseContext(PluginActivityContext(newBase, javaClass))
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

    override fun overrideActivityTransition(overrideType: Int, enterAnim: Int, exitAnim: Int, backgroundColor: Int) {
        var realEnterAnim = enterAnim
        var realExitAnim = exitAnim
        if (enterAnim != ResourcesCompat.ID_NULL) {
            try {
                application.resources.getAnimation(enterAnim)
            } catch (_: Exception) {
                realEnterAnim = ResourcesCompat.ID_NULL
                Log.w(TAG, "load anim failure. try use public.xml fixed. enterAnim=0x${Integer.toHexString(enterAnim)}}")
            }
        }
        if (exitAnim != ResourcesCompat.ID_NULL) {
            try {
                application.resources.getAnimation(exitAnim)
            } catch (_: Exception) {
                realExitAnim = ResourcesCompat.ID_NULL
                Log.w(TAG, "load anim failure. try use public.xml fixed. exitAnim=0x${Integer.toHexString(exitAnim)}}")
            }
        }
        super.overrideActivityTransition(overrideType, realEnterAnim, realExitAnim, backgroundColor)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java", ReplaceWith("super.overridePendingTransition(enterAnim, exitAnim, backgroundColor)", "android.app.Activity"))
    override fun overridePendingTransition(enterAnim: Int, exitAnim: Int, backgroundColor: Int) {
        var realEnterAnim = enterAnim
        var realExitAnim = exitAnim
        if (enterAnim != ResourcesCompat.ID_NULL) {
            try {
                application.resources.getAnimation(enterAnim)
            } catch (_: Exception) {
                realEnterAnim = ResourcesCompat.ID_NULL
                Log.w(TAG, "load anim failure. try use public.xml fixed. enterAnim=0x${Integer.toHexString(enterAnim)}}")
            }
        }
        if (exitAnim != ResourcesCompat.ID_NULL) {
            try {
                application.resources.getAnimation(exitAnim)
            } catch (_: Exception) {
                realExitAnim = ResourcesCompat.ID_NULL
                Log.w(TAG, "load anim failure. try use public.xml fixed. exitAnim=0x${Integer.toHexString(exitAnim)}}")
            }
        }
        super.overridePendingTransition(realEnterAnim, realExitAnim, backgroundColor)
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
                if (enterId != ResourcesCompat.ID_NULL) {
                    requireNotNull(application.resources.getAnimation(enterId))
                }
                if (exitId != ResourcesCompat.ID_NULL) {
                    requireNotNull(application.resources.getAnimation(exitId))
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

}