package com.ysj.lib.android.stable.plugin.component.provider

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.annotation.RequiresApi
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

    private val Uri.unwrap get() = getQueryParameter(QUERY_KEY_WRAP)?.toUri()

    override fun onCreate(): Boolean {
        Log.i(TAG, "onCreate.")
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        val unwrap = uri.unwrap
            ?: return null
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.query(unwrap, projection, selection, selectionArgs, sortOrder)
            is ContentResolver -> contentInterface.query(unwrap, projection, selection, selectionArgs, sortOrder)
            else -> null
        }
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?, cancellationSignal: CancellationSignal?): Cursor? {
        val unwrap = uri.unwrap
            ?: return super.query(uri, projection, selection, selectionArgs, sortOrder, cancellationSignal)
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.query(unwrap, projection, selection, selectionArgs, sortOrder, cancellationSignal)
            is ContentResolver -> contentInterface.query(unwrap, projection, selection, selectionArgs, sortOrder, cancellationSignal)
            else -> super.query(unwrap, projection, selection, selectionArgs, sortOrder, cancellationSignal)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun query(uri: Uri, projection: Array<out String>?, queryArgs: Bundle?, cancellationSignal: CancellationSignal?): Cursor? {
        val unwrap = uri.unwrap
            ?: return super.query(uri, projection, queryArgs, cancellationSignal)
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.query(unwrap, projection, queryArgs, cancellationSignal)
            is ContentResolver -> contentInterface.query(unwrap, projection, queryArgs, cancellationSignal)
            else -> super.query(unwrap, projection, queryArgs, cancellationSignal)
        }
    }

    override fun getType(uri: Uri): String? {
        val unwrap = uri.unwrap
            ?: return null
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.getType(unwrap)
            is ContentResolver -> contentInterface.getType(unwrap)
            else -> null
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun insert(uri: Uri, values: ContentValues?, extras: Bundle?): Uri? {
        val unwrap = uri.unwrap
            ?: return super.insert(uri, values, extras)
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.insert(unwrap, values, extras)
            is ContentResolver -> contentInterface.insert(unwrap, values, extras)
            else -> super.insert(unwrap, values, extras)
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val unwrap = uri.unwrap
            ?: return null
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.insert(unwrap, values)
            is ContentResolver -> contentInterface.insert(unwrap, values)
            else -> null
        }
    }

    override fun bulkInsert(uri: Uri, values: Array<out ContentValues>): Int {
        val unwrap = uri.unwrap
            ?: return super.bulkInsert(uri, values)
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.bulkInsert(unwrap, values)
            is ContentResolver -> contentInterface.bulkInsert(unwrap, values)
            else -> super.bulkInsert(unwrap, values)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun delete(uri: Uri, extras: Bundle?): Int {
        val unwrap = uri.unwrap
            ?: return super.delete(uri, extras)
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.delete(unwrap, extras)
            is ContentResolver -> contentInterface.delete(unwrap, extras)
            else -> super.delete(unwrap, extras)
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val unwrap = uri.unwrap
            ?: return 0
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.delete(unwrap, selection, selectionArgs)
            is ContentResolver -> contentInterface.delete(unwrap, selection, selectionArgs)
            else -> 0
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun update(uri: Uri, values: ContentValues?, extras: Bundle?): Int {
        val unwrap = uri.unwrap
            ?: return super.update(uri, values, extras)
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.update(unwrap, values, extras)
            is ContentResolver -> contentInterface.update(unwrap, values, extras)
            else -> super.update(unwrap, values, extras)
        }
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val unwrap = uri.unwrap
            ?: return 0
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.update(unwrap, values, selection, selectionArgs)
            is ContentResolver -> contentInterface.update(unwrap, values, selection, selectionArgs)
            else -> 0
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val unwrap = uri.unwrap
            ?: return openFile(uri, mode)
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.openFile(unwrap, mode)
            is ContentResolver -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentInterface.openFile(unwrap, mode, null)
            } else {
                super.openFile(unwrap, mode)
            }
            else -> super.openFile(unwrap, mode)
        }
    }

    override fun openFile(uri: Uri, mode: String, signal: CancellationSignal?): ParcelFileDescriptor? {
        val unwrap = uri.unwrap
            ?: return super.openFile(uri, mode, signal)
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.openFile(unwrap, mode, signal)
            is ContentResolver -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentInterface.openFile(unwrap, mode, signal)
            } else {
                super.openFile(unwrap, mode, signal)
            }
            else -> super.openFile(unwrap, mode, signal)
        }
    }

    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        val unwrap = uri.unwrap
            ?: return super.openAssetFile(uri, mode)
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.openAssetFile(unwrap, mode)
            is ContentResolver -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentInterface.openAssetFile(unwrap, mode, null)
            } else {
                super.openAssetFile(unwrap, mode)
            }
            else -> super.openAssetFile(unwrap, mode)
        }
    }

    override fun openAssetFile(uri: Uri, mode: String, signal: CancellationSignal?): AssetFileDescriptor? {
        val unwrap = uri.unwrap
            ?: return super.openAssetFile(uri, mode, signal)
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.openAssetFile(unwrap, mode, signal)
            is ContentResolver -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentInterface.openAssetFile(unwrap, mode, signal)
            } else {
                super.openAssetFile(unwrap, mode, signal)
            }
            else -> super.openAssetFile(unwrap, mode, signal)
        }
    }

    override fun getStreamTypes(uri: Uri, mimeTypeFilter: String): Array<String>? {
        val unwrap = uri.unwrap
            ?: return super.getStreamTypes(uri, mimeTypeFilter)
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.getStreamTypes(unwrap, mimeTypeFilter)
            is ContentResolver -> contentInterface.getStreamTypes(unwrap, mimeTypeFilter)
            else -> super.getStreamTypes(unwrap, mimeTypeFilter)
        }
    }

    override fun openTypedAssetFile(uri: Uri, mimeTypeFilter: String, opts: Bundle?): AssetFileDescriptor? {
        val unwrap = uri.unwrap
            ?: return super.openTypedAssetFile(uri, mimeTypeFilter, opts)
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.openTypedAssetFile(unwrap, mimeTypeFilter, opts)
            is ContentResolver -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentInterface.openTypedAssetFile(unwrap, mimeTypeFilter, opts, null)
            } else {
                super.openTypedAssetFile(unwrap, mimeTypeFilter, opts)
            }
            else -> super.openTypedAssetFile(unwrap, mimeTypeFilter, opts)
        }
    }

    override fun openTypedAssetFile(uri: Uri, mimeTypeFilter: String, opts: Bundle?, signal: CancellationSignal?): AssetFileDescriptor? {
        val unwrap = uri.unwrap
            ?: return super.openTypedAssetFile(uri, mimeTypeFilter, opts, signal)
        return when (val contentInterface = findContentInterface(uri, unwrap)) {
            is ContentProvider -> contentInterface.openTypedAssetFile(unwrap, mimeTypeFilter, opts, signal)
            is ContentResolver -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentInterface.openTypedAssetFile(unwrap, mimeTypeFilter, opts, signal)
            } else {
                super.openTypedAssetFile(unwrap, mimeTypeFilter, opts, signal)
            }
            else -> super.openTypedAssetFile(unwrap, mimeTypeFilter, opts, signal)
        }
    }

    private fun findContentInterface(uri: Uri, unwrapUri: Uri): Any {
        val authority = unwrapUri.authority
        val context = context ?: StablePlugin.application
        if (authority.isNullOrEmpty() || !authority.startsWith(context.packageName)) {
            return context.contentResolver
        }
        val providerMap = uri.getQueryParameter(QUERY_KEY_PLUGIN_NAME)
            ?.let { StablePlugin.findPluginByName(it) }
            ?.providerMap
            ?: return context.contentResolver
        return providerMap[authority] ?: context.contentResolver
    }

}