package com.pretty.eventbus.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pretty.eventbus.anno.Subscribe
import com.pretty.eventbus.core.BusAutoRegister
import kotlinx.android.synthetic.main.f_test_2.*

class TestFragment2 : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BusAutoRegister.initWith(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.f_test_2, container, false)
    }

    @Subscribe(tag = BusTags.TAG_STR)
    fun test1(message: String) {
        tv.text = message
    }

    @Subscribe(tag = BusTags.TAG_NUM)
    fun test1(number: Int) {
        tv.text = "receive number = $number"
    }

    @Subscribe(tag = BusTags.TAG_NO_ARG)
    fun testNoParam() {
        tv.text = "收到无参数消息: ${System.currentTimeMillis() / 1000}"
    }
}