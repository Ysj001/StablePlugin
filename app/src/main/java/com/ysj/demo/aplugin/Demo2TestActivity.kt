package com.ysj.demo.aplugin

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
                StablePlugin.installPlugin("demo_plugin2", File(MainApplication.pluginFileStorageDir, "demo_plugin2.apk"))
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
            }.invokeOnCompletion {
                if (it == null || it is CancellationException) {
                    return@invokeOnCompletion
                }
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }


}