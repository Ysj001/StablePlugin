package com.ysj.demo.aplugin

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ysj.demo.aplugin.databinding.ActivityDemo2TestBinding
import com.ysj.lib.android.stable.plugin.StablePlugin
import java.io.File
import java.util.concurrent.CancellationException

/**
 * 用于测试 Demo1 插件。
 *
 * @author Ysj
 * Create time: 2024/12/4
 */
class Demo2TestActivity : AppCompatActivity() {

    private val vb by lazy(LazyThreadSafetyMode.NONE) {
        ActivityDemo2TestBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)
        vb.btnInstallPlugin.setOnClickListener {
            lifecycleScope.launchSafety {
                StablePlugin.installReleasedPlugin("demo_plugin2")
                    ?: StablePlugin.installPlugin("demo_plugin2", File(MainApplication.pluginFileStorageDir, "demo_plugin2.apk"))
                Toast.makeText(this@Demo2TestActivity, "安装完成", Toast.LENGTH_SHORT).show()
            }.invokeOnCompletion {
                if (it == null || it is CancellationException) {
                    return@invokeOnCompletion
                }
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        vb.btnUninstallPlugin.setOnClickListener {
            lifecycleScope.launchSafety {
                StablePlugin.uninstallPlugin("demo_plugin2")
                Toast.makeText(this@Demo2TestActivity, "卸载完成", Toast.LENGTH_SHORT).show()
            }.invokeOnCompletion {
                if (it == null || it is CancellationException) {
                    return@invokeOnCompletion
                }
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        vb.btnStartActivity.setOnClickListener {
            startActivity(Intent().setComponent(ComponentName(packageName, "com.ysj.demo.aplugin.demo2.MainActivity")))
        }
    }


}