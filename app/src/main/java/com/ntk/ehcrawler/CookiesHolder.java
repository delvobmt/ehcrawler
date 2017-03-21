package com.ntk.ehcrawler;

import java.util.Map;

public class CookiesHolder {
	public static Map<String, String> holder;
	
	public static Map<String, String> get() {
		return holder;
	}
	
	public static void set(Map<String, String> value) {
		holder = value;
	}
	
	public static void clear(){
		holder = null;
	}
}
