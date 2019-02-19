package com.arikachmad.ocrektp

import android.app.Application
import com.google.firebase.FirebaseApp
import timber.log.Timber.DebugTree
import timber.log.Timber


class MyApp : Application() {


    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

    }
}