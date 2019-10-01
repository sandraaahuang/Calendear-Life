package com.sandra.calendearlife

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Initialize ThreeTenABP library
        AndroidThreeTen.init(this)
    }
}