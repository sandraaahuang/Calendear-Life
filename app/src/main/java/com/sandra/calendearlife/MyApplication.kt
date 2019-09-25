package com.sandra.calendearlife

import android.app.Application
import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlin.properties.Delegates

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