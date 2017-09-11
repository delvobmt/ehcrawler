package com.ntk.ehcrawler;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

import com.ntk.ehcrawler.model.Book;
import com.ntk.ehcrawler.model.PageConstants;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EHUtils {

	private static final String LOG_TAG = "LOG_" + EHUtils.class.getSimpleName();

	public static ContentValues getPageData(String pageUrl, String newLink) {
		ContentValues contentValues = new ContentValues();
		try {
            Map<String, String> cookies = prepareCookies(ContextHolder.getCookies());
			Connection connection = Jsoup.connect(pageUrl).cookies(cookies);
			if(!TextUtils.isEmpty(newLink)){
				connection.data(EHConstants.NEWLINK_PARAM, newLink);
			}
			Document doc = connection.get();
			String src = doc.select(EHConstants.IMAGE_CSS_SELECTOR).attr("src");
			String nl = doc.select(EHConstants.NEWLINK_CSS_SELECTOR).attr("onClick")
					.replace("return nl('","").replace("')","");
			contentValues.put(PageConstants.SRC, src);
			contentValues.put(PageConstants.NEWLINK, nl);
			return contentValues;
		} catch (IOException e) {
			Log.w(LOG_TAG, "Error while getPageData()", e);
			return null;
		}
	}

	public static Book getBookInfo(String url, String pageIndex) throws IOException {
		Book book = new Book();
		book.setUrl(url);
		getBookInfo(book, pageIndex);
		return book;
	}

	public static void getBookInfo(Book book, String pageIndex) throws IOException {
		Map<String, String> cookies = prepareCookies(ContextHolder.getCookies());
		String url = book.getUrl();
		Document doc = Jsoup.connect(url).data(EHConstants.PAGE_PAGE_PARAM, pageIndex).cookies(cookies).get();
		//get detail info
		Map<String, String> detailMap = new HashMap<>();
		for (Element e : doc.select(EHConstants.DETAIL_CSS_SELECTOR)) {
			String key = e.select(EHConstants.DETAIL_KEY_CSS_SELECTOR).text().replaceAll(":", "");
			String value = e.select(EHConstants.DETAIL_VALUE_CSS_SELECTOR).text();
			detailMap.put(key, value);
		}
		book.setInfoMap(detailMap);
		//get taglist
		Map<String, Set<String>> tagMap = new HashMap<>();
		for (Element e : doc.select(EHConstants.DETAIL_TAGLIST_CSS_SELECTOR)) {
			String key = e.select(EHConstants.DETAIL_TAG_PRE_CSS_SELECTOR).text().replaceAll(":", "");
			Set<String> value = new HashSet<>();
			for (Element i : e.select(EHConstants.DETAIL_TAG_CSS_SELECTOR)) {
				value.add(i.text());
			}
			tagMap.put(key, value);
		}
		book.setTagMap(tagMap);
		//get pages
		LinkedHashMap<String, String> pageMap = new LinkedHashMap<>();
		for (Element e : doc.select(EHConstants.PAGE_URL_CSS_SELECTOR)) {
			String style = e.parent().attr("style");
			int from = style.indexOf("(") + 1;
			int to = style.indexOf(")");
			String thumbSrc = style.substring(from, to);
			String key = e.attr("href");
			pageMap.put(key, thumbSrc);
		}
		book.setPageMap(pageMap);
	}

	public static List<Book> getBooks(String pageIndex, Map<String, String> filters) {
		Map<String, String> cookies = prepareCookies(ContextHolder.getCookies());
		try {
			Connection connection = Jsoup.connect(EHConstants.HOST);
			if (!"0".equals(pageIndex)) {
				connection.data(EHConstants.BOOK_PAGE_PARAM, pageIndex);
			}
			if(filters != null && !filters.equals(Collections.<String, String>emptyMap())){
				filters.put(EHConstants.SEARCH_APPLY_KEY, EHConstants.SEARCH_APPLY_VALUE);
				connection.data(filters);
			}
			Document doc = connection.cookies(cookies).get();
			List<Book> books = new ArrayList<>();
            for(Element e: doc.select(EHConstants.ITEM_CSS_SELECTOR)){
				String title = e.select(EHConstants.ITEM_TITLE_CSS_SELECTOR).text();
				String url = e.select(EHConstants.ITEM_TITLE_CSS_SELECTOR).attr("href");
				String imageSrc = e.select(EHConstants.ITEM_IMAGE_CSS_SELECTOR).attr("src").replace("l","250");
				String fileCount = e.select(EHConstants.ITEM_FILE_COUNT_CSS_SELECTOR).text();
				String type = e.select(EHConstants.ITEM_TYPE_CSS_SELECTOR).attr("title");
				String style = e.select(EHConstants.ITEM_STAR_RATE_CSS_SELECTOR).attr("style");
				Book book = new Book(title, url, imageSrc, calculateFileCount(fileCount), calculateRate(style), type);
				books.add(book);
			}
			Log.i(LOG_TAG, "get books success at page " + pageIndex + " with " + books.size() + " items");
			return books;
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error while getting books", e);
			return Collections.emptyList();
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
		ContextHolder.setCookies(cookies);
		return cookies;
	}

	public static Map<String, String> getCookies() {
		Connection connection = Jsoup.connect(EHConstants.HOST);
		try {
			Map<String, String> cookies = connection.execute().cookies();
			Log.i(LOG_TAG, String.format("_getCookies_ return %s", cookies));
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
