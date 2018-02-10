package com.willowtreeapps.apptest.android

import com.willowtreeapps.apptest.Config
import com.willowtreeapps.apptest.ProcessResult
import com.willowtreeapps.apptest.exec
import io.reactivex.Single
import java.io.File
import javax.annotation.CheckReturnValue

class Adb(private val config: Config) {

    @CheckReturnValue
    fun shell(cmd: String): Single<ProcessResult> = this("shell", cmd)

    @CheckReturnValue
    @JvmName("exec")
    operator fun invoke(vararg cmd: String): Single<ProcessResult> = exec(adb, *cmd)

    private val adb: String
        get() = config.androidSdkDir?.let { File(it, "platform-tools/adb").path } ?: "adb"
}