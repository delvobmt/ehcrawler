package com.ntk.ehcrawler;

import java.util.Map;

public class TheHolder {
	private static Map<String, String> cookies;
	private static int width;
	private static int height;

	public static Map<String, String> getCookies() {
		return cookies;
	}
	
	public static void setCookies(Map<String, String> value) {
		cookies = value;
	}

	public static int getScreenHeight() {
		return height;
	}

	public static void setHeight(int height) {
		TheHolder.height = height;
	}

	public static int getScreenWidth() {
		return width;
	}

	public static void setWidth(int width) {
		TheHolder.width = width;
	}
}
