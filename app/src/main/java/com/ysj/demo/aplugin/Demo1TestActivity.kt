package com.ysj.demo.aplugin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ysj.demo.aplugin.databinding.ActivityDemo1TestBinding
import com.ysj.demo.aplugin.demo1.api.Demo1Component
import com.ysj.demo.aplugin.demo1.api.Demo1ServiceData
import com.ysj.lib.android.stable.plugin.StablePlugin
import com.ysj.lib.bcu.modifier.component.di.api.ComponentInject
import java.io.File
import java.util.concurrent.CancellationException

/**
 * 用于测试 Demo1 插件。
 *
 * @author Ysj
 * Create time: 2024/12/4
 */
class Demo1TestActivity : AppCompatActivity() {

    companion object {
        private const val PLUGIN_NAME = "demo_plugin1"
    }

    @ComponentInject
    private lateinit var demo1Component: Demo1Component

    private val vb by lazy(LazyThreadSafetyMode.NONE) {
        ActivityDemo1TestBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)
        vb.btnInstallPlugin.setOnClickListener {
            lifecycleScope.launchSafety {
                StablePlugin.installReleasedPlugin(PLUGIN_NAME)
                    ?: StablePlugin.installPlugin(PLUGIN_NAME, File(MainApplication.pluginFileStorageDir, "demo_plugin1.apk"))
                Toast.makeText(this@Demo1TestActivity, "安装完成", Toast.LENGTH_SHORT).show()
            }.invokeOnCompletion {
                if (it == null || it is CancellationException) {
                    return@invokeOnCompletion
                }
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        vb.btnUninstallPlugin.setOnClickListener {
            lifecycleScope.launchSafety {
                StablePlugin.uninstallPlugin(PLUGIN_NAME)
                Toast.makeText(this@Demo1TestActivity, "卸载完成", Toast.LENGTH_SHORT).show()
            }.invokeOnCompletion {
                if (it == null || it is CancellationException) {
                    return@invokeOnCompletion
                }
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        vb.btnPluginVersion.setOnClickListener {
            if (!StablePlugin.checkPluginInstalled(PLUGIN_NAME)) {
                return@setOnClickListener
            }
            Toast.makeText(this, "version:${demo1Component.version()}", Toast.LENGTH_SHORT).show()
        }
        vb.btnStartActivity.setOnClickListener {
            if (!StablePlugin.checkPluginInstalled(PLUGIN_NAME)) {
                return@setOnClickListener
            }
            demo1Component.startMainActivity(this)
        }
        vb.btnCallService.setOnClickListener {
            if (!StablePlugin.checkPluginInstalled(PLUGIN_NAME)) {
                return@setOnClickListener
            }
            demo1Component.callService(this, Demo1ServiceData(
                title = "来自App",
                content = "我是来自Host的调用",
            ))
        }
    }


}