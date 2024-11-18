package com.ysj.demo.aplugin

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ysj.demo.aplugin.databinding.ActivityMainBinding
import com.ysj.lib.android.stable.plugin.StablePlugin
import java.io.File
import java.util.concurrent.CancellationException

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
        vb.btnInstallPlugins.setOnClickListener {
            lifecycleScope.launchSafety {
                StablePlugin.installPlugin("demo_plugin1", File(filesDir, "demo_plugin1.apk"))
//                StablePlugin.installPlugin("demo_plugin2", File(filesDir, "demo_plugin2.apk"))
//                StablePlugin.installPlugin("llm", File(filesDir, "llm.apk"))
            }.invokeOnCompletion {
                if (it == null || it is CancellationException) {
                    return@invokeOnCompletion
                }
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        vb.btnUninstallPlugins.setOnClickListener {
            lifecycleScope.launchSafety {
                StablePlugin.uninstallPlugin("demo_plugin1")
                StablePlugin.uninstallPlugin("demo_plugin2")
                StablePlugin.uninstallPlugin("llm")
            }
        }
        vb.btnToPlugin1.setOnClickListener {
            val intent = Intent()
                .setComponent(ComponentName(packageName, "com.ysj.demo.aplugin.demo1.Demo1MainActivity"))
            startActivity(intent)
        }
        vb.btnToPlugin2.setOnClickListener {
            val intent = Intent()
                .setComponent(ComponentName(packageName, "com.ysj.demo.aplugin.demo2.MainActivity"))
            startActivity(intent)
        }
        vb.btnToPlugin3.setOnClickListener {
            val intent = Intent()
                .setComponent(ComponentName(packageName, "com.czm.component.llm.demo.MainActivity"))
            startActivity(intent)
        }
    }

}