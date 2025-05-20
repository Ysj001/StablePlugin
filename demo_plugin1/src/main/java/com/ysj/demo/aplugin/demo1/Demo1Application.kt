package com.ysj.demo.aplugin.demo1

import android.app.Application
import android.content.Context
import android.util.Log
import com.ysj.demo.aplugin.demo1.Demo1MainActivity.Companion

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
        // 验证在 application 中能正确获取主包和宿主包的 assets 中的资源
        assets.open("aaa/ic_launcher.png").use {
            Log.i(TAG, "read host assets: ${it.available()}")
        }
        assets.open("bbb/ic_launcher.png").use {
            Log.i(TAG, "read my assets: ${it.available()}")
        }
    }

}