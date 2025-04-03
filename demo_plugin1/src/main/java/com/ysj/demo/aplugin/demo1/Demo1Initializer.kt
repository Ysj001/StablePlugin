package com.ysj.demo.aplugin.demo1

import android.content.Context
import android.util.Log
import androidx.startup.Initializer

/**
 * 用于演示对 [Initializer] 的支持。
 *
 * @author Ysj
 * Create time: 2024/12/6
 */
class Demo1Initializer : Initializer<Unit> {

    companion object {
        private const val TAG = "Demo1Initializer"
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    override fun create(context: Context) {
        Log.i(TAG, "create.")
    }

}