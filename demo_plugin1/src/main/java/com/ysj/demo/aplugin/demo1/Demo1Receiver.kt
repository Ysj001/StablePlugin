package com.ysj.demo.aplugin.demo1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 用于演示对 [BroadcastReceiver] 的支持。
 *
 * @author Ysj
 * Create time: 2024/11/18
 */
class Demo1Receiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "Demo1Receiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        intent ?: return
        Log.i(TAG, "onReceive: ${intent.action}")
    }

}