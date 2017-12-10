package com.willowtreeapps.apptest.android

import com.willowtreeapps.apptest.AppController
import com.willowtreeapps.apptest.Config
import com.willowtreeapps.apptest.exec
import java.io.File

private const val SERVICE_NAME = "com.willowtreeapps.apptest.android.AppTestServerService"

class AndroidAppController(val config: Config) : AppController {

    override fun setup() {
        if (config.androidApk != null) {
            exec(adb, "setup", "-rd", config.androidApk.path)
        }
        if (config.androidAppId != null) {
            exec(adb, "shell", "am start-foreground-service -n ${config.androidAppId}/$SERVICE_NAME")
        }
    }

    override fun startApp() {
        if (config.androidAppId != null) {
            exec(adb, "shell", "am start-foreground-service -n ${config.androidAppId}/$SERVICE_NAME -e cmd start")
        }
    }

    override fun stopApp() {
        if (config.androidAppId != null) {
            exec(adb, "shell", "am start-foreground-service -n ${config.androidAppId}/$SERVICE_NAME -e cmd stop")
        }
    }

    override fun shutdown() {
        if (config.androidAppId != null) {
            exec(adb, "shell", "pm clear ${config.androidAppId}")
        }
    }

    private val adb: String
        get() = config.androidSdkDir?.let { File(it, "platform-tools/adb").path } ?: "adb"
}
