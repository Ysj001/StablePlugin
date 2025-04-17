package com.ysj.lib.android.stable.plugin.component.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat

/**
 * 当插件执行过程中出现异常时会启动该页面，可以当做兜底处理。
 *
 * @author Ysj
 * Create time: 2025/4/17
 */
abstract class PluginExceptionHandlerActivity : AppCompatActivity() {

    companion object {

        private const val KEY_PREFIX = "stable_plugin.exception"

        private const val KEY_FROM = "${KEY_PREFIX}.from"
        private const val KEY_EXCEPTION = "${KEY_PREFIX}.exception"

        const val FROM_ACTIVITY_NEW = 2000
        const val FROM_ACTIVITY_ON_CREATE = 2001

        internal fun applyReason(intent: Intent, from: Int, exception: Exception) {
            intent.putExtra(KEY_FROM, from)
            intent.putExtra(KEY_EXCEPTION, exception)
        }
    }

    /**
     * 异常来自哪里。
     */
    protected val from get() = intent.getIntExtra(KEY_FROM, 0)

    /**
     * 具体异常。
     */
    protected val exception
        get() = IntentCompat.getSerializableExtra(
            intent,
            KEY_EXCEPTION,
            Exception::class.java
        )

}