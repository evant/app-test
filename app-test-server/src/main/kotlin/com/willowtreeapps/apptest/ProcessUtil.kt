package com.willowtreeapps.apptest

import io.reactivex.Single
import javax.annotation.CheckReturnValue

@CheckReturnValue
internal fun exec(vararg cmd: String): Single<ProcessResult> {
    return Single.fromCallable {
        println(cmd.joinToString(" "))
        val process = ProcessBuilder()
            .command(*cmd)
            .inheritIO()
            .start()
        val code = process.waitFor()
        val stdout = process.inputStream.bufferedReader().readText()
        val stderr = process.errorStream.bufferedReader().readText()
        ProcessResult(code, stdout, stderr)
    }
}

data class ProcessResult(val code: Int, val stdout: String, val stderr: String)
