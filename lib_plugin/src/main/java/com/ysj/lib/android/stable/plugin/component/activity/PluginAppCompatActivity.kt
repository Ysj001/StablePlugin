package com.ysj.lib.android.stable.plugin.component.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity

/**
 * 插件 [AppCompatActivity]。
 *
 * @author Ysj
 * Create time: 2024/9/23
 */
abstract class PluginAppCompatActivity : AppCompatActivity() {

    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    override fun getLastNonConfigurationInstance(): Any? {
        // plugin 更新后 classloader 会变更，这里需要判断
        return super.getLastNonConfigurationInstance().takeIf {
            it is NonConfigurationInstances
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.classLoader = classLoader
        intent.setExtrasClassLoader(classLoader)
        applyAppCompatFactoryCompat()
        super.onCreate(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.setExtrasClassLoader(classLoader)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun applyAppCompatFactoryCompat() {
        /*
            这里要设置 Factory2 是因为 AppCompatDelegateImpl 中 installViewFactory 会先判断
            当前 LayoutInflater 的 Factory 有没有设置，如果设置了就不会在设置自己的了
            会导致 xml 中 xxxCompat 相关属性用不了
            （目前在 appcompat:1.7.0 中发现该问题）
         */
        val delegate = this.delegate
        if (delegate is LayoutInflater.Factory2) {
            layoutInflater.factory2 = delegate
        }
    }

}