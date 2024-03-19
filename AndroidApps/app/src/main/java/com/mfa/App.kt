package com.mfa

import android.app.Application

class App : Application() {
    private lateinit var singleton: App

    fun getInstance(): App {
        return singleton
    }

    override fun onCreate() {
        super.onCreate()
        singleton = this
    }
}