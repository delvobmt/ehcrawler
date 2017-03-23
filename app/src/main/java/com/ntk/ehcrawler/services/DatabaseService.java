package com.ntk.ehcrawler.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.provider.Settings;

import com.ntk.ehcrawler.EHUtils;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.Book;
import com.ntk.ehcrawler.model.BookConstants;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatabaseService extends IntentService {
    private static final String ACTION_GET_BOOKS = "GET_BOOKS";
    private static final String ACTION_GET_BOOK_DETAILS = "GET_BOOK_DETAILS";

    public static final String EXTRA_URL = "URL";

    public DatabaseService() {
        super("DatabaseService");
    }

    public static void startGetBook(Context context) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_GET_BOOKS);
        context.startService(intent);
    }

    public static void startGetBookDetail(Context context, String url) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_GET_BOOK_DETAILS);
        intent.putExtra(EXTRA_URL, url);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_BOOKS.equals(action)) {
                getBooks();
            }else if (ACTION_GET_BOOK_DETAILS.equals(action)){
                String url = intent.getStringExtra(EXTRA_URL);
                getBookDetail(url);
            }
        }
    }

    private void getBookDetail(String url) {
        Book book = new Book();
        book.setUrl(url);
        EHUtils.getBookInfo(book);
        Map<String, String> infoMap = book.getInfoMap();
        StringBuilder infoBuilder = new StringBuilder();
        for(String key: infoMap.keySet()){
            infoBuilder.append(key).append(":").append(infoMap.get(key));
            infoBuilder.append(System.getProperty("line.separator"));
        }
        Map<String, Set<String>> tagMap = book.getTagMap();
        StringBuilder tagsBuilder = new StringBuilder();
        for(String key: tagMap.keySet()){
            tagsBuilder.append(key);
            tagsBuilder.append(":");
            Set<String> tags = tagMap.get(key);
            Iterator<String> iterator = tags.iterator();
            while (iterator.hasNext()){
                tagsBuilder.append(iterator.next());
                if(iterator.hasNext()){
                    tagsBuilder.append(",");
                }
            }
            tagsBuilder.append(System.getProperty("line.separator"));
        }
        Map<String, String> pageMap = book.getPageMap();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BookConstants.DETAIL, infoBuilder.toString());
        contentValues.put(BookConstants.TAGS, tagsBuilder.toString());
        String selection = "url=?";
        String[] selectionArgs = {url};
        getContentResolver().update(BookProvider.BOOKS_CONTENT_URI, contentValues, selection, selectionArgs);
    }

    private void getBooks() {
        List<Book> books = EHUtils.getBooks();
        ContentValues[] valuesArray = new ContentValues[books.size()];
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            valuesArray[i] = new ContentValues();
            valuesArray[i].put(BookConstants.TITLE, book.getTitle());
            valuesArray[i].put(BookConstants.URL, book.getUrl());
            valuesArray[i].put(BookConstants.IMAGE_SRC, book.getImageSrc());
            valuesArray[i].put(BookConstants.FILE_COUNT, book.getFileCount());
            valuesArray[i].put(BookConstants.RATE, book.getRate());
            valuesArray[i].put(BookConstants.TYPE, book.getType());
        }
        getContentResolver().bulkInsert(BookProvider.BOOKS_CONTENT_URI,valuesArray);
    }
}
