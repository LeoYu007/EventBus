package com.pretty.eventbus.core;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class BusImpl implements IBus {

    private static final Object NULL = "nULl";
    private static final String TAG = "Bus";

    private final Map<String, List<SubscriberMethod>> mTag_MethodInfoListMap = new HashMap<>();

    private final Map<String, Set<Object>> mClassName_ObjectsMap = new ConcurrentHashMap<>();
    private final Map<String, List<String>> mClassName_TagsMap = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> mClassName_Tag_Arg4StickyMap = new ConcurrentHashMap<>();

    private BusImpl() {
    }

    @Override
    public String toString() {
        return "Bus: " + mTag_MethodInfoListMap;
    }

    public static BusImpl getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void registerBus(String tag, String className, String funName, String paramType,
                            String paramName, boolean sticky, String threadMode, int priority) {
        List<SubscriberMethod> methodInfoList = mTag_MethodInfoListMap.get(tag);
        if (methodInfoList == null) {
            methodInfoList = new ArrayList<>();
            mTag_MethodInfoListMap.put(tag, methodInfoList);
        }
        methodInfoList.add(
                new SubscriberMethod(className, funName, paramType, paramName, sticky, threadMode, priority)
        );
    }

    @Override
    public void post(String tag) {
        post(tag, NULL);
    }

    @Override
    public void postSticky(String tag) {
        postSticky(tag, NULL);
    }


    @Override
    public void register(final Object obj) {
        if (obj == null) return;
        Class aClass = obj.getClass();
        String className = aClass.getName();
        synchronized (mClassName_ObjectsMap) {
            Set<Object> objectSet = mClassName_ObjectsMap.get(className);
            if (objectSet == null) {
                objectSet = new CopyOnWriteArraySet<>();
                mClassName_ObjectsMap.put(className, objectSet);
            }
            objectSet.add(obj);
        }
        List<String> tags = mClassName_TagsMap.get(className);
        if (tags == null) {
            synchronized (mClassName_TagsMap) {
                tags = mClassName_TagsMap.get(className);
                if (tags == null) {
                    tags = new CopyOnWriteArrayList<>();
                    for (Map.Entry<String, List<SubscriberMethod>> entry : mTag_MethodInfoListMap.entrySet()) {
                        for (SubscriberMethod methodInfo : entry.getValue()) {
                            try {
                                if (Class.forName(methodInfo.className).isAssignableFrom(aClass)) {
                                    tags.add(entry.getKey());
                                    methodInfo.subClassNames.add(className);
                                }
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    mClassName_TagsMap.put(className, tags);
                }
            }
        }
        // 判断新的注册者有没有订阅sticky事件，有就发送
        processSticky(obj);
    }

    private void processSticky(final Object obj) {
        Map<String, Object> tagArgMap = mClassName_Tag_Arg4StickyMap.get(obj.getClass().getName());
        if (tagArgMap == null) return;
        synchronized (mClassName_Tag_Arg4StickyMap) {
            for (Map.Entry<String, Object> tagArgEntry : tagArgMap.entrySet()) {
                post(tagArgEntry.getKey(), tagArgEntry.getValue());
            }
        }
    }

    @Override
    public void unregister(final Object bus) {
        if (bus == null) return;
        String className = bus.getClass().getName();
        synchronized (mClassName_ObjectsMap) {
            Set<Object> buses = mClassName_ObjectsMap.get(className);
            if (buses == null || !buses.contains(bus)) {
                System.out.println("The bus of <" + bus + "> was not registered before.");
                return;
            }
            buses.remove(bus);
        }
    }


    @Override
    public void post(final String tag, final Object arg) {
        post(tag, arg, false);
    }

    private void post(final String tag, final Object arg, final boolean sticky) {
        List<SubscriberMethod> methodInfoList = mTag_MethodInfoListMap.get(tag);
        if (methodInfoList == null) {
            System.out.println("The bus of tag <" + tag + "> is not exists.");
            return;
        }
        for (SubscriberMethod methodInfo : methodInfoList) {
            if (methodInfo.method == null) {
                Method method = getMethod(methodInfo);
                if (method == null) {
                    return;
                }
                methodInfo.method = method;
            }
            invokeMethod(tag, arg, methodInfo, sticky);
        }
    }

    private Method getMethod(SubscriberMethod methodInfo) {
        try {
            if ("".equals(methodInfo.paramType)) {
                return Class.forName(methodInfo.className).getDeclaredMethod(methodInfo.funName);
            } else {
                return Class.forName(methodInfo.className)
                        .getDeclaredMethod(methodInfo.funName, getClassName(methodInfo.paramType));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Class getClassName(String paramType) throws ClassNotFoundException {
        switch (paramType) {
            case "boolean":
                return boolean.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "short":
                return short.class;
            case "byte":
                return byte.class;
            case "double":
                return double.class;
            case "float":
                return float.class;
            case "char":
                return char.class;
            default:
                return Class.forName(paramType);
        }
    }

    private void invokeMethod(final String tag, final Object arg,
                              final SubscriberMethod methodInfo, final boolean sticky) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                realInvokeMethod(tag, arg, methodInfo, sticky);
            }
        };
        switch (methodInfo.threadMode) {
            case "MAIN":
                BusExecutors.getMainExecutor().execute(runnable);
                return;
            case "IO":
                BusExecutors.getIOExecutor().submit(runnable);
                return;
            case "CPU":
                BusExecutors.getCPUExecutor().submit(runnable);
                return;
            default:
                runnable.run();
        }
    }

    private void realInvokeMethod(final String tag, Object arg, SubscriberMethod methodInfo, boolean sticky) {
        Set<Object> objectSet = new HashSet<>();
        for (String subClassName : methodInfo.subClassNames) {
            Set<Object> subSet = mClassName_ObjectsMap.get(subClassName);
            if (subSet != null && !subSet.isEmpty()) {
                objectSet.addAll(subSet);
            }
        }
        if (objectSet.size() == 0) {
            if (!sticky) {
                Log.i(TAG, "The bus of tag <" + tag + "> was not registered before.");
                return;
            } else {
                return;
            }
        }
        try {
            if (arg == NULL) {
                for (Object obj : objectSet) {
                    methodInfo.method.invoke(obj);
                }
            } else {
                for (Object obj : objectSet) {
                    methodInfo.method.invoke(obj, arg);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void postSticky(final String tag, final Object arg) {
        List<SubscriberMethod> methodInfoList = mTag_MethodInfoListMap.get(tag);
        if (methodInfoList == null) {
            Log.i(TAG, "The bus of tag <" + tag + "> is not exists.");
            return;
        }
        for (SubscriberMethod methodInfo : methodInfoList) {
            if (!methodInfo.sticky) { // not sticky bus will post directly.
                post(tag, arg);
                return;
            }
            synchronized (mClassName_Tag_Arg4StickyMap) {
                Map<String, Object> tagArgMap = mClassName_Tag_Arg4StickyMap.get(methodInfo.className);
                if (tagArgMap == null) {
                    tagArgMap = new HashMap<>();
                    mClassName_Tag_Arg4StickyMap.put(methodInfo.className, tagArgMap);
                }
                tagArgMap.put(tag, arg);
            }
            post(tag, arg, true);
        }
    }

    @Override
    public void removeSticky(final String tag) {
        List<SubscriberMethod> methodInfoList = mTag_MethodInfoListMap.get(tag);
        if (methodInfoList == null) {
            Log.i(TAG, "The bus of tag <" + tag + "> is not exists.");
            return;
        }
        for (SubscriberMethod methodInfo : methodInfoList) {
            if (!methodInfo.sticky) {
                Log.i(TAG, "The bus of tag <" + tag + "> is not sticky.");
                return;
            }
            synchronized (mClassName_Tag_Arg4StickyMap) {
                Map<String, Object> tagArgMap = mClassName_Tag_Arg4StickyMap.get(methodInfo.className);
                if (tagArgMap == null || !tagArgMap.containsKey(tag)) {
                    Log.i(TAG, "The sticky bus of tag <" + tag + "> didn't post.");
                    return;
                }
                tagArgMap.remove(tag);
            }
        }
    }

    private static class LazyHolder {
        private static final BusImpl INSTANCE = new BusImpl();
    }
}
