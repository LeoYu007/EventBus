package com.pretty.eventbus.sample

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.pretty.eventbus.anno.Subscribe
import com.pretty.eventbus.anno.ThreadMode
import com.pretty.eventbus.core.BusAutoRegister
import com.pretty.eventbus.core.BusManager
import kotlinx.android.synthetic.main.f_test.*

class TestFragment : Fragment() {
    private var count = 0
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BusAutoRegister.initWith(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.f_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.LTGRAY)

        // 可以使用XBus发送消息，也可以使用BusManager生成的方法，方便追踪消息在哪里被订阅了
        btn.setOnClickListener {
//            XBus.post(BusTags.TAG_STR, "来自Fragment1的消息 ${count++}")
            BusManager.postTo_str_msg("来自Fragment1的消息 ${count++}")
        }
        btn2.setOnClickListener {
//            XBus.post(BusTags.TAG_NUM, count++)
            BusManager.postTo_num_msg(count++)
        }
        btn3.setOnClickListener {
//            XBus.post(BusTags.TAG_NO_ARG)
            BusManager.postTo_no_arg()
        }
        btnSticky.setOnClickListener {
//            XBus.postSticky(BusTags.TAG_STICKY_NO_ARG)
            BusManager.postTo_no_arg_sticky()
            startTestAty()
        }

        btnSticky2.setOnClickListener {
//            XBus.postSticky(BusTags.TAG_STICKY_MSG, "sticky message ${count++}")
            BusManager.postTo_str_sticky("sticky message ${count++}")
            startTestAty()
        }
    }

    private fun startTestAty() {
        handler.postDelayed({
            startActivity(Intent(activity, TestStickyActivity::class.java))
        }, 1000)
    }


    @Subscribe(tag = BusTags.TAG_NO_ARG, threadMode = ThreadMode.MAIN)
    fun testNoParam() {
        Toast.makeText(
            activity,
            "fragment_1: ${System.currentTimeMillis() / 1000}",
            Toast.LENGTH_SHORT
        ).show()
    }
}