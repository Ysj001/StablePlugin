@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.ysj.lib.android.stable.plugin.component

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.TypedArray
import android.content.res.XmlResourceParser
import android.graphics.Movie
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import com.ysj.lib.android.stable.plugin.Plugin
import java.io.InputStream

/**
 * 有些低版本机型通过 [PackageManager.getResourcesForApplication]
 * 获取的 [Resources] 无法使用主包中的资源，这里做个兼容处理。
 *
 * 该方案类似官方 [androidx.appcompat.widget.ResourcesWrapper] 可以保证兼容性。
 *
 * @author Ysj
 * Create time: 2025/5/6
 */
internal class PluginResourceCompat(
    private val pluginResource: Resources,
    private val hostResource: Resources,
) : Resources(
    pluginResource.assets,
    pluginResource.displayMetrics,
    pluginResource.configuration,
) {

    companion object {

        private const val TAG = "PluginResourceCompat"

        fun getResourceFromPlugin(plugin: Plugin, base: Context): Resources {
            var res = base.packageManager.getResourcesForApplication(plugin.packageInfo.applicationInfo)
            try {
                // 测试是否能从 host 中获取应用图标，如果可以则不用兼容。
                ResourcesCompat.getDrawable(res, plugin.hostApplication.applicationInfo.icon, null)
            } catch (_: NotFoundException) {
                try {
                    res = PluginResourceCompat(res, base.resources)
                } catch (e: Exception) {
                    Log.w(TAG, "try compat plugin resource failure.", e)
                }
            }
            return res
        }
    }

    override fun getText(id: Int): CharSequence = call {
        getText(id)
    }

    override fun getText(id: Int, def: CharSequence?): CharSequence = call {
        getText(id, def)
    }

    override fun getFont(id: Int): Typeface = call {
        getFont(id)
    }

    override fun getQuantityText(id: Int, quantity: Int): CharSequence = call {
        getQuantityText(id, quantity)
    }

    override fun getString(id: Int): String = call {
        getString(id)
    }

    override fun getString(id: Int, vararg formatArgs: Any?): String = call {
        getString(id, *formatArgs)
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any?): String = call {
        getQuantityString(id, quantity, *formatArgs)
    }

    override fun getQuantityString(id: Int, quantity: Int): String = call {
        getQuantityString(id, quantity)
    }

    override fun getTextArray(id: Int): Array<CharSequence> = call {
        getTextArray(id)
    }

    override fun getStringArray(id: Int): Array<String> = call {
        getStringArray(id)
    }

    override fun getIntArray(id: Int): IntArray = call {
        getIntArray(id)
    }

    override fun obtainTypedArray(id: Int): TypedArray = call {
        obtainTypedArray(id)
    }

    override fun getDimension(id: Int): Float = call {
        getDimension(id)
    }

    override fun getDimensionPixelOffset(id: Int): Int = call {
        getDimensionPixelOffset(id)
    }

    override fun getDimensionPixelSize(id: Int): Int = call {
        getDimensionPixelSize(id)
    }

    override fun getFraction(id: Int, base: Int, pbase: Int): Float = call {
        getFraction(id, base, pbase)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun getDrawable(id: Int): Drawable = call {
        getDrawable(id)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun getDrawable(id: Int, theme: Theme?): Drawable = call {
        getDrawable(id, theme)
    }

    override fun getDrawableForDensity(id: Int, density: Int): Drawable? = call {
        getDrawableForDensity(id, density)
    }

    override fun getDrawableForDensity(id: Int, density: Int, theme: Theme?): Drawable? = call {
        getDrawableForDensity(id, density, theme)
    }

    override fun getMovie(id: Int): Movie = call {
        getMovie(id)
    }

    override fun getColor(id: Int): Int = call {
        getColor(id)
    }

    override fun getColor(id: Int, theme: Theme?): Int = call {
        getColor(id, theme)
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    override fun getColorStateList(id: Int): ColorStateList = call {
        getColorStateList(id)
    }

    override fun getColorStateList(id: Int, theme: Theme?): ColorStateList = call {
        getColorStateList(id, theme)
    }

    override fun getBoolean(id: Int): Boolean = call {
        getBoolean(id)
    }

    override fun getInteger(id: Int): Int = call {
        getInteger(id)
    }

    override fun getFloat(id: Int): Float = call {
        getFloat(id)
    }

    override fun getLayout(id: Int): XmlResourceParser = call {
        getLayout(id)
    }

    override fun getAnimation(id: Int): XmlResourceParser = call {
        getAnimation(id)
    }

    override fun getXml(id: Int): XmlResourceParser = call {
        getXml(id)
    }

    override fun openRawResource(id: Int): InputStream = call {
        openRawResource(id)
    }

    override fun openRawResource(id: Int, value: TypedValue?): InputStream = call {
        openRawResource(id, value)
    }

    override fun openRawResourceFd(id: Int): AssetFileDescriptor = call {
        openRawResourceFd(id)
    }

    override fun getValue(id: Int, outValue: TypedValue?, resolveRefs: Boolean) = call {
        getValue(id, outValue, resolveRefs)
    }

    @SuppressLint("DiscouragedApi")
    override fun getValue(name: String?, outValue: TypedValue?, resolveRefs: Boolean) = call {
        getValue(name, outValue, resolveRefs)
    }

    override fun getValueForDensity(id: Int, density: Int, outValue: TypedValue?, resolveRefs: Boolean) = call {
        getValueForDensity(id, density, outValue, resolveRefs)
    }

    override fun obtainAttributes(set: AttributeSet?, attrs: IntArray?): TypedArray = call {
        obtainAttributes(set, attrs)
    }

    override fun updateConfiguration(config: Configuration?, metrics: DisplayMetrics?) = call {
        updateConfiguration(config, metrics)
    }

    override fun getDisplayMetrics(): DisplayMetrics = call {
        displayMetrics
    }

    override fun getConfiguration(): Configuration = call {
        configuration
    }

    @SuppressLint("DiscouragedApi")
    override fun getIdentifier(name: String?, defType: String?, defPackage: String?): Int = call {
        getIdentifier(name, defType, defPackage)
    }

    override fun getResourceName(resid: Int): String = call {
        getResourceName(resid)
    }

    override fun getResourcePackageName(resid: Int): String = call {
        getResourcePackageName(resid)
    }

    override fun getResourceTypeName(resid: Int): String = call {
        getResourceTypeName(resid)
    }

    override fun getResourceEntryName(resid: Int): String = call {
        getResourceEntryName(resid)
    }

    override fun parseBundleExtras(parser: XmlResourceParser?, outBundle: Bundle?) = call {
        parseBundleExtras(parser, outBundle)
    }

    override fun parseBundleExtra(tagName: String?, attrs: AttributeSet?, outBundle: Bundle?) = call {
        parseBundleExtra(tagName, attrs, outBundle)
    }

    private fun <T> call(b: Resources.() -> T): T {
        return try {
            pluginResource.b()
        } catch (_: NotFoundException) {
            hostResource.b()
        }
    }

}