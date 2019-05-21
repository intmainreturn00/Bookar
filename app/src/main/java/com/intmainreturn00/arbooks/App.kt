package com.intmainreturn00.arbooks

import android.app.Application
import com.intmainreturn00.grapi.grapi

class App : Application() {

    companion object {
        lateinit var instance: App
            private set

        lateinit var books: MutableList<ARBook>
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        grapi.init(this, BuildConfig.goodreadsKey, BuildConfig.goodreadsSecret, BuildConfig.goodreadsCallback)
    }
}