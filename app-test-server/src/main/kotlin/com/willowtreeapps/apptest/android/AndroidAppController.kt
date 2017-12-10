package com.willowtreeapps.apptest.android

import com.willowtreeapps.apptest.AppController
import com.willowtreeapps.apptest.Config

private const val SERVICE_NAME = "com.willowtreeapps.apptest.android.AppTestServerService"

class AndroidAppController(val config: Config) : AppController {
    private val adb = Adb(config)

    override fun setup() {
        if (config.androidApk != null) {
            adb("install", "-rd", config.androidApk.path)
        }
        if (config.androidAppId != null) {
           adb.shell("am start-foreground-service -n ${config.androidAppId}/$SERVICE_NAME")
        }
    }

    override fun startApp() {
        if (config.androidAppId != null) {
            adb.shell("am start-foreground-service -n ${config.androidAppId}/$SERVICE_NAME -e cmd start")
        }
    }

    override fun stopApp() {
        if (config.androidAppId != null) {
            adb.shell("am start-foreground-service -n ${config.androidAppId}/$SERVICE_NAME -e cmd stop")
        }
    }

    override fun shutdown() {
        if (config.androidAppId != null) {
            adb.shell("pm clear ${config.androidAppId}")
        }
    }
}
