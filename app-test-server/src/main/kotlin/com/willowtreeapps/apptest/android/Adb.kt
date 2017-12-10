package com.willowtreeapps.apptest.android

import com.willowtreeapps.apptest.Config
import com.willowtreeapps.apptest.ProcessResult
import java.io.File

class Adb(private val config: Config) {

    fun shell(cmd: String): ProcessResult
            = this("shell", cmd)

    @JvmName("exec")
    operator fun invoke(vararg cmd: String): ProcessResult
            = com.willowtreeapps.apptest.exec(adb, *cmd)

    private val adb: String
        get() = config.androidSdkDir?.let { File(it, "platform-tools/adb").path } ?: "adb"
}