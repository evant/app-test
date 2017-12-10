package com.example.app_test_android_sample

import android.app.Application
import com.squareup.rx2.idler.Rx2Idler
import com.willowtreeapps.apptest.android.AppTest
import io.reactivex.plugins.RxJavaPlugins

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppTest.onInstrumented {
            RxJavaPlugins.setInitIoSchedulerHandler(
                    Rx2Idler.create("RxJava 2.x IO Scheduler"))
        }
    }
}

