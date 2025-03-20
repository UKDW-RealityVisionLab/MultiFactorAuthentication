package com.mfa

import android.app.Application
import com.google.firebase.FirebaseApp

class App : Application() {
    private lateinit var singleton: App

    fun getInstance(): App {
        return singleton
    }

    override fun onCreate() {
        super.onCreate()
        singleton = this
        FirebaseApp.initializeApp(this)

    }
}