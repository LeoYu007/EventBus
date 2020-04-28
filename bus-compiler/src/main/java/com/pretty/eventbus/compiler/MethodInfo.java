package com.pretty.eventbus.compiler;

import com.squareup.javapoet.TypeName;

import java.util.List;

public class MethodInfo {
    String tag;
    String className;
    String funName;
    TypeName paramType;
    boolean sticky;
    List<String> d;

    public MethodInfo(String tag, String className, String funName, TypeName paramType, boolean sticky) {
        this.tag = tag;
        this.className = className;
        this.funName = funName;
        this.paramType = paramType;
        this.sticky = sticky;
    }

    public boolean hasParam() {
        return paramType != null;
    }

    @Override
    public int hashCode() {
        if (hasParam()) {
            return (tag + paramType).hashCode();
        } else {
            return tag.hashCode();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MethodInfo)) {
            return false;
        } else {
            MethodInfo mi = (MethodInfo) o;
            if (hasParam()) {
                return tag.equals(mi.tag)
                        && paramType.equals(mi.paramType);
            } else {
                return tag.equals(mi.tag);
            }
        }
    }
}
