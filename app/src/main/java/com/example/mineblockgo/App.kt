package com.example.mineblockgo

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseManager.initialize(this)
    }
}