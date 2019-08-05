package com.intmainreturn00.bookar.platform

import android.app.Application
import android.graphics.Typeface
import com.intmainreturn00.bookar.BuildConfig
import com.intmainreturn00.grapi.grapi
import es.dmoral.toasty.Toasty

class App : Application() {

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        grapi.init(this,
            BuildConfig.goodreadsKey,
            BuildConfig.goodreadsSecret,
            BuildConfig.goodreadsCallback
        )

        Toasty.Config.getInstance()
            .setToastTypeface(Typeface.createFromAsset(assets, "fonts/Podkova-Regular.ttf"))
            .setTextSize(18)
            .allowQueue(true)
            .apply()
    }
}