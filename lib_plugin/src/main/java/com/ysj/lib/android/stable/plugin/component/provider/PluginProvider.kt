package com.ysj.lib.android.stable.plugin.component.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * [ContentProvider] 坑位，用于代理插件的 [ContentProvider] 调用。
 *
 * @author Ysj
 * Create time: 2024/9/24
 */
class PluginProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val a = context!!.contentResolver
        val name = "b"
        a.acquireContentProviderClient(name)
        PluginContentResolverProxy.acquireContentProviderClient(a, name)
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {

        return 0
    }


}