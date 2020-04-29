package com.pretty.eventbus.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pretty.eventbus.anno.Subscribe
import com.pretty.eventbus.core.BusAutoRegister
import com.pretty.eventbus.core.XBus
import kotlinx.android.synthetic.main.a_test_sticky.btn
import kotlinx.android.synthetic.main.a_test_sticky.tv

class TestStickyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_test_sticky)

        BusAutoRegister.initWith(this)

        btn.setOnClickListener {
            XBus.post(BusTags.TAG_NO_ARG)
        }
    }

    @Subscribe(tag = BusTags.TAG_STICKY_MSG, priority = 22, sticky = true)
    fun testSticky(message: String) {
        tv.text = message
    }

    @Subscribe(tag = BusTags.TAG_STICKY_NO_ARG, priority = 11, sticky = true)
    fun testSticky2() {
        tv.text = "收到无参数粘性消息:${System.currentTimeMillis() / 1000}"
    }

}
