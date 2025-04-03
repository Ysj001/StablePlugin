package com.ysj.demo.aplugin.demo1

import android.app.Application
import android.content.Context
import android.util.Log

/**
 * 演示对 [Application] 的支持。
 *
 * @author Ysj
 * Create time: 2024/9/14
 */
class Demo1Application : Application() {

    companion object {
        private const val TAG = "Demo1Application"
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Log.i(TAG, "attachBaseContext.")
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate.")
    }

}