## 基于编译时注解的EventBus，相比于RxBus，它不依赖于RxJava，更加轻量。相比于Greenrobot的EventBus，它代码更少，少量使用反射，效率更高。

* 使用方式
```
 // 初始化
 XBus.init()

 // 注册
 XBus.register(this)
 // 反注册
 XBus.unregister(this)

 // 注解标记接收消息的方法，注意必须为public的方法，并且最多只能有一个参数或者没有参数
 @Subscribe(tag = BusTags.TAG_NO_ARG)
 fun test(){
   // do something
 }

 // 发送消息
 XBus.post(BusTags.TAG_NO_ARG)
 // 有参消息
 XBus.post(BusTags.TAG_NO_ARG, param)
 // 粘性消息
 XBus.postSticky(BusTags.TAG_STICKY_NO_ARG)

 // 粘性消息在销毁的时候必须移除，否则会造成内存泄露
 XBus.removeSticky(BusTags.TAG_STICKY_MSG)

```
* 也可以使用注解自动生成的BusManager，好处是可以方便的追踪消息在哪里被订阅
```
   // 每个tag都在BusManager中生成一个方法，代码如下：可以直接点击注释的链接跳转目标方法

  /**
   * 订阅的方法：{@link com.pretty.eventbus.sample.TestFragment2#test1}
   */
  public static void postTo_str_msg(String arg) {
    XBus.post("str_msg", arg);
  }

```

## 感谢
核心代码来自：[Blankj/AndroidUtilCode](https://github.com/Blankj/AndroidUtilCode) 的 BusUtils。
原库基于ASM字节码插桩技术插入初始化代码，我简单修改了一下，把核心代码剥离出来，改成用APT生成代码，在init中反射调用来完成。