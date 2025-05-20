package com.ysj.demo.aplugin

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.setPadding
import com.ysj.lib.android.stable.plugin.component.activity.PluginExceptionHandlerActivity
import kotlin.math.roundToInt

/**
 * 演示当插件处理过程异常时的处理。
 *
 * @author Ysj
 * Create time: 2025/4/17
 */
class PluginExceptionHandlerImplActivity : PluginExceptionHandlerActivity() {

    private val Number.dp get() = resources.displayMetrics.density * toFloat()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ScrollView(this).also { scrollView ->
            scrollView.addView(LinearLayout(this).also { linearLayout ->
                linearLayout.addView(AppCompatTextView(this).also { textView ->
                    textView.setPadding(16.dp.roundToInt())
                    textView.text = "from=$from\nexception=\n${Log.getStackTraceString(exception)}"
                })
            })
        })
    }

}