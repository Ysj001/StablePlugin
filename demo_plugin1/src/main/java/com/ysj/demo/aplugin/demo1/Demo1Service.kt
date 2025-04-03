package com.ysj.demo.aplugin.demo1

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.IntentCompat
import com.ysj.demo.aplugin.demo1.api.Demo1ServiceData

/**
 * 用于演示对 [Service] 的支持。
 *
 * @author Ysj
 * Create time: 2024/11/18
 */
class Demo1Service : Service() {

    companion object {
        private const val TAG = "Demo1Service"

        private val NOTIFY_ID = "Demo1Service".hashCode()
        private const val CHANNEL_ID = "Demo1Service"
        private const val CHANNEL_NAME = "Demo1Service"

        private const val KEY_DATA = "KEY_DATA"

        fun createNotifyIntent(context: Context, data: Demo1ServiceData) = Intent().apply {
            setClass(context, Demo1Service::class.java)
            putExtra(KEY_DATA, data)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate.")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand.")
        val data = IntentCompat
            .getParcelableExtra(intent, KEY_DATA, Demo1ServiceData::class.java)
            ?: return super.onStartCommand(intent, flags, startId)
        notify(data.title, data.content)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i(TAG, "onBind.")
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy.")
    }

    private fun notify(title: String, content: String) {
        val nm = NotificationManagerCompat.from(this)
        val channel = NotificationChannelCompat
            .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(CHANNEL_NAME)
            .setSound(Uri.EMPTY, null)
            .build()
        nm.createNotificationChannel(channel)
        val notificationView = RemoteViews(packageName, R.layout.notification_common)
        notificationView.setTextViewText(R.id.tvTitle, title)
        notificationView.setTextViewText(R.id.tvContent, content)
        val notification = NotificationCompat
            .Builder(this, channel.id)
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationView)
            .build()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        nm.notify(NOTIFY_ID, notification)
    }

}