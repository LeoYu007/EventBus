package com.pretty.eventbus.core;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 入口
 * 规范：
 * 1、Subscribe注解标记的方法，也就是接收消息的方法必须是public的
 * 2、方法最多只能有一个参数，否则生成代码失败会造成消息无法正确接收
 * 3、所有传递的bean都要keep下来，否则混淆会找不到类
 */
public class XBus {
    private static final String TAG = "Bus";

    private XBus() {
    }

    public static void init() {
        try {
            Class<?> aClass = Class.forName("com.pretty.eventbus.BusRegisterImpl");
            Object o = aClass.newInstance();
            Method method = aClass.getDeclaredMethod("registerEvent");
            method.setAccessible(true);
            method.invoke(o);
            Log.i(TAG, "______________init XBus success______________");
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "______________init XBus fail______________");
    }

    public static void register(final Object obj) {
        BusImpl.getInstance().register(obj);
    }

    public static void unregister(final Object obj) {
        BusImpl.getInstance().unregister(obj);
    }

    public static void post(final String tag) {
        BusImpl.getInstance().post(tag);
    }

    public static void post(final String tag, final Object arg) {
        BusImpl.getInstance().post(tag, arg);
    }

    public static void postSticky(final String tag) {
        BusImpl.getInstance().postSticky(tag);
    }

    public static void postSticky(final String tag, final Object arg) {
        BusImpl.getInstance().postSticky(tag, arg);
    }

    public static void removeSticky(final String tag) {
        BusImpl.getInstance().removeSticky(tag);
    }

}
