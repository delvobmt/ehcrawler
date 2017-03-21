package com.ntk.ehcrawler.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ntk.ehcrawler.EHUtils;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.database.DatabaseHelper;
import com.ntk.ehcrawler.model.Book;
import com.ntk.ehcrawler.model.BookConstants;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

public class DatabaseService extends IntentService {
    private static final String ACTION_GET = "GET";

    private static final String EXTRA_TABLE = "COLLECTION";

    public DatabaseService() {
        super("DatabaseService");
    }

    public static void startGetBook(Context context) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_GET);
        intent.putExtra(EXTRA_TABLE, BookConstants.TABLE_NAME);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET.equals(action)) {
                getBooks();
            }
        }
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
