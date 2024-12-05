package com.ysj.lib.android.stable.plugin.component.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.ysj.lib.android.stable.plugin.StablePlugin

/**
 * [ContentProvider] 坑位，用于代理插件的 [ContentProvider] 调用。
 *
 * @author Ysj
 * Create time: 2024/9/24
 */
internal class PluginProvider : ContentProvider() {

    companion object {
        private const val TAG = "PluginProvider"

        const val QUERY_KEY_WRAP = "wrap"
        const val QUERY_KEY_PLUGIN_NAME = "plugin_name"
    }

    private val Uri.pluginName get() = requireNotNull(getQueryParameter(QUERY_KEY_PLUGIN_NAME))
    private val Uri.unwrap get() = requireNotNull(getQueryParameter(QUERY_KEY_WRAP)).toUri()

    private val Uri.provider get() = StablePlugin.findPluginByName(pluginName)

    override fun onCreate(): Boolean {
        Log.i(TAG, "onCreate.")
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        val unwrap = uri.unwrap
        val authority = unwrap.authority ?: return null
        return uri.provider?.providerMap?.get(authority)?.query(unwrap, projection, selection, selectionArgs, sortOrder)
    }

    override fun getType(uri: Uri): String? {
        val unwrap = uri.unwrap
        val authority = unwrap.authority ?: return null
        return uri.provider?.providerMap?.get(authority)?.getType(unwrap)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val unwrap = uri.unwrap
        val authority = unwrap.authority ?: return null
        return uri.provider?.providerMap?.get(authority)?.insert(unwrap, values)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val unwrap = uri.unwrap
        val authority = unwrap.authority ?: return 0
        return uri.provider?.providerMap?.get(authority)?.delete(unwrap, selection, selectionArgs) ?: 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val unwrap = uri.unwrap
        val authority = unwrap.authority ?: return 0
        return uri.provider
            ?.providerMap?.get(authority)
            ?.update(unwrap, values, selection, selectionArgs)
            ?: 0
    }

}