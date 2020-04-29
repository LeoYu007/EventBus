package com.pretty.eventbus.core;

public interface IBus {

    void register(final Object obj);

    void unregister(final Object obj);

    void post(final String tag);

    void post(final String tag, final Object arg);

    void postSticky(final String tag);

    void postSticky(final String tag, final Object arg);

    void removeSticky(final String tag);

    void removeStickyByClass(final Class<?> clazz);
}
