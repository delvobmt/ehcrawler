package com.ntk.ehcrawler.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.EHUtils;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.Book;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;

import java.io.File;
import java.io.IOException;
import java.net.URI;
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
    private static final String ACTION_CLEAR_PAGE_SRC = "CLEAR_PAGE_SRC";

    private static final String LOG_TAG = "LOG_"+DatabaseService.class.getSimpleName();

    private static Map<String, String> filterMap;
    private static int pageIndex = -1;

    public DatabaseService() {
        super("DatabaseService");
    }

    public static void startClearPageSrc(Context context){
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_CLEAR_PAGE_SRC);
        context.startService(intent);
    }

    public static void startGetBook(Context context, int pageIndex) {
        int index = Integer.valueOf(pageIndex);
        if(DatabaseService.pageIndex == index){
            /* don't no reload this page */
            return;
        }else{
            DatabaseService.pageIndex = index;
        }
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_GET_BOOKS);
        intent.putExtra(EHConstants.PAGE_INDEX, pageIndex);
        context.startService(intent);
    }

    public static void startGetBookDetail(Context context, String id, String url, int pageIndex) {
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
        /* reset page Index */
        DatabaseService.pageIndex = -1;
        DatabaseService.filterMap = filterMap;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if(ACTION_CLEAR_PAGE_SRC.equals(action)) {
                clearPageSrc();
            } else if (ACTION_GET_BOOKS.equals(action)) {
                int pageIndex = intent.getIntExtra(EHConstants.PAGE_INDEX,0);
                getBooks(pageIndex);
            } else if (ACTION_GET_BOOK_DETAILS.equals(action)) {
                int pageIndex = intent.getIntExtra(EHConstants.PAGE_INDEX, 0);
                String id = intent.getStringExtra(BookConstants._ID);
                String url = intent.getStringExtra(BookConstants.URL);
                DatabaseUtils.getBookDetail(this, id, url, pageIndex);
            } else if (ACTION_GET_PAGE_IMAGE.equals(action)) {
                String id = intent.getStringExtra(PageConstants._ID);
                String url = intent.getStringExtra(PageConstants.URL);
                String nl = intent.getStringExtra(PageConstants.NEWLINK);
                try {
                    DatabaseUtils.getPageData(this, id, url, nl);
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getLocalizedMessage());
                    return;
                }
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

    private void clearPageSrc(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(PageConstants.SRC, "");
        String selection = PageConstants.SRC+" not like 'file:%'";
        String[] selectionArgs = null;
        Uri uri = BookProvider.PAGES_CONTENT_URI;
        getContentResolver().update(uri, contentValues, selection, selectionArgs);
        Log.i(LOG_TAG, "Clear all page src!");
    }

    private void favoriteBook(String id, boolean favorite) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BookConstants._ID, id);
        contentValues.put(BookConstants.IS_FAVORITE, favorite);
        String selection = PageConstants._ID + "=?";
        String[] selectionArgs = {id};
        Uri uri = BookProvider.BOOKS_CONTENT_URI;
        getContentResolver().update(uri, contentValues, selection, selectionArgs);
        Log.i(LOG_TAG, "Favorite book " + contentValues.valueSet() + " id=" + id);
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
        Log.i(LOG_TAG, "Update book " + contentValues.valueSet() + " url=" + url);
    }

    private void getBooks(int pageIndex) {
        List<Book> books = null;
        try {
            books = EHUtils.getBooks(pageIndex, filterMap);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());
            return;
        }
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
        if (0 == pageIndex) {
            /* set hidden on all kept books, it will be unhidden when appeared in new data list*/
            ContentValues values = new ContentValues();
            values.put(BookConstants.IS_HIDDEN, true);
            int update = getContentResolver().update(BookProvider.BOOKS_CONTENT_URI, values, null, null);
        }
        int insert = getContentResolver().bulkInsert(BookProvider.BOOKS_CONTENT_URI, valuesArray);
        Log.i(LOG_TAG, "inserted " + insert + " new books");
    }
}
