package com.willowtreeapps.apptest.android.idlers

import com.squareup.rx2.idler.Rx2Idler
import io.reactivex.plugins.RxJavaPlugins

object Rx2Idlers {
    fun install() {
        RxJavaPlugins.setInitComputationSchedulerHandler(
            Rx2Idler.create("RxJava 2.x Computation Scheduler")
        )
        RxJavaPlugins.setInitIoSchedulerHandler(
            Rx2Idler.create("RxJava 2.x Io Scheduler")
        )
        RxJavaPlugins.setInitNewThreadSchedulerHandler(
            Rx2Idler.create("RxJava 2.x New Thread Scheduler")
        )
    }
}
