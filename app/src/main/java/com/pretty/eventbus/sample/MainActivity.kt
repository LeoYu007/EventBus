package com.pretty.eventbus.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pretty.eventbus.anno.Anno2

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fl1, TestFragment())
            .replace(R.id.fl2, TestFragment2())
            .commit()
    }

}
