package com.ntk.ehcrawler.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.SparseArray;

import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;

public class BookProvider extends ContentProvider {
    private static final String AUTHORITY = "com.ntk.ehcrawler.providers";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final Uri BOOKS_CONTENT_URI = Uri.withAppendedPath(
            CONTENT_URI, BookConstants.TABLE_NAME);
    public static final Uri PAGES_CONTENT_URI = Uri.withAppendedPath(
            CONTENT_URI, PageConstants.TABLE_NAME);

    private static final int BOOKS_QUERY = 1;
    private static final int ONE_BOOK_QUERY = 2;
    private static final int PAGES_QUERY = 3;
    private static final UriMatcher sUriMatcher;
    private static final SparseArray<String> sMimeTypes;
    public static final int BOOKS_LOADER = 0;
    public static final int BOOK_INFO_LOADER = 1;
    public static final int PAGE_INFO_LOADER = 2;

    static {
        sUriMatcher = new UriMatcher(0);
        sUriMatcher.addURI(AUTHORITY, BookConstants.TABLE_NAME, BOOKS_QUERY);
        sUriMatcher.addURI(AUTHORITY, PageConstants.TABLE_NAME, PAGES_QUERY);

        sMimeTypes = new SparseArray<>();
        sMimeTypes.put(BOOKS_QUERY, "vnd.android.cursor.dir/vnd." + AUTHORITY
                + "." + BookConstants.TABLE_NAME);
        sMimeTypes.put(ONE_BOOK_QUERY, "vnd.android.cursor.item/vnd." + AUTHORITY
                + "." + BookConstants.TABLE_NAME);
        sMimeTypes.put(PAGES_QUERY, "vnd.android.cursor.dir/vnd." + AUTHORITY
                + "." + PageConstants.TABLE_NAME);
    }

    private DatabaseHelper mDBHelper;
    public BookProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case BOOKS_QUERY:
                return db.delete(BookConstants.TABLE_NAME, selection, selectionArgs);
            case PAGES_QUERY:
                return db.delete(PageConstants.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Insert invalid URI " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        return sMimeTypes.get(sUriMatcher.match(uri));
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case BOOKS_QUERY:
                try {
                    db.insertOrThrow(BookConstants.TABLE_NAME, null, values);
                } catch (SQLiteConstraintException e) {
                    //book url is existed, update book with new info
                    String whereClause = BookConstants.URL + "=?";
                    String[] whereArgs = { values.getAsString(BookConstants.URL) };
                    db.update(BookConstants.TABLE_NAME, values, whereClause, whereArgs);
                }
            default:
                throw new IllegalArgumentException("Insert invalid URI " + uri);
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case BOOKS_QUERY:
                db.beginTransaction();
                for (ContentValues v : values) {
                    try {
                        db.insertOrThrow(BookConstants.TABLE_NAME, null, v);
                        count++;
                    } catch (SQLiteConstraintException e) {
                        //book url is existed, update book with new info
                        String whereClause = BookConstants.URL + "=?";
                        String[] whereArgs = { v.getAsString(BookConstants.URL) };
                        db.update(BookConstants.TABLE_NAME, v, whereClause, whereArgs);
                        count++;
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            case PAGES_QUERY:
                db.beginTransaction();
                for (ContentValues v : values) {
                    try {
                        db.insertOrThrow(PageConstants.TABLE_NAME, null, v);
                        count++;
                    } catch (SQLiteConstraintException e) {
                        //page url is existed, update book with new info
                        String whereClause = PageConstants.URL + "=?";
                        String[] whereArgs = { v.getAsString(PageConstants.URL) };
                        db.update(PageConstants.TABLE_NAME, v, whereClause, whereArgs);
                        count++;
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            default:
                throw new IllegalArgumentException("Insert invalid URI " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case BOOKS_QUERY:
                cursor = db.query(BookConstants.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case ONE_BOOK_QUERY:
                cursor = db.query(BookConstants.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case PAGES_QUERY:
                cursor = db.query(PageConstants.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, null);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            default:
                throw new IllegalArgumentException("Invalid uri " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int update = -1;
        switch (sUriMatcher.match(uri)) {
            case BOOKS_QUERY:
                update = db.update(BookConstants.TABLE_NAME, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return update;
            case PAGES_QUERY:
                update = db.update(PageConstants.TABLE_NAME, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
            default:
                return update;
        }
    }
}
