package com.willowtreeapps.apptest.android

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.test.InstrumentationRegistry
import android.support.test.internal.runner.lifecycle.ActivityLifecycleMonitorImpl
import android.support.test.internal.runner.lifecycle.ApplicationLifecycleMonitorImpl
import android.support.test.runner.MonitoringInstrumentation
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.ApplicationLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.ApplicationStage
import android.support.test.runner.lifecycle.Stage

internal object InstrumentationHelper {

    fun setup(context: Context) {
        val app = context.applicationContext as Application

        val appImpl = ApplicationLifecycleMonitorImpl()
        appImpl.signalLifecycleChange(app, ApplicationStage.CREATED)
        ApplicationLifecycleMonitorRegistry.registerInstance(appImpl)
        val activityImpl = ActivityLifecycleMonitorImpl()
        app.registerActivityLifecycleCallbacks(ActivityLifecycleListener(activityImpl))
        ActivityLifecycleMonitorRegistry.registerInstance(activityImpl)

        InstrumentationRegistry.registerInstance(
                object: MonitoringInstrumentation() {
                    override fun onCreate(arguments: Bundle?) {
                        super.onCreate(arguments)
                        startActivitySync(Intent(Intent.ACTION_MAIN)
                                .addCategory(Intent.CATEGORY_LAUNCHER))
                    }

                    override fun getContext(): Context {
                        return context
                    }

                    override fun getTargetContext(): Context {
                        return context
                    }
                },
                Bundle()
        )
    }

    private class ActivityLifecycleListener(private val impl: ActivityLifecycleMonitorImpl) : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
            impl.signalLifecycleChange(Stage.CREATED, activity)
        }

        override fun onActivityStarted(activity: Activity) {
            impl.signalLifecycleChange(Stage.STARTED, activity)
        }

        override fun onActivityResumed(activity: Activity) {
            impl.signalLifecycleChange(Stage.RESUMED, activity)
        }

        override fun onActivityPaused(activity: Activity) {
            impl.signalLifecycleChange(Stage.PAUSED, activity)
        }

        override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle?) {
        }

        override fun onActivityStopped(activity: Activity) {
            impl.signalLifecycleChange(Stage.STOPPED, activity)
        }

        override fun onActivityDestroyed(activity: Activity) {
            impl.signalLifecycleChange(Stage.DESTROYED, activity)
        }
    }
}