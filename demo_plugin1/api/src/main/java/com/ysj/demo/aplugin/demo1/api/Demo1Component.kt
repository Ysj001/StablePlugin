package com.ysj.demo.aplugin.demo1.api

import android.content.Context
import com.ysj.lib.bcu.modifier.component.di.api.Component
import java.io.File

/**
 * 定义 demo1 对外接口。
 *
 * @author Ysj
 * Create time: 2024/12/4
 */
@Component
interface Demo1Component {

    fun version(): String

    fun startMainActivity(context: Context)

    fun callService(context: Context, data: Demo1ServiceData)

}