package com.ysj.demo.aplugin.demo1

import android.content.Context
import android.content.Intent
import com.ysj.demo.aplugin.demo1.api.Demo1Component
import com.ysj.demo.aplugin.demo1.api.Demo1ServiceData
import com.ysj.lib.bcu.modifier.component.di.api.ComponentImpl
import java.io.File

/**
 * [Demo1Component] 实现。
 *
 * @author Ysj
 * Create time: 2024/12/4
 */
@ComponentImpl
object Demo1ComponentImpl : Demo1Component {

    override fun version(): String {
        return BuildConfig.VERSION_NAME
    }

    override fun startMainActivity(context: Context) {
        context.startActivity(Intent(context, Demo1MainActivity::class.java))
    }

    override fun callService(context: Context, data: Demo1ServiceData) {
        context.startService(Demo1Service.createNotifyIntent(context, data))
    }

}