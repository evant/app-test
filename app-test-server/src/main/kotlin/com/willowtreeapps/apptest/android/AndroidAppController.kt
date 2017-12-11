package com.willowtreeapps.apptest.android

import com.willowtreeapps.apptest.AppController
import com.willowtreeapps.apptest.Config
import com.willowtreeapps.apptest.exec
import com.willowtreeapps.apptest.proto.LifecycleGrpc
import com.willowtreeapps.apptest.proto.Rpc
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipInputStream

private const val SERVICE_NAME = "com.willowtreeapps.apptest.android.AppTestServerService"

class AndroidAppController(val config: Config,
                           private val stub: LifecycleGrpc.LifecycleBlockingStub) : AppController {
    private val adb = Adb(config)

    override fun setup() {
        if (config.androidAppId != null) {
            //TODO: figure how to package up this apk
            val testApkPath = "/Users/evantatarka/IdeaProjects/app-test/app-test-client-android-apk/build/outputs/apk/androidTest/debug/app-test-client-android-apk-debug-androidTest.apk"
            val tmpDir = Files.createTempDirectory("app-test")
            val tmpFile = File(tmpDir.toFile(), "app-test.apk")
            val tmpOutDir = File(tmpDir.toFile(), "out")
            val tmpOutFile = File(tmpDir.toFile(), "app-test-out.apk")
            val tmpOutFileSigned = File(tmpDir.toFile(), "app-test-signed.apk")
            File(testApkPath).copyTo(tmpFile, overwrite = true)

            tmpFile.inputStream().buffered().let { ZipInputStream(it) }.use { zis ->
                var zipEntry = zis.nextEntry
                while (zipEntry != null) {
                    val fileName = zipEntry.name
                    val newFile = File(tmpOutDir, fileName)
                    newFile.parentFile.mkdirs()
                    newFile.outputStream().buffered().use { out ->
                        zis.copyTo(out)
                    }
                    zipEntry = zis.nextEntry
                }
                zis.closeEntry()
            }
            exec(aapt(), "p", "--rename-instrumentation-target-package", config.androidAppId, "-F", tmpOutFile.path, tmpOutDir.path)
            val debugKeystore = File(System.getProperty("user.home"), ".android/debug.keystore")
            exec(apkSigner(), "sign", "--ks", debugKeystore.path, "--out", tmpOutFileSigned.path, "--ks-key-alias", "androiddebugkey", "--ks-pass", "pass:android", "--key-pass", "pass:android", tmpOutFile.path)

            adb("install", "-r", tmpOutFileSigned.path)
        }

        if (config.androidApk != null) {
            adb("install", "-r", config.androidApk.path)
        }

        adb.shell("am instrument -w com.willowtreeapps.apptest.android.apk.test/com.willowtreeapps.apptest.android.AppTestRunner")
        stub.setup(Rpc.SetupRequest.getDefaultInstance())
    }

    override fun startApp() {
        stub.startApp(Rpc.StartRequest.getDefaultInstance())
    }

    override fun stopApp() {
        stub.stopApp(Rpc.StopRequest.getDefaultInstance())
    }

    override fun shutdown() {
        stub.shutdown(Rpc.ShutodownRequest.getDefaultInstance())
    }

    private fun aapt(): String {
        return File(config.androidSdkDir, "build-tools/25.0.0/aapt").path
    }

    private fun apkSigner(): String {
        return File(config.androidSdkDir, "build-tools/25.0.0/apksigner").path
    }
}
