package com.ysj.demo.aplugin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ysj.demo.aplugin.databinding.ActivityMainBinding
import java.io.File

/**
 * Demo 主页。
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
        vb.editPluginStorageDir.setText(MainApplication.pluginFileStorageDir.absolutePath)
        vb.btnPluginStorageDirConfirm.setOnClickListener {
            MainApplication.pluginFileStorageDir = File(vb.editPluginStorageDir.text.toString())
            Toast.makeText(this, "已修改", Toast.LENGTH_SHORT).show()
        }
        vb.btnTestPlugin1.setOnClickListener {
            startActivity(Intent(this, Demo1TestActivity::class.java))
        }
        vb.btnTestPlugin2.setOnClickListener {
            startActivity(Intent(this, Demo2TestActivity::class.java))
        }
    }

}