package com.ysj.demo.aplugin.demo1

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * 用于演示对 [Service] 的支持。
 *
 * @author Ysj
 * Create time: 2024/11/18
 */
class Demo1Service : Service() {

    companion object {
        private const val TAG = "Demo1Service"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand.")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i(TAG, "onBind.")
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy.")
    }

}