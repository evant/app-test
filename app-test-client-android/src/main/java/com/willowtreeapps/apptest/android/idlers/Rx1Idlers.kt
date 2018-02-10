package com.willowtreeapps.apptest.android.idlers

import com.squareup.rx.idler.RxIdler
import rx.plugins.RxJavaPlugins


object Rx1Idlers {

    fun install() {
        RxJavaPlugins.getInstance().registerSchedulersHook(RxIdler.hooks())
    }
}