package com.ntk.ehcrawler.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;

public class DatabaseHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "ehcrawler";
    public static final int DATABASE_VERSION = 2;

    public static final String[] SQL_CREATE_DATABASE = {
            "CREATE TABLE " + BookConstants.TABLE_NAME
                    + " (" + BookConstants._ID + " INTEGER PRIMARY KEY,"
                    + BookConstants.TITLE + " TEXT,"
                    + BookConstants.URL + " TEXT UNIQUE,"
                    + BookConstants.IMAGE_SRC + " TEXT,"
                    + BookConstants.FILE_COUNT + " TEXT,"
                    + BookConstants.RATE + " INT,"
                    + BookConstants.TYPE + " TEXT,"
                    + BookConstants.DETAIL + " TEXT,"
                    + BookConstants.TAGS + " TEXT)",
            "CREATE TABLE " + BookConstants.TABLE_BOOK_STATUS_NAME
                    + " (" + BookConstants._ID + " INTEGER PRIMARY KEY,"
                    + BookConstants.URL + " TEXT UNIQUE,"
                    + BookConstants.CURRENT_POSITION + " INT,"
                    + BookConstants.IS_FAVORITE + " INT)",
            "CREATE TABLE " + BookConstants.TABLE_FAVORITE_NAME
                    + " (" + BookConstants._ID + " INTEGER PRIMARY KEY,"
                    + BookConstants.TITLE + " TEXT,"
                    + BookConstants.URL + " TEXT UNIQUE,"
                    + BookConstants.IMAGE_SRC + " TEXT,"
                    + BookConstants.FILE_COUNT + " TEXT,"
                    + BookConstants.RATE + " INT,"
                    + BookConstants.TYPE + " TEXT,"
                    + BookConstants.DETAIL + " TEXT,"
                    + BookConstants.TAGS + " TEXT)",
            "CREATE TABLE " + PageConstants.TABLE_NAME
                    + " (" + PageConstants._ID + " INTEGER PRIMARY KEY,"
                    + PageConstants.BOOK_URL + " TEXT,"
                    + PageConstants.URL + " TEXT,"
                    + PageConstants.THUMB_SRC + " TEXT,"
                    + PageConstants.SRC + " TEXT,"
                    + PageConstants.NEWLINK + " TEXT);"
    };
    public static final String SQL_FAVORITE_BOOK = "INSERT INTO " + BookConstants.TABLE_FAVORITE_NAME
            + "("+ BookConstants.TITLE + ","
            + BookConstants.URL + ","
            + BookConstants.IMAGE_SRC + ","
            + BookConstants.FILE_COUNT + ","
            + BookConstants.RATE + ","
            + BookConstants.TYPE + ","
            + BookConstants.DETAIL + ","
            + BookConstants.TAGS
            +") SELECT "
            + BookConstants.TITLE + ","
            + BookConstants.URL + ","
            + BookConstants.IMAGE_SRC + ","
            + BookConstants.FILE_COUNT + ","
            + BookConstants.RATE + ","
            + BookConstants.TYPE + ","
            + BookConstants.DETAIL + ","
            + BookConstants.TAGS
            +" FROM " + BookConstants.TABLE_NAME + " WHERE " + BookConstants._ID + "=?";
    public static final String[] SQL_CLEAR_DATABASE = {
            "DROP TABLE IF EXISTS " + BookConstants.TABLE_NAME,
            "DROP TABLE IF EXISTS " + BookConstants.TABLE_FAVORITE_NAME,
            "DROP TABLE IF EXISTS " + BookConstants.TABLE_BOOK_STATUS_NAME,
            "DROP TABLE IF EXISTS " + PageConstants.TABLE_NAME
    };

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String sql : SQL_CREATE_DATABASE) {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (String sql : SQL_CLEAR_DATABASE) {
            db.execSQL(sql);
        }
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
