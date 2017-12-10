package com.example.app_test_android_sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_second)

        Single.fromCallable {
            Thread.sleep(TimeUnit.SECONDS.toMillis(5))
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { n ->
                    findViewById<TextView>(R.id.hello_world2).text = "Hello World 2!"
                }
    }
}