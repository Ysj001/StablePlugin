package com.ysj.demo.aplugin.demo1

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.startup.InitializationProvider
import com.ysj.demo.aplugin.demo1.api.Demo1ServiceData
import com.ysj.demo.aplugin.demo1.databinding.ActivityMainBinding


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
        Log.i(TAG, "本条 log 验证 getApplication 会优先获取本插件的 Application. $application")
        Log.i(TAG, "本条 log 验证 packageManager 会优先获取本插件的信息. ${
            application.packageManager.getProviderInfo(
                ComponentName(application, InitializationProvider::class.java),
                PackageManager.GET_META_DATA,
            ).metaData?.keySet()?.toTypedArray()?.contentToString()
        }")
        // 验证在 activity 中能正确获取主包和宿主包的 assets 中的资源
        assets.open("aaa/ic_launcher.png").use {
            Log.i(TAG, "read host assets: ${it.available()}")
        }
        assets.open("bbb/ic_launcher.png").use {
            Log.i(TAG, "read my assets: ${it.available()}")
        }
        vb.btnToActivity1.setOnClickListener {
            val aaa = ActivityOptionsCompat.makeCustomAnimation(
                this,
                // 由于动画是在 ams 中加载的，无法加载插件中的动画资源
                // 插件中设置动画得使用主包中的或安卓包中的，可以使用 public.xml 方式固定资源 id
                android.R.anim.fade_in,
                android.R.anim.fade_out,
//                0x7f010021,
//                0x7f010022,
            )
            startActivity(Intent(this, Activity1::class.java), aaa.toBundle())
        }
        vb.btnToExceptionTest.setOnClickListener {
            startActivity(Intent(this, ExceptionTestActivity::class.java))
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