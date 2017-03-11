package com.ntk.ehcrawler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ntk.ehcrawler.model.Book;

public class EHUtils {

	public static void main(String[] args) {
		Map<String, String> cookies = CookiesHolder.get();
		cookies = prepareCookies(cookies);
		try {
			Document doc = Jsoup.connect(EHConstants.HOST).cookies(cookies).get();
			Elements items = doc.select(EHConstants.ITEM_CSS_SELECTOR);
			items.forEach(i -> {
				String title = i.select(EHConstants.ITEM_TITLE_CSS_SELECTOR).text();
				String imageSrc = i.select(EHConstants.ITEM_IMAGE_CSS_SELECTOR).attr("src");
				String fileCount = i.select(EHConstants.ITEM_FILE_COUNT_CSS_SELECTOR).text();
				String type = i.select(EHConstants.ITEM_TYPE_CSS_SELECTOR).attr("title");
				String style = i.select(EHConstants.ITEM_STAR_RATE_CSS_SELECTOR).attr("style");
				Book book = new Book(title, imageSrc, calculateFileCount(fileCount), calculateRate(style), type);
				System.out.println(book);
			});

		} catch (IOException e) {
		}
	}

	private static Map<String, String> prepareCookies(Map<String, String> cookies) {
		if (cookies == null || cookies.isEmpty()) {
			cookies = getCookies();
		}
		if (!cookies.containsKey("uconfig")) {
			cookies.put("uconfig", "dm_t");
		}
		return cookies;
	}

	public static Map<String, String> getCookies() {
		Connection connection = Jsoup.connect(EHConstants.HOST);
		try {
			Map<String, String> cookies = connection.execute().cookies();
			System.out.println(String.format("_getCookies_ return %s", cookies));
			return cookies;
		} catch (IOException e) {
			return Collections.emptyMap();
		}
	}
	
	public static int calculateRate(String style){
		String[] split = style.split(";",2)[0].split(":")[1].split("px");
		int lostStar = Math.abs(Integer.valueOf(split[0].trim()))/EHConstants.RATE_STAR_WIDTH;
		int halfStar = Math.abs(Integer.valueOf(split[1].trim()))>EHConstants.RATE_STAR_WIDTH?1:0;
		return 10 - lostStar - halfStar;
	}
	
	public static int calculateFileCount(String text){
		return Integer.valueOf(text.split(" files")[0]);
	}
}
