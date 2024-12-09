package com.ysj.lib.android.stable.plugin

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.util.Log
import java.lang.reflect.Modifier

/**
 * 仅用于兼容 api<28 的场景。
 *
 * @author Ysj
 * Create time: 2024/12/9
 */
class InstrumentationCompat(
    private val hostClassLoader: ClassLoader,
) : Instrumentation() {

    companion object {
        private const val TAG = "InstrumentationCompat"
    }

    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
        log("newApplication.", Throwable())
        return StablePlugin.application
    }

    override fun callApplicationOnCreate(app: Application) {
        if (app != StablePlugin.application) {
            super.callApplicationOnCreate(app)
        }
    }

    override fun newActivity(cl: ClassLoader?, className: String?, intent: Intent?): Activity {
        if (cl != hostClassLoader) {
            val newActivity = super.newActivity(hostClassLoader, className, intent)
            log("newActivity.", Throwable())
            return newActivity
        }
        return super.newActivity(cl, className, intent)
    }

    fun init(org: Instrumentation) {
        val orgFields = org.javaClass.declaredFields
        for (index in orgFields.indices) {
            val field = orgFields[index]
            if (Modifier.isFinal(field.modifiers) && Modifier.isStatic(field.modifiers)) {
                continue
            }
            field.isAccessible = true
            field.set(this, field.get(org))
            Log.d(TAG, "replace field: ${field.name}")
        }
    }

    private fun log(msg: String, t: Throwable? = null) {
        if (StablePlugin.config.isDebugEnable) {
            Log.d(TAG, msg, t)
        }
    }

}