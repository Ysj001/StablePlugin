package com.ysj.demo.aplugin

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/*
 * 一些通用工具。
 *
 * @author Ysj
 * Create time: 2024/9/26
 */


/**
 * 安全的启动协程。
 */
fun CoroutineScope.launchSafety(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
): Job = launch(ExceptionHandler + context, start, block)

object ExceptionHandler : CoroutineExceptionHandler {

    private const val TAG = "ExceptionHandler"

    override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        if (exception is CancellationException) {
            return
        }
        Log.w(TAG, "coroutine exception.", exception)
    }
}