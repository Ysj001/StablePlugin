package com.ysj.demo.aplugin.demo1

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log

/**
 * 用于演示对 [ContentProvider] 的支持。
 *
 * @author Ysj
 * Create time: 2024/9/24
 */
class Demo1Provider : ContentProvider() {

    companion object {
        private const val TAG = "Demo1Provider"

        const val URI_QUERY_KEY_QUESTION = "question"

        const val COLUMNS_ANSWER = "answer"
    }

    override fun attachInfo(context: Context?, info: ProviderInfo?) {
        super.attachInfo(context, info)
        Log.i(TAG, "attachInfo.")
    }

    override fun onCreate(): Boolean {
        Log.i(TAG, "onCreate.")
        return false
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        Log.i(TAG, "query. $uri")
        val columns = arrayOf(COLUMNS_ANSWER)
        val cursor = MatrixCursor(columns, 1)
        cursor.addRow(arrayOf(javaClass.name))
        return cursor
    }

    override fun getType(uri: Uri): String? {
        Log.i(TAG, "getType: $uri")
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.i(TAG, "insert: $uri")
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.i(TAG, "delete: $uri")
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.i(TAG, "update: $uri")
        return 0
    }

}