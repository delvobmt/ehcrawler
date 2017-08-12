package com.ntk.ehcrawler.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.EHUtils;
import com.ntk.ehcrawler.TheHolder;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.Book;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatabaseService extends IntentService {
    private static final String ACTION_GET_BOOKS = "GET_BOOKS";
    private static final String ACTION_GET_BOOK_DETAILS = "GET_BOOK_DETAILS";
    private static final String ACTION_GET_PAGE_IMAGE = "GET_BOOK_IMAGE";
    private static final String ACTION_UPDATE_BOOK_POSITION = "UPDATE_BOOK_POSITION";
    private static final String ACTION_FAVORITE_BOOK = "FAVORITE_BOOK";

    private static Map<String, String> filterMap;

    public DatabaseService() {
        super("DatabaseService");
    }

    public static void startGetBook(Context context, String pageIndex) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_GET_BOOKS);
        intent.putExtra(EHConstants.PAGE_INDEX, pageIndex);
        context.startService(intent);
    }

    public static void startGetBookDetail(Context context, String id, String url, String pageIndex) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_GET_BOOK_DETAILS);
        intent.putExtra(BookConstants._ID, id);
        intent.putExtra(BookConstants.URL, url);
        intent.putExtra(EHConstants.PAGE_INDEX, pageIndex);
        context.startService(intent);
    }


    public static void startGetPageData(Context context, String id, String url) {
        startGetPageData(context, id, url, null);
    }

    public static void startGetPageData(Context context, String id, String url, String newLink) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_GET_PAGE_IMAGE);
        intent.putExtra(PageConstants._ID, id);
        intent.putExtra(PageConstants.URL, url);
        intent.putExtra(PageConstants.NEWLINK, newLink);
        context.startService(intent);
    }

    public static void startUpdateBookPosition(Context context, String url, int position) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_UPDATE_BOOK_POSITION);
        intent.putExtra(BookConstants.URL, url);
        intent.putExtra(BookConstants.CURRENT_POSITION, position);
        context.startService(intent);
    }

    public static void startFavoriteBook(Context context, String id, boolean favorite) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_FAVORITE_BOOK);
        intent.putExtra(BookConstants._ID, id);
        intent.putExtra(BookConstants.IS_FAVORITE, favorite);
        context.startService(intent);
    }

    public static void setFilterMap(Map<String, String> filterMap) {
        DatabaseService.filterMap = filterMap;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_BOOKS.equals(action)) {
                String pageIndex = intent.getStringExtra(EHConstants.PAGE_INDEX);
                getBooks(pageIndex);
            } else if (ACTION_GET_BOOK_DETAILS.equals(action)) {
                String pageIndex = intent.getStringExtra(EHConstants.PAGE_INDEX);
                String id = intent.getStringExtra(BookConstants._ID);
                String url = intent.getStringExtra(BookConstants.URL);
                getBookDetail(id, url, pageIndex);
            } else if (ACTION_GET_PAGE_IMAGE.equals(action)) {
                String id = intent.getStringExtra(PageConstants._ID);
                String url = intent.getStringExtra(PageConstants.URL);
                String nl = intent.getStringExtra(PageConstants.NEWLINK);
                getPageData(id, url, nl);
            }else if(ACTION_UPDATE_BOOK_POSITION.equals(action)){
                String url = intent.getStringExtra(BookConstants.URL);
                int position = intent.getIntExtra(BookConstants.CURRENT_POSITION, 0);
                updateBookPosition(url, position);
            }else if(ACTION_FAVORITE_BOOK.equals(action)){
                String id = intent.getStringExtra(BookConstants._ID);
                Boolean favorite = intent.getBooleanExtra(BookConstants.IS_FAVORITE, false);
                favoriteBook(id, favorite);
            }
        }
    }

    private void favoriteBook(String id, boolean favorite) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BookConstants._ID, id);
        contentValues.put(BookConstants.IS_FAVORITE, favorite);
        String selection = PageConstants._ID + "=?";
        String[] selectionArgs = {id};
        Uri uri = BookProvider.BOOKS_CONTENT_URI;
        getContentResolver().update(uri, contentValues, selection, selectionArgs);
    }

    private void updateBookPosition(String url, int position) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BookConstants.CURRENT_POSITION, position);
        contentValues.put(BookConstants.URL, url);
        contentValues.put(BookConstants.LAST_READ, System.currentTimeMillis());
        String selection = PageConstants.URL + "=?";
        String[] selectionArgs = {url};
        Uri uri = BookProvider.BOOK_STATUS_CONTENT_URI;
        getContentResolver().update(uri, contentValues, selection, selectionArgs);
    }

    private void getPageData(String id, String url, String nl) {
        ContentValues contentValues = EHUtils.getPageData(url, nl);
        Uri uri = Uri.withAppendedPath(BookProvider.PAGES_CONTENT_URI, id);
        String selection = PageConstants.URL + "=?";
        String[] selectionArgs = {url};
        getContentResolver().update(uri, contentValues, selection, selectionArgs);
    }

    private void getBookDetail(String id, String url, String pageIndex) {
        Book book = new Book();
        book.setUrl(url);
        try {
            EHUtils.getBookInfo(book, pageIndex);
        } catch (IOException e) {
            //TODO: REPORT UI
            return;
        }
        if("0".equals(pageIndex)) {
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
            getContentResolver().update(uri, contentValues, selection, selectionArgs);
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
        getContentResolver().bulkInsert(BookProvider.PAGES_CONTENT_URI, pageValues);
    }

    private void getBooks(String pageIndex) {
        List<Book> books = EHUtils.getBooks(pageIndex, filterMap);
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
        if("0".equals(pageIndex.trim())) {
            /* clear data , keep favorite books */
            String where = BookConstants.IS_FAVORITE + "!=?";
            String[] whereArgs = {"1"};
            int delete = getContentResolver().delete(BookProvider.BOOKS_CONTENT_URI, where, whereArgs);

            /* set hidden on all kept books, it will be unhidden when appeared in new data list*/
            ContentValues values = new ContentValues();
            values.put(BookConstants.IS_HIDDEN, true);
            int update = getContentResolver().update(BookProvider.BOOKS_CONTENT_URI, values, null, null);
            int delete1 = getContentResolver().delete(BookProvider.PAGES_CONTENT_URI, null, null);
        }
        getContentResolver().bulkInsert(BookProvider.BOOKS_CONTENT_URI, valuesArray);
    }
}
