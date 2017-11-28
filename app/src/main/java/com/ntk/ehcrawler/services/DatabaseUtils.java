package com.ntk.ehcrawler.services;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.ntk.ehcrawler.EHUtils;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.Book;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DatabaseUtils {
    private static final String LOG_TAG = "LOG_" + DatabaseUtils.class.getSimpleName();

    public static ContentValues getPageData(Context context,String id, String url, String nl) {
        ContentValues contentValues = EHUtils.getPageData(url, nl);
        Uri uri = Uri.withAppendedPath(BookProvider.PAGES_CONTENT_URI, id);
        String selection = PageConstants.URL + "=?";
        String[] selectionArgs = {url};
        int update = context.getContentResolver().update(uri, contentValues, selection, selectionArgs);
        Log.i(LOG_TAG, "get " + update + " url=" + url + " nl=" + nl);
        return contentValues;
    }

    public static void getBookDetail(Context context, String id, String url, int pageIndex) {
        Book book = null;
        try {
            book = EHUtils.getBookInfo(url, pageIndex);
        } catch (IOException e) {
            Log.e(LOG_TAG, "error while get book Info " + url + " - page " + pageIndex, e);
            return;
        }
        if(pageIndex == 0) {
            Map<String, String> infoMap = book.getInfoMap();
            StringBuilder infoBuilder = new StringBuilder();
            for (String key : infoMap.keySet()) {
                infoBuilder.append(key).append(":").append(infoMap.get(key));
                infoBuilder.append(System.getProperty("line.separator"));
            }
            Map<String, Set<String>> tagMap = book.getTagMap();
            StringBuilder tagsBuilder = new StringBuilder();
            for (String key : tagMap.keySet()) {
                tagsBuilder.append(key);
                tagsBuilder.append(":");
                Set<String> tags = tagMap.get(key);
                Iterator<String> iterator = tags.iterator();
                while (iterator.hasNext()) {
                    tagsBuilder.append(iterator.next());
                    if (iterator.hasNext()) {
                        tagsBuilder.append(",");
                    }
                }
                tagsBuilder.append(System.getProperty("line.separator"));
            }
            Uri uri = Uri.withAppendedPath(BookProvider.BOOKS_CONTENT_URI, id);
            ContentValues contentValues = new ContentValues();
            contentValues.put(BookConstants.DETAIL, infoBuilder.toString());
            contentValues.put(BookConstants.TAGS, tagsBuilder.toString());
            String selection = BookConstants.URL + "=?";
            String[] selectionArgs = {url};
            context.getContentResolver().update(uri, contentValues, selection, selectionArgs);
            Log.i(LOG_TAG, "update book details url = " + url);
        }
        //insert page info
        Map<String, String> pageMap = book.getPageMap();
        ContentValues[] pageValues = new ContentValues[pageMap.size()];
        int i = 0;
        for (String pageUrl : pageMap.keySet()) {
            String thumbSrc = pageMap.get(pageUrl);
            pageValues[i] = new ContentValues();
            pageValues[i].put(PageConstants.BOOK_URL, url);
            pageValues[i].put(PageConstants.URL, pageUrl);
            pageValues[i].put(PageConstants.THUMB_SRC, thumbSrc);
            i++;
        }
        int insert = context.getContentResolver().bulkInsert(BookProvider.PAGES_CONTENT_URI, pageValues);
        Log.i(LOG_TAG, "Inserted " + insert + " new pages on page " + pageIndex);
    }
}
