package com.ntk.ehcrawler;

import java.util.Map;

public class CookiesHolder {
	public static ThreadLocal<Map<String, String>> holder = new ThreadLocal<>();
	
	public static Map<String, String> get() {
		return holder.get();
	}
	
	public static void set(Map<String, String> value) {
		holder.set(value);
	}
	
	public static void clear(){
		holder.remove();
	}
}
