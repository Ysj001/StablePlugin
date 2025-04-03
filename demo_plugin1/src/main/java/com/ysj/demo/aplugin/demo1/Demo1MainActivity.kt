package com.ysj.demo.aplugin.demo1

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ysj.demo.aplugin.demo1.Demo1ComponentImpl.pluginFileStorageDir
import com.ysj.demo.aplugin.demo1.api.Demo1ServiceData
import com.ysj.demo.aplugin.demo1.databinding.ActivityMainBinding
import com.ysj.lib.android.stable.plugin.StablePlugin
import kotlinx.coroutines.CancellationException
import java.io.File


/**
 * Demo1 主页。
 *
 * @author Ysj
 * Create time: 2024/9/14
 */
class Demo1MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "Demo1MainActivity"
    }

    private val vb by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)
        vb.btnReInit.setOnClickListener {
            // 演示在插件内部热更新插件
            lifecycleScope.launchSafety {
                StablePlugin.uninstallPlugin("demo_plugin1")
                StablePlugin.installPlugin("demo_plugin1", File(pluginFileStorageDir, "demo_plugin1.apk"))
                recreate()
                Toast.makeText(this@Demo1MainActivity, "更新成功", Toast.LENGTH_SHORT).show()
            }.invokeOnCompletion {
                if (it == null || it is CancellationException) {
                    return@invokeOnCompletion
                }
                Toast.makeText(this, "更新失败. $it", Toast.LENGTH_SHORT).show()
            }
        }
        vb.btnToActivity1.setOnClickListener {
            startActivity(Intent(this, Activity1::class.java))
        }
        vb.btnTestDemo1Provider.setOnClickListener {
            val uri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority("${packageName}.demo1.provider")
                .appendQueryParameter(Demo1Provider.URI_QUERY_KEY_QUESTION, "what's your name?")
                .build()
            contentResolver.query(uri, null, null, null, null)?.use {
                it.moveToFirst()
                val answerIndex = it.getColumnIndex(Demo1Provider.COLUMNS_ANSWER)
                val answer = it.getString(answerIndex)
                Toast.makeText(this, answer, Toast.LENGTH_SHORT).show()
            }
        }
        vb.btnTestDemo1Service.setOnClickListener {
            startService(Demo1Service.createNotifyIntent(this, Demo1ServiceData(
                title = "来自Demo1",
                content = "哈哈哈哈"
            )))
        }
        vb.btnTestDemo1Receiver.setOnClickListener {
            val intent = Intent(this, Demo1Receiver::class.java)
                .setAction("demo1.receiver.action1")
            sendBroadcast(intent)
            Toast.makeText(this, "请看 logcat 输出", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, Demo1Service::class.java))
    }

}