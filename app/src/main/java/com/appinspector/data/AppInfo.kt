package com.appinspector.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    var launchActivity: String? = null,
    val discoveredBy: MutableSet<QueryMethod> = mutableSetOf()
) {
    /** Shorthand: is this app found by at least one Root method? */
    val foundByRoot: Boolean get() = discoveredBy.any { it.category == QueryMethod.Category.ROOT }
}
