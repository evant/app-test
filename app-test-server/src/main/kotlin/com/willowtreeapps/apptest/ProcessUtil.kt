package com.willowtreeapps.apptest

internal fun exec(vararg cmd: String): ProcessResult {
    println(cmd.joinToString(" "))
    val process = ProcessBuilder()
            .command(*cmd)
            .inheritIO()
            .start()
    val code = process.waitFor()
    val stdout = process.inputStream.bufferedReader().readText()
    val stderr = process.errorStream.bufferedReader().readText()
    return ProcessResult(code, stdout, stderr)
}

internal data class ProcessResult(val code: Int, val stdout: String, val stderr: String)
