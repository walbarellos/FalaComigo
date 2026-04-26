package br.com.falacomigo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FalaComigoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}