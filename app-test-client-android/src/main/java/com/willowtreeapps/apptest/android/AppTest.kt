package com.willowtreeapps.apptest.android

import android.support.annotation.MainThread

object AppTest {

    @get:MainThread
    var instrumented: Boolean = false
        private set
    private var listener: Runnable? = null

    fun onInstrumented(f: () -> Unit) {
        onInstrumented(Runnable(f))
    }

    @JvmStatic
    @MainThread
    fun onInstrumented(f: Runnable) {
        if (instrumented) {
            f.run()
        } else {
            listener = f
        }
    }

    @JvmStatic
    @MainThread
    internal fun instrumented() {
        instrumented = true
        listener?.run()
        listener = null
    }
}