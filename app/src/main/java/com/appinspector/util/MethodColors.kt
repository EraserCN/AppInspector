package com.appinspector.util

import android.graphics.Color
import com.appinspector.data.QueryMethod

object MethodColors {
    // Subtle, modern palette for ColorOS/OxygenOS style
    private val categoryColors = mapOf(
        QueryMethod.Category.PACKAGE_MANAGER to 0xFFE1F5FE.toInt(), // Light Blue
        QueryMethod.Category.INTENT_LAUNCHER  to 0xFFE8F5E9.toInt(), // Light Green
        QueryMethod.Category.INTENT_MEDIA     to 0xFFFFF3E0.toInt(), // Light Orange
        QueryMethod.Category.INTENT_SHARE     to 0xFFF3E5F5.toInt(), // Light Purple
        QueryMethod.Category.INTENT_COMM      to 0xFFE0F2F1.toInt(), // Light Teal
        QueryMethod.Category.INTENT_BROWSER   to 0xFFEFEBE9.toInt(), // Light Brown
        QueryMethod.Category.INTENT_SYSTEM    to 0xFFF1F8E9.toInt(), // Light Lime
        QueryMethod.Category.INTENT_INSTALL   to 0xFFFFFDE7.toInt(), // Light Yellow
        QueryMethod.Category.ROOT             to 0xFFFFEBEE.toInt()  // Light Red
    )

    private val textColors = mapOf(
        QueryMethod.Category.PACKAGE_MANAGER to 0xFF0288D1.toInt(),
        QueryMethod.Category.INTENT_LAUNCHER  to 0xFF388E3C.toInt(),
        QueryMethod.Category.INTENT_MEDIA     to 0xFFF57C00.toInt(),
        QueryMethod.Category.INTENT_SHARE     to 0xFF7B1FA2.toInt(),
        QueryMethod.Category.INTENT_COMM      to 0xFF00796B.toInt(),
        QueryMethod.Category.INTENT_BROWSER   to 0xFF5D4037.toInt(),
        QueryMethod.Category.INTENT_SYSTEM    to 0xFF689F38.toInt(),
        QueryMethod.Category.INTENT_INSTALL   to 0xFFFBC02D.toInt(),
        QueryMethod.Category.ROOT             to 0xFFD32F2F.toInt()
    )

    fun chipBgColorFor(method: QueryMethod): Int =
        categoryColors[method.category] ?: 0xFFF5F5F5.toInt()

    fun chipTextColorFor(method: QueryMethod): Int =
        textColors[method.category] ?: 0xFF757575.toInt()

    fun chipTextColor(): Int = Color.BLACK
}
