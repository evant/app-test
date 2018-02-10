package com.willowtreeapps.apptest.android.idlers

object Idlers {

    fun install() {
        try {
            Rx1Idlers.install()
        } catch (e: NoClassDefFoundError) {
            //ignore
        }
        try {
            Rx2Idlers.install()
        } catch (e: NoClassDefFoundError) {
            //ignore
        }
    }
}