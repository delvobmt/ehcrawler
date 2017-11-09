package com.ntk.ehcrawler.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.Book;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class DownloadService extends IntentService {
    public static final String ACTION_START = "START_DOWNLOAD";
    public static final String ACTION_STOP = "STOP_DOWNLOAD";

    private static Queue<String> waitingBooksQueue = new LinkedBlockingDeque<>();
    private static Queue<String> waitingPageQueue = new LinkedBlockingDeque<>();
    private static String pendingBook = null;
    private static List<String> pendingPage = new ArrayList<>();

    public DownloadService() {
        super("DownloadService");
    }

    public static void startDownloadBook(Context context, String bookUrl) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_START);
        intent.putExtra(BookConstants.URL, bookUrl);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            String bookUrl = intent.getStringExtra(BookConstants.URL);
            if (ACTION_START.equals(action)) {
                doStartDownloadBook(bookUrl);
            } else if (ACTION_STOP.equals(action)) {
                doStopDownloadBook(bookUrl);
            }
        }
    }

    private void doStartDownloadBook(String bookUrl) {
        if (pendingBook == null) {
            Cursor bookQuery = getContentResolver().query(BookProvider.BOOKS_CONTENT_URI, BookConstants.PROJECTION,
                    BookConstants.URL, new String[]{bookUrl}, null);
            if(bookQuery.moveToFirst()) {
                String bookId = bookQuery.getString(0);
                int totalPages = bookQuery.getInt(BookConstants.FILE_COUNT_INDEX);
                Cursor pageQuery = getContentResolver().query(BookProvider.PAGES_CONTENT_URI, PageConstants.PROJECTION,
                        PageConstants.BOOK_URL, new String[]{bookUrl}, null);
                int count = pageQuery.getCount();
                while (count < totalPages) {
                    /* get page info */
                    int pageIndex = count/ EHConstants.PAGES_PER_PAGE;
                    DatabaseUtils.getBookDetail(this, bookId, bookUrl, String.valueOf(pageIndex));


                }
            }else{
                /* TODO: db error, book not found*/
            }
        }else{
            waitingBooksQueue.add(bookUrl);
        }
    }

    private void doStopDownloadBook(String bookUrl) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
