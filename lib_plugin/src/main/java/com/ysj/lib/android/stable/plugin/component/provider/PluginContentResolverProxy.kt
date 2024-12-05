package com.ysj.lib.android.stable.plugin.component.provider

import android.content.ContentProviderClient
import android.content.ContentResolver
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.ContentObserver
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.util.Size
import androidx.annotation.RequiresApi
import com.ysj.lib.android.stable.plugin.StablePlugin
import java.io.InputStream
import java.io.OutputStream


/**
 * 仅用于在编译时代理插件内的 [ContentResolver] 的方法调用。
 *
 * @author Ysj
 * Create time: 2024/9/24
 */
internal object PluginContentResolverProxy {

    @JvmStatic
    fun acquireContentProviderClient(resolver: ContentResolver, name: String): ContentProviderClient? {
        // todo 遍历查找属于哪个进程的
        return resolver.acquireContentProviderClient(name)
    }

    @JvmStatic
    fun acquireUnstableContentProviderClient(resolver: ContentResolver, uri: Uri): ContentProviderClient? {
        return resolver.acquireUnstableContentProviderClient(wrapUri(uri))
    }

    @JvmStatic
    fun query(resolver: ContentResolver, uri: Uri, projection: Array<String?>?, selection: String?, selectionArgs: Array<String?>?, sortOrder: String?): Cursor? {
        return resolver.query(wrapUri(uri), projection, selection, selectionArgs, sortOrder)
    }

    @JvmStatic
    fun query(resolver: ContentResolver, uri: Uri, projection: Array<String?>?, selection: String?, selectionArgs: Array<String?>?, sortOrder: String?, cancellationSignal: CancellationSignal?): Cursor? {
        return resolver.query(wrapUri(uri), projection, selection, selectionArgs, sortOrder, cancellationSignal)
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.O)
    fun query(resolver: ContentResolver, uri: Uri, projection: Array<String?>?, queryArgs: Bundle?, cancellationSignal: CancellationSignal?): Cursor? {
        return resolver.query(wrapUri(uri), projection, queryArgs, cancellationSignal)
    }

    @JvmStatic
    fun insert(resolver: ContentResolver, url: Uri, values: ContentValues?): Uri? {
        return resolver.insert(wrapUri(url), values)
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.R)
    fun insert(resolver: ContentResolver, uri: Uri, initialValues: ContentValues?, extras: Bundle?): Uri? {
        return resolver.insert(wrapUri(uri), initialValues, extras)
    }

    @JvmStatic
    fun bulkInsert(resolver: ContentResolver, uri: Uri, initialValues: Array<ContentValues?>): Int {
        return resolver.bulkInsert(wrapUri(uri), initialValues)
    }

    @JvmStatic
    fun delete(resolver: ContentResolver, url: Uri, where: String?, selectionArgs: Array<String?>?): Int {
        return resolver.delete(wrapUri(url), where, selectionArgs)
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.R)
    fun delete(resolver: ContentResolver, uri: Uri, extras: Bundle?): Int {
        return resolver.delete(wrapUri(uri), extras)
    }

    @JvmStatic
    fun update(resolver: ContentResolver, uri: Uri, values: ContentValues?, where: String?, selectionArgs: Array<String?>?): Int {
        return resolver.update(wrapUri(uri), values, where, selectionArgs)
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.R)
    fun update(resolver: ContentResolver, uri: Uri, values: ContentValues?, extras: Bundle?): Int {
        return resolver.update(wrapUri(uri), values, extras)
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.Q)
    fun openFile(resolver: ContentResolver, uri: Uri, mode: String, signal: CancellationSignal?): ParcelFileDescriptor? {
        return resolver.openFile(wrapUri(uri), mode, signal)
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.Q)
    fun openAssetFile(resolver: ContentResolver, uri: Uri, mode: String, signal: CancellationSignal?): AssetFileDescriptor? {
        return resolver.openAssetFile(wrapUri(uri), mode, signal)
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.Q)
    fun openTypedAssetFile(resolver: ContentResolver, uri: Uri, mimeTypeFilter: String, opts: Bundle?, signal: CancellationSignal?): AssetFileDescriptor? {
        return resolver.openTypedAssetFile(wrapUri(uri), mimeTypeFilter, opts, signal)
    }

