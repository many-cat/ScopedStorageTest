package com.example.scopedstoragetest

import android.app.Application
import android.content.ContextWrapper

private lateinit var INSTALL: Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        INSTALL = this
    }
}

object AppContext : ContextWrapper(INSTALL)