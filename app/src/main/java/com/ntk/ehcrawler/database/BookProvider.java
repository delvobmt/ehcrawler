package com.ntk.ehcrawler.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;

public class BookProvider extends ContentProvider {
    private static final String AUTHORITY = "com.ntk.ehcrawler.providers";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final Uri BOOKS_CONTENT_URI = Uri.withAppendedPath(
            CONTENT_URI, BookConstants.TABLE_NAME);
    public static final Uri FAVORITE_BOOKS_CONTENT_URI = Uri.withAppendedPath(
            CONTENT_URI, BookConstants.TABLE_FAVORITE_NAME);
    public static final Uri PAGES_CONTENT_URI = Uri.withAppendedPath(
            CONTENT_URI, PageConstants.TABLE_NAME);

    private static final int BOOKS_QUERY = 1;
    private static final int PAGES_QUERY = 2;
    private static final int ONE_BOOK_QUERY = 3;
    private static final int ONE_PAGE_QUERY = 4;
    private static final int FAVORITE_BOOKS_QUERY = 5;
    private static final int ONE_FAVORITE_BOOK_QUERY = 6;

    private static final UriMatcher sUriMatcher;
    private static final SparseArray<String> sMimeTypes;
    public static final int BOOKS_LOADER = 0;
    public static final int BOOK_INFO_LOADER = 1;
    public static final int PAGE_INFO_LOADER = 2;
    public static final int FAVORITE_BOOKS_LOADER = 3;

    static {
        sUriMatcher = new UriMatcher(0);
        sUriMatcher.addURI(AUTHORITY, BookConstants.TABLE_NAME, BOOKS_QUERY);
        sUriMatcher.addURI(AUTHORITY, PageConstants.TABLE_NAME, PAGES_QUERY);
        sUriMatcher.addURI(AUTHORITY, BookConstants.TABLE_NAME+"/#", ONE_BOOK_QUERY);
        sUriMatcher.addURI(AUTHORITY, PageConstants.TABLE_NAME+"/#", ONE_PAGE_QUERY);
        sUriMatcher.addURI(AUTHORITY, BookConstants.TABLE_FAVORITE_NAME, FAVORITE_BOOKS_QUERY);
        sUriMatcher.addURI(AUTHORITY, BookConstants.TABLE_FAVORITE_NAME+"/#", ONE_FAVORITE_BOOK_QUERY);

        sMimeTypes = new SparseArray<>();
        sMimeTypes.put(BOOKS_QUERY, "vnd.android.cursor.dir/vnd." + AUTHORITY
                + "." + BookConstants.TABLE_NAME);
        sMimeTypes.put(PAGES_QUERY, "vnd.android.cursor.dir/vnd." + AUTHORITY
                + "." + PageConstants.TABLE_NAME);
        sMimeTypes.put(ONE_BOOK_QUERY, "vnd.android.cursor.item/vnd." + AUTHORITY
                + "." + BookConstants.TABLE_NAME);
        sMimeTypes.put(ONE_PAGE_QUERY, "vnd.android.cursor.item/vnd." + AUTHORITY
                + "." + PageConstants.TABLE_NAME);
        sMimeTypes.put(BOOKS_QUERY, "vnd.android.cursor.dir/vnd." + AUTHORITY
                + "." + BookConstants.TABLE_FAVORITE_NAME);
        sMimeTypes.put(ONE_BOOK_QUERY, "vnd.android.cursor.item/vnd." + AUTHORITY
                + "." + BookConstants.TABLE_FAVORITE_NAME);
    }

    private DatabaseHelper mDBHelper;
    private SQLiteDatabase readableDB;
    private SQLiteDatabase writableDB;

    public BookProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case BOOKS_QUERY:
                return writableDB.delete(BookConstants.TABLE_NAME, selection, selectionArgs);
            case PAGES_QUERY:
                return writableDB.delete(PageConstants.TABLE_NAME, selection, selectionArgs);
            case FAVORITE_BOOKS_QUERY:
                return writableDB.delete(BookConstants.TABLE_FAVORITE_NAME, selection, selectionArgs);
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
        switch (sUriMatcher.match(uri)) {
            case FAVORITE_BOOKS_QUERY:
                try {
                    Object[] id = {values.getAsString(BookConstants._ID)};
                    writableDB.execSQL(DatabaseHelper.SQL_FAVORITE_BOOK, id);
                }catch (SQLiteException e){
                    Log.e("BookProvider", "Error while insert Favorite book", e);
                }
                break;
            default:
                throw new IllegalArgumentException("Insert invalid URI " + uri);
        }
        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case BOOKS_QUERY:
                writableDB.beginTransaction();
                for (ContentValues v : values) {
                    try {
                        writableDB.insertOrThrow(BookConstants.TABLE_NAME, null, v);
                        count++;
                    } catch (SQLiteConstraintException e) {
                        //book url is existed, update book with new info
                        String whereClause = BookConstants.URL + "=?";
                        String[] whereArgs = { v.getAsString(BookConstants.URL) };
                        writableDB.update(BookConstants.TABLE_NAME, v, whereClause, whereArgs);
                        count++;
                    }
                }
                writableDB.setTransactionSuccessful();
                writableDB.endTransaction();
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            case PAGES_QUERY:
                writableDB.beginTransaction();
                for (ContentValues v : values) {
                    try {
                        writableDB.insertOrThrow(PageConstants.TABLE_NAME, null, v);
                        count++;
                    } catch (SQLiteConstraintException e) {
                        //page url is existed, update book with new info
                        String whereClause = PageConstants.URL + "=?";
                        String[] whereArgs = { v.getAsString(PageConstants.URL) };
                        writableDB.update(PageConstants.TABLE_NAME, v, whereClause, whereArgs);
                        count++;
                    }
                }
                writableDB.setTransactionSuccessful();
                writableDB.endTransaction();
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            default:
                throw new IllegalArgumentException("Insert invalid URI " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new DatabaseHelper(getContext());
        readableDB = mDBHelper.getReadableDatabase();
        writableDB = mDBHelper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case BOOKS_QUERY:
                cursor = readableDB.query(BookConstants.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case ONE_BOOK_QUERY:
                cursor = readableDB.query(BookConstants.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder,"1");
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case FAVORITE_BOOKS_QUERY:
                cursor = readableDB.query(BookConstants.TABLE_FAVORITE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case ONE_FAVORITE_BOOK_QUERY:
                cursor = readableDB.query(BookConstants.TABLE_FAVORITE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder,"1");
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case PAGES_QUERY:
                cursor = readableDB.query(PageConstants.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case ONE_PAGE_QUERY:
                cursor = readableDB.query(PageConstants.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder,"1");
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            default:
                throw new IllegalArgumentException("Invalid uri " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int update = -1;
        switch (sUriMatcher.match(uri)) {
            case BOOKS_QUERY:
            case ONE_BOOK_QUERY:
                update = writableDB.update(BookConstants.TABLE_NAME, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return update;
            case PAGES_QUERY:
            case ONE_PAGE_QUERY:
                update = writableDB.update(PageConstants.TABLE_NAME, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
            default:
                return update;
        }
    }
}
