package com.ntk.ehcrawler.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ntk.ehcrawler.model.BookConstants;

public class DatabaseHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "ehcrawler";
    public static final int DATABASE_VERSION = 1;
    public static final String SQL_CREATE_BOOK_DATABASE =
            "CREATE TABLE "+BookConstants.TABLE_NAME
            +" ("+BookConstants._ID+" INTEGER PRIMARY KEY,"
            +BookConstants.TITLE+" TEXT,"
            +BookConstants.URL+" TEXT UNIQUE,"
            +BookConstants.IMAGE_SRC+" TEXT,"
            +BookConstants.FILE_COUNT+" TEXT,"
            +BookConstants.RATE+" INT,"
            +BookConstants.TYPE+" TEXT)";
    public static final String SQL_CLEAR_DATABASE =
            "DROP TABLE IF EXISTS "+BookConstants.TABLE_NAME;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BOOK_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_CLEAR_DATABASE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
