package com.ysj.demo.aplugin.demo1.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 用于操作 Demo1Service 的数据 。
 *
 * @author Ysj
 * Create time: 2024/12/5
 */
@Parcelize
data class Demo1ServiceData(
    val title: String,
    val content: String,
) : Parcelable