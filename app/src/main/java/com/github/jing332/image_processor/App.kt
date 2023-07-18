package com.github.jing332.image_processor

import android.app.Application

val app by lazy { App.instance }

class App : Application() {
    companion object {
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
    }

}