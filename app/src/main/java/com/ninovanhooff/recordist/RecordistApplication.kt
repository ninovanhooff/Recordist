package com.ninovanhooff.recordist

import android.app.Application
import com.dimowner.phonograph.Phonograph
import com.ninovanhooff.recordist.presentation.MainActivity
import timber.log.Timber

class RecordistApplication: Application() {

    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        super.onCreate()

        injector = Injector(applicationContext)
        Phonograph.initialize(
                applicationContext,
                MainActivity::class.java,
                injector.provideAppRecorder()
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        Timber.v("onTerminate")
        injector.closeTasks()
    }

    companion object {
        lateinit var injector: Injector
    }
}
