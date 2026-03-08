package com.appinspector

import android.app.Application
import com.google.android.material.color.DynamicColors

class AppInspectorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply Material You / Monet dynamic colors globally
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
