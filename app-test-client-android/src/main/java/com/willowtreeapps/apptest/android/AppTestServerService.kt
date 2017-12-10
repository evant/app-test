package com.willowtreeapps.apptest.android

import android.app.*
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.support.test.InstrumentationRegistry
import android.support.test.internal.runner.lifecycle.ActivityLifecycleMonitorImpl
import android.support.test.internal.runner.lifecycle.ApplicationLifecycleMonitorImpl
import android.support.test.runner.MonitoringInstrumentation
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.ApplicationLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.ApplicationStage
import io.grpc.*

class AppTestServerService : Service() {

    private var server: Server? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification())
        InstrumentationHelper.setup(this)
        AppTest.instrumented()
    }

    private fun buildNotification(): Notification {
        return Notification.Builder(this).apply {
            if (Build.VERSION.SDK_INT >= 26) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(NotificationChannel("app-test", "App Test", NotificationManager.IMPORTANCE_NONE))
                setChannelId("app-test")
            }
        }.notification
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val finderService = com.willowtreeapps.apptest.android.Service()
        if (server == null) {
            server = ServerBuilder.forPort(2734)
                    .addService(finderService)
                    .build()
                    .start()
        }

        if (intent != null) {
            when (intent.getStringExtra("cmd")) {
                "start" -> {
                    startActivity(packageManager.getLaunchIntentForPackage(packageName)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
                }
                "stop" -> {
                    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    if (Build.VERSION.SDK_INT >= 21) {
                        for (appTask in activityManager.appTasks) {
                            appTask.finishAndRemoveTask()
                        }
                    } else {
                        //TODO: Is this even possible?
                    }

                    if (Build.VERSION.SDK_INT >= 19) {
//                        activityManager.clearApplicationUserData()
                    } else {
                        //TODO
                    }
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.shutdown()
    }

    override fun onBind(intent: Intent): IBinder? = null
}