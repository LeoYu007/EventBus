package com.pretty.eventbus.sample;

import android.app.Application;

import com.pretty.eventbus.core.XBus;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        XBus.init();
    }
}
