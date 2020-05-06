package com.pretty.eventbus.core;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
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

    public static void initWith(AppCompatActivity activity) {
        XBus.register(activity);
        activity.getLifecycle().addObserver(new BusAutoRegister());
        Log.i("Bus", activity.toString() + ": 注册了XBus");
    }

    public static void initWith(Fragment fragment) {
        XBus.register(fragment);
        fragment.getLifecycle().addObserver(new BusAutoRegister());
        Log.i("Bus", fragment.toString() + ": 注册了XBus");
    }
}
