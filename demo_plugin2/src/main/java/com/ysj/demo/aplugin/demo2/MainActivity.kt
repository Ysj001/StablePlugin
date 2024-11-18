package com.ysj.demo.aplugin.demo2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ysj.demo.aplugin.demo2.databinding.ActivityMainBinding

/**
 * Demo2 主页。
 *
 * @author Ysj
 * Create time: 2024/9/14
 */
class MainActivity : AppCompatActivity() {

    private val vb by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)
    }

}