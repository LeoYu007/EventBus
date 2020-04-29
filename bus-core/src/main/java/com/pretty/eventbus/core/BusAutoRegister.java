package com.pretty.eventbus.core;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * 提供XBus在Activity和Fragment中的自动注册和注销功能
 */
public class BusAutoRegister implements LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy(LifecycleOwner owner) {
        XBus.removeStickyByClass(owner.getClass());
        XBus.unregister(owner);
        Log.i("Bus", owner.toString() + ": 注销了XBus");
    }

    public static void initWith(LifecycleOwner lifecycleOwner) {
        XBus.register(lifecycleOwner);
        lifecycleOwner.getLifecycle().addObserver(new BusAutoRegister());
    }
}
