package com.pretty.eventbus.anno;

public enum ThreadMode {
    POSTING,
    MAIN,
    IO,
    CPU;

    @Override
    public String toString() {
        switch (this) {
            case POSTING:
                return "POSTING";
            case MAIN:
                return "MAIN";
            case IO:
                return "IO";
            case CPU:
                return "CPU";
        }
        return super.toString();
    }
}
