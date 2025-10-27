package com.jinjinjara.pola

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PolaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

    }
}