package com.ntk.ehcrawler;

import java.io.IOException;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class EHUtils {
	
	public static void main(String[] args) {
		get();
	}
	
	public static void get() {
		Connection connection = Jsoup.connect(EHConstants.HOST);
		try {
			Map<String, String> cookies = connection.execute().cookies();
			Document document = connection.get();
			System.out.println(cookies);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
