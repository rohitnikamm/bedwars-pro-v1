package com.github.rohitnikamm.bedwarsprov1;

public class DebugControl {
    private static volatile boolean enabled = true;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean e) {
        enabled = e;
    }
}

