package com.pretty.eventbus.core;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SubscriberMethod {

    String className;
    String funName;
    String paramType;
    String paramName;
    boolean sticky;
    String threadMode;
    int priority;
    Method method;
    List<String> subClassNames;

    SubscriberMethod(String className, String funName, String paramType, String paramName,
                     boolean sticky, String threadMode, int priority) {
        this.className = className;
        this.funName = funName;
        this.paramType = paramType;
        this.paramName = paramName;
        this.sticky = sticky;
        this.threadMode = threadMode;
        this.priority = priority;
        this.subClassNames = new CopyOnWriteArrayList<>();
    }

    @Override
    public String toString() {
        return "BusInfo { desc: " + className + "#" + funName +
                ("".equals(paramType) ? "()" : ("(" + paramType + " " + paramName + ")")) +
                ", sticky: " + sticky +
                ", threadMode: " + threadMode +
                ", method: " + method +
                ", priority: " + priority +
                " }";
    }
}
