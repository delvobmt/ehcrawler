package com.ntk.reactor;

import java.util.HashMap;
import java.util.Map;

public class ContextHolder {
    private static Map<String, String> cookies;
    private static int width;
    private static int height;

    public static void setCookies(Map<String, String> cookies) {
        ContextHolder.cookies = cookies;
    }

    public static Map<String, String> getCookies() {
        return cookies;
    }

    public static void setWidth(int width) {
        ContextHolder.width = width;
    }

    public static int getWidth() {
        return width;
    }

    public static void setHeight(int height) {
        ContextHolder.height = height;
    }

    public static int getHeight() {
        return height;
    }
}
