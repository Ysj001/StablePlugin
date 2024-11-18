package com.ysj.demo.aplugin.demo1

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
                Log.i(TAG, "query result: $answer")
            }
        }
        vb.btnTestDemo1Service.setOnClickListener {
            startService(Intent(this, Demo1Service::class.java))
        }
        vb.btnTestDemo1Receiver.setOnClickListener {
            val intent = Intent(this, Demo1Receiver::class.java)
                .setAction("demo1.receiver.action1")
            sendBroadcast(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, Demo1Service::class.java))
    }

}