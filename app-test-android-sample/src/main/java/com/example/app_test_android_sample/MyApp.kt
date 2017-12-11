package com.example.app_test_android_sample

import android.app.Application

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
//        AppTest.onInstrumented {
//            RxJavaPlugins.setInitIoSchedulerHandler(
//                    Rx2Idler.create("RxJava 2.x IO Scheduler"))
//        }
    }
}

