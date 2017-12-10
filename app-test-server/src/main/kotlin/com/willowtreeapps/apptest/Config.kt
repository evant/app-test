package com.willowtreeapps.apptest

import java.io.File
import java.io.FileReader
import java.util.*

class Config(
        val androidAppId: String? = null,
        val androidSdkDir: File? = null,
        val androidApk: File? = null
) {
    companion object {

        @JvmStatic
        fun fromResource(path: String): Config
                = fromProperties(Properties().apply { javaClass.getResourceAsStream(path).use { load(it) } })

        @JvmStatic
        fun fromFile(file: File): Config =
                fromProperties(Properties().apply { FileReader(file).use { load(it) } })

        @JvmStatic
        fun fromProperties(props: Properties): Config = Config(
                androidAppId = props.getProperty("android.appId"),
                androidSdkDir = props.getProperty("android.sdkDir")?.let{ File(it) },
                androidApk = props.getProperty("android.apk")?.let { File(it) }
        )
    }
}
