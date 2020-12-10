package com.example.common.tools

import java.text.SimpleDateFormat
import java.util.*

const val YY_MM_DD = "yyyy-MM-dd"
const val MMM_DD_YYYY_H_M_S = "yyyy-MM-dd HH:mm:ss"

/**
 * 格式化时间 根据时间戳
 */
fun formatDate(timeMillis: Long, pattern: String): String? {
    val dateFormat = SimpleDateFormat(YY_MM_DD, Locale.CHINA)
    dateFormat.applyPattern(pattern)
    return dateFormat.format(Date(handleDate(timeMillis)))
}

/**
 * 兼容时间戳
 */
fun handleDate(date: Long): Long {
    return if (date.toString().length > 10) date else date * 1000
}