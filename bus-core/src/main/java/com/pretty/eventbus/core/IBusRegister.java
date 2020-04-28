package com.pretty.eventbus.core;

/**
 * 注解解析器会生成一个实现类BusRegisterImpl
 * XBus.init()的时候会创建BusRegisterImpl并调用registerEvent方法完成Method的注册
 */
public interface IBusRegister {

    void registerEvent();
}
