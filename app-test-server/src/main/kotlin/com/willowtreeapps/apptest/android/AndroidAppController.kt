package com.willowtreeapps.apptest.android

import com.willowtreeapps.apptest.AppController
import com.willowtreeapps.apptest.Config
import com.willowtreeapps.apptest.exec
import com.willowtreeapps.apptest.proto.LifecycleGrpc
import com.willowtreeapps.apptest.proto.Rpc
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class AndroidAppController(
    val config: Config,
    private val stub: LifecycleGrpc.LifecycleBlockingStub
) : AppController {
    private val adb = Adb(config)

    override fun setup() {
        if (config.androidAppId != null) {
            val cacheDir =
                File(System.getProperty("user.home"), ".cache/app-test/${config.androidAppId}")
            val testApkSigned = File(cacheDir, "app-test-signed.apk")

            if (!testApkSigned.exists()) {
                cacheDir.mkdirs()

                val manifest = File(cacheDir, "AndroidManifest.xml")
                val compiledManifest = File("${manifest.path}.apk")
                val testApkIn = File(cacheDir, "app-test.apk")
                val testApkOut = File(cacheDir, "app-test-out.apk")

                copyResource("/AndroidManifest.xml", manifest)
                copyResource("/app-test-client-android-apk-debug-androidTest.apk", testApkIn)

                exec(
                    aapt(),
                    "package",
                    "-M",
                    manifest.path,
                    "--rename-instrumentation-target-package",
                    config.androidAppId,
                    "-I",
                    androidJar(),
                    "-F",
                    compiledManifest.path,
                    "-f"
                ).subscribe()

                // replace manifest
                compiledManifest.inputStream().let { ZipInputStream(it) }.use { manifest ->
                    manifest.nextEntry
                    testApkIn.inputStream().let { ZipInputStream(it) }.use { apkIn ->
                        testApkOut.outputStream().let { ZipOutputStream(it) }.use { apkOut ->
                            var entry = apkIn.nextEntry
                            while (entry != null) {
                                apkOut.putNextEntry(ZipEntry(entry.name))
                                if (entry.name == "AndroidManifest.xml") {
                                    manifest.copyTo(apkOut)
                                } else {
                                    apkIn.copyTo(apkOut)
                                }
                                entry = apkIn.nextEntry
                            }
                        }
                    }
                }

                val debugKeystore = File(System.getProperty("user.home"), ".android/debug.keystore")
                exec(
                    apkSigner(),
                    "sign",
                    "--ks",
                    debugKeystore.path,
                    "--out",
                    testApkSigned.path,
                    "--ks-key-alias",
                    "androiddebugkey",
                    "--ks-pass",
                    "pass:android",
                    "--key-pass",
                    "pass:android",
                    testApkOut.path
                ).subscribe()
            }

            adb("install", "-r", testApkSigned.path).subscribe()
        }

        if (config.androidApk != null) {
            adb("install", "-r", config.androidApk.path).subscribe()
        }

        adb("forward", "tcp:2734", "tcp:2734").subscribe()

        adb.shell("am instrument -w com.willowtreeapps.apptest.android.apk.test/com.willowtreeapps.apptest.android.AppTestRunner")
            .subscribeOn(Schedulers.io())
            .subscribe()

        Single.timer(500, TimeUnit.MILLISECONDS).blockingGet()

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

    private fun androidJar(): String {
        return File(config.androidSdkDir, "platforms/android-25/android.jar").path
    }

    private fun apkSigner(): String {
        return File(config.androidSdkDir, "build-tools/25.0.0/apksigner").path
    }

    private fun copyResource(resource: String, dest: File) {
        javaClass.getResourceAsStream(resource).buffered().use { input ->
            dest.outputStream().buffered().use { output ->
                input.copyTo(output)
            }
        }
    }
}
