package com.ysj.demo.aplugin.demo1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.ysj.demo.aplugin.demo1.databinding.ActivityTest1Binding

/**
 *
 *
 * @author Ysj
 * Create time: 2024/9/19
 */
class Activity1 : AppCompatActivity() {

    companion object {
        private const val TAG = "Activity1"
    }

    private val vb by lazy(LazyThreadSafetyMode.NONE) {
        ActivityTest1Binding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)
        vb.btnClose.setOnClickListener {
            finish()
        }
        vb.btnMore.setOnClickListener {
            vb.root.openDrawer(GravityCompat.END)
        }
        vb.btnInfo.setOnClickListener {
            recreate()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(TAG, "onNewIntent: $intent")
    }

}