package com.lidroid.xutils.util.core;

/**
 * Author: wyouflf
 * Date: 14-5-16
 * Time: 上午11:25
 */
public enum Priority {
    UI_TOP(0), UI_NORMAL(1), UI_LOW(2), BG_TOP(3), BG_NORMAL(4), BG_LOW(5);
    private int value = 0;

    Priority(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static Priority valueOf(int value) {
        switch (value) {
            case 0:
                return UI_TOP;
            case 1:
                return UI_NORMAL;
            case 2:
                return UI_LOW;
            case 3:
                return BG_TOP;
            case 4:
                return BG_NORMAL;
            case 5:
                return BG_LOW;
            default:
                return UI_LOW;
        }
    }
}
