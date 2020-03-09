package com.ninovanhooff.recordist

import android.app.Application
import com.ninovanhooff.phonograph.Phonograph
import com.ninovanhooff.phonograph.util.AndroidUtils
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
        Phonograph.setScreenWidthDp(AndroidUtils.pxToDp(AndroidUtils.getScreenWidth(applicationContext)))
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
