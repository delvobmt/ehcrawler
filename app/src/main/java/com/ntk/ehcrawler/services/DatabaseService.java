package com.ntk.ehcrawler.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ntk.ehcrawler.database.DatabaseHelper;
import com.ntk.ehcrawler.model.Book;
import com.ntk.ehcrawler.model.BookConstants;

import java.io.Serializable;

public class DatabaseService extends IntentService {
    private static final String ACTION_GET = "GET";
    private static final String ACTION_UPDATE = "UPDATE";

    private static final String EXTRA_TABLE = "COLLECTION";

    private DatabaseHelper mDBHelper;
    public DatabaseService() {
        super("DatabaseService");
    }

    public static void startGetBook(Context context) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_GET);
        intent.putExtra(EXTRA_TABLE, BookConstants.TABLE_NAME);
        context.startService(intent);
    }

    public static void startUpdateBook(Context context) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(EXTRA_TABLE, BookConstants.TABLE_NAME);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET.equals(action)) {
                getBooks();
            } else if (ACTION_UPDATE.equals(action)) {
//                updateBook(param1, param2);
            }
        }
    }

    private void getBooks() {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
    }
}