    @JvmStatic
    fun openInputStream(resolver: ContentResolver, uri: Uri): InputStream? {
        return resolver.openInputStream(wrapUri(uri))
    }

    @JvmStatic
    fun openOutputStream(resolver: ContentResolver, uri: Uri): OutputStream? {
        return resolver.openOutputStream(wrapUri(uri))
    }

    @JvmStatic
    fun openOutputStream(resolver: ContentResolver, uri: Uri, mode: String): OutputStream? {
        return resolver.openOutputStream(wrapUri(uri), mode)
    }

    @JvmStatic
    fun openFileDescriptor(resolver: ContentResolver, uri: Uri, mode: String): ParcelFileDescriptor? {
        return resolver.openFileDescriptor(wrapUri(uri), mode)
    }

    @JvmStatic
    fun openFileDescriptor(resolver: ContentResolver, uri: Uri, mode: String, cancellationSignal: CancellationSignal?): ParcelFileDescriptor? {
        return resolver.openFileDescriptor(wrapUri(uri), mode, cancellationSignal)
    }

    @JvmStatic
    fun registerContentObserver(resolver: ContentResolver, uri: Uri, notifyForDescendants: Boolean, observer: ContentObserver) {
        return resolver.registerContentObserver(uri, notifyForDescendants, observer)
    }

    @JvmStatic
    fun getType(resolver: ContentResolver, uri: Uri): String? {
        return resolver.getType(wrapUri(uri))
    }

    @JvmStatic
    fun getStreamTypes(resolver: ContentResolver, url: Uri, mimeTypeFilter: String): Array<String>? {
        return resolver.getStreamTypes(wrapUri(url), mimeTypeFilter)
    }

    @JvmStatic
    fun notifyChange(resolver: ContentResolver, uri: Uri, observer: ContentObserver?) {
        resolver.notifyChange(wrapUri(uri), observer)
    }

    @JvmStatic
    fun notifyChange(resolver: ContentResolver, uri: Uri, observer: ContentObserver?, syncToNetwork: Boolean) {
        @Suppress("DEPRECATION")
        resolver.notifyChange(wrapUri(uri), observer, syncToNetwork)
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.N)
    fun notifyChange(resolver: ContentResolver, uri: Uri, observer: ContentObserver?, flags: Int) {
        resolver.notifyChange(wrapUri(uri), observer, flags)
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.R)
    fun notifyChange(resolver: ContentResolver, uris: MutableCollection<Uri>, observer: ContentObserver?, flags: Int) {
        resolver.notifyChange(uris.mapTo(ArrayList()) { wrapUri(it) }, observer, flags)
    }

    @JvmStatic
    fun takePersistableUriPermission(resolver: ContentResolver, uri: Uri, modeFlags: Int) {
        resolver.takePersistableUriPermission(wrapUri(uri), modeFlags)
    }

    @JvmStatic
    fun releasePersistableUriPermission(resolver: ContentResolver, uri: Uri, modeFlags: Int) {
        resolver.releasePersistableUriPermission(wrapUri(uri), modeFlags)
    }

    @JvmStatic
    fun startSync(resolver: ContentResolver, uri: Uri?, extras: Bundle?) {
        @Suppress("DEPRECATION")
        resolver.startSync(uri?.let { wrapUri(it) }, extras)
    }

    @JvmStatic
    fun cancelSync(resolver: ContentResolver, uri: Uri?) {
        @Suppress("DEPRECATION")
        resolver.cancelSync(uri?.let { wrapUri(it) })
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.Q)
    fun loadThumbnail(resolver: ContentResolver, uri: Uri, size: Size, signal: CancellationSignal?): Bitmap {
        return resolver.loadThumbnail(wrapUri(uri), size, signal)
    }

    private fun wrapUri(uri: Uri): Uri {
        val plugin = StablePlugin.findPluginByClassLoader(javaClass.classLoader!!)
        requireNotNull(plugin)
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority("${StablePlugin.application.packageName}.stable.plugin.provider")
            .appendQueryParameter(PluginProvider.QUERY_KEY_WRAP, uri.toString())
            .appendQueryParameter(PluginProvider.QUERY_KEY_PLUGIN_NAME, plugin.name)
            .build()
    }

}