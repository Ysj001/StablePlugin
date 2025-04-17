package com.ysj.demo.aplugin.demo1

import androidx.appcompat.app.AppCompatActivity

/**
 * 用于测试异常处理机制。
 *
 * @author Ysj
 * Create time: 2025/4/17
 */
class ExceptionTestActivity : AppCompatActivity() {

    init {
        // 测试构造时异常
        throw RuntimeException("test init exception.")
    }

}