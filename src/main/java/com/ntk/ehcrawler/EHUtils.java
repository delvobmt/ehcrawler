package com.ntk.ehcrawler;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ntk.ehcrawler.model.Book;

public class EHUtils {

	public static String getPageImageUrl(Map<String, String> pageMap, String keyUrl) {
		try {
			Document doc = Jsoup.connect(keyUrl).cookies(CookiesHolder.get()).get();
			String src = doc.select(EHConstants.IMAGE_CSS_SELECTOR).attr("src");
			pageMap.put(keyUrl, src);
			return src;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static void getBookInfo(Book book) {
		Map<String, String> cookies = CookiesHolder.get();
		String url = book.getUrl();
		try {
			Document doc = Jsoup.connect(url).cookies(cookies).get();
			//get detail info
			Map<String, String> detailMap = doc.select(EHConstants.DETAIL_CSS_SELECTOR).stream()
				.collect(Collectors.toMap(
					e->e.select(EHConstants.DETAIL_KEY_CSS_SELECTOR).text().replaceAll(":", ""), 
					e->e.select(EHConstants.DETAIL_VALUE_CSS_SELECTOR).text()));
			book.setInfoMap(detailMap);
			//get taglist
			Map<String, Set<String>> tagMap = doc.select(EHConstants.DETAIL_TAGLIST_CSS_SELECTOR).stream()
				.collect(Collectors.toMap(
					e->e.select(EHConstants.DETAIL_TAG_PRE_CSS_SELECTOR).text().replaceAll(":", ""),
					e->e.select(EHConstants.DETAIL_TAG_CSS_SELECTOR).stream()
						.map(Element::text).collect(Collectors.toSet())));
			book.setTagMap(tagMap);
			//get pages
			LinkedHashMap<String, String> pageMap = doc.select(EHConstants.PAGE_URL_CSS_SELECTOR).stream()
					.map(e->e.attr("href")).collect(Collectors.toMap(
							Function.identity(), e->"", 
							(k,v)->{throw new IllegalStateException(String.format("Duplicate key %s", k));}, 
							LinkedHashMap::new));
			book.setPageMap(pageMap);
			System.out.println(pageMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Set<Book> getBooks() {
		Map<String, String> cookies = CookiesHolder.get();
		try {
			Document doc = Jsoup.connect(EHConstants.HOST).cookies(cookies).get();
			Elements items = doc.select(EHConstants.ITEM_CSS_SELECTOR);
			Set<Book> collect = items.stream().map(i -> {
				String title = i.select(EHConstants.ITEM_TITLE_CSS_SELECTOR).text();
				String url = i.select(EHConstants.ITEM_TITLE_CSS_SELECTOR).attr("href");
				String imageSrc = i.select(EHConstants.ITEM_IMAGE_CSS_SELECTOR).attr("src");
				String fileCount = i.select(EHConstants.ITEM_FILE_COUNT_CSS_SELECTOR).text();
				String type = i.select(EHConstants.ITEM_TYPE_CSS_SELECTOR).attr("title");
				String style = i.select(EHConstants.ITEM_STAR_RATE_CSS_SELECTOR).attr("style");
				Book book = new Book(title, url, imageSrc, calculateFileCount(fileCount), calculateRate(style), type);
				return book;
			}).collect(Collectors.toSet());
			return collect;
		} catch (IOException e) {
			e.printStackTrace();
			return Collections.emptySet();
		}
	}

	public static Map<String, String> prepareCookies(Map<String, String> cookies) {
		if (cookies == null || cookies.isEmpty()) {
			cookies = getCookies();
		}
		if (!cookies.containsKey("uconfig")) {
			cookies.put("uconfig", "dm_t");
		}
		if (!cookies.containsKey("nw")){
			cookies.put("nw", "1");
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
		int halfStar = Math.abs(Integer.valueOf(split[1].trim()))>EHConstants.RATE_STAR_WIDTH?0:1;
		return 10 - lostStar - halfStar;
	}
	
	public static int calculateFileCount(String text){
		return Integer.valueOf(text.split(" files")[0]);
	}
}
