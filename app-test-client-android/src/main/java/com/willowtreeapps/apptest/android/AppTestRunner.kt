package com.willowtreeapps.apptest.android

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.support.test.internal.runner.lifecycle.ActivityLifecycleMonitorImpl
import android.support.test.internal.runner.lifecycle.ApplicationLifecycleMonitorImpl
import android.support.test.runner.MonitoringInstrumentation
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.ApplicationLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.ApplicationStage
import android.support.test.runner.lifecycle.Stage
import com.willowtreeapps.apptest.proto.LifecycleGrpc
import com.willowtreeapps.apptest.proto.Rpc
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver

class AppTestRunner: MonitoringInstrumentation() {

    private var server: Server? = null

    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        start()
    }

    override fun onStart() {
        super.onStart()

        val app = targetContext.applicationContext as Application
        val appImpl = ApplicationLifecycleMonitorImpl()
        appImpl.signalLifecycleChange(app, ApplicationStage.CREATED)
        ApplicationLifecycleMonitorRegistry.registerInstance(appImpl)
        val activityImpl = ActivityLifecycleMonitorImpl()
        app.registerActivityLifecycleCallbacks(ActivityLifecycleListener(activityImpl))
        ActivityLifecycleMonitorRegistry.registerInstance(activityImpl)

        val finderService = Service()
        server = ServerBuilder.forPort(2734)
                .addService(finderService)
                .addService(LifecycleService())
                .build()
                .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.shutdown()
    }

    internal inner class LifecycleService : LifecycleGrpc.LifecycleImplBase() {
        private val finisher = ActivityFinisher()

        override fun setup(request: Rpc.SetupRequest, responseObserver: StreamObserver<Rpc.SetupResponse>) {
            responseObserver.onNext(Rpc.SetupResponse.getDefaultInstance())
            responseObserver.onCompleted()
        }

        override fun startApp(request: Rpc.StartRequest, responseObserver: StreamObserver<Rpc.StartResponse>) {
            startActivitySync(targetContext.packageManager.getLaunchIntentForPackage(targetContext.packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
            responseObserver.onNext(Rpc.StartResponse.getDefaultInstance())
            responseObserver.onCompleted()
        }

        override fun stopApp(request: Rpc.StopRequest, responseObserver: StreamObserver<Rpc.StopResponse>) {
            finisher.run()
            waitForActivitiesToComplete()
            responseObserver.onNext(Rpc.StopResponse.getDefaultInstance())
            responseObserver.onCompleted()
        }

        override fun shutdown(request: Rpc.ShutodownRequest, responseObserver: StreamObserver<Rpc.ShutdownResponse>) {
            responseObserver.onNext(Rpc.ShutdownResponse.getDefaultInstance())
            responseObserver.onCompleted()
            finish(Activity.RESULT_OK, null)
        }
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
