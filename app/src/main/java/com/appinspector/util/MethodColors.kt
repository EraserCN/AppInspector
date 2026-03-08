package com.appinspector.util

import android.graphics.Color
import com.appinspector.data.QueryMethod

object MethodColors {
    // Colors for each category — vivid & distinct
    private val categoryColors = mapOf(
        QueryMethod.Category.PACKAGE_MANAGER to 0xFF6200EE.toInt(), // deep purple
        QueryMethod.Category.INTENT_LAUNCHER  to 0xFF0288D1.toInt(), // blue
        QueryMethod.Category.INTENT_MEDIA     to 0xFF00897B.toInt(), // teal
        QueryMethod.Category.INTENT_SHARE     to 0xFF43A047.toInt(), // green
        QueryMethod.Category.INTENT_COMM      to 0xFF039BE5.toInt(), // light blue
        QueryMethod.Category.INTENT_BROWSER   to 0xFF7B1FA2.toInt(), // purple
        QueryMethod.Category.INTENT_SYSTEM    to 0xFFF57F17.toInt(), // amber
        QueryMethod.Category.INTENT_INSTALL   to 0xFFE64A19.toInt(), // deep orange
        QueryMethod.Category.ROOT             to 0xFFD32F2F.toInt()  // red
    )

    fun chipBgColorFor(method: QueryMethod): Int =
        categoryColors[method.category] ?: Color.GRAY

    fun chipTextColor(): Int = Color.WHITE
}
