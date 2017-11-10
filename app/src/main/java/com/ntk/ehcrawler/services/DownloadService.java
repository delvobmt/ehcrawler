package com.ntk.ehcrawler.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class DownloadService extends IntentService {
    private static final String LOG_TAG = "LOG_" + DownloadManager.class.getSimpleName();

    public static final String ACTION_START = "START_DOWNLOAD";
    public static final String ACTION_STOP = "STOP_DOWNLOAD";

    private static Queue<String> waitingBooksQueue = new LinkedBlockingDeque<>();
    private static Queue<String> waitingPagesQueue = new LinkedBlockingDeque<>();
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
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (pendingBook == null) {
            Cursor bookQuery = bookQuery(bookUrl);
            if(bookQuery.moveToFirst()) {
                String bookId = bookQuery.getString(0);
                String title = bookQuery.getString(BookConstants.TITLE_INDEX);
                int totalPages = bookQuery.getInt(BookConstants.FILE_COUNT_INDEX);

                Cursor pageQuery = pageQuery(bookUrl);
                int count = pageQuery.getCount();
                while (count < totalPages) {
                    /* get page info */
                    int pageIndex = count/ EHConstants.PAGES_PER_PAGE;
                    DatabaseUtils.getBookDetail(this, bookId, bookUrl, pageIndex);
                    pageQuery.close();
                    pageQuery = pageQuery(bookUrl);
                    count = pageQuery.getCount();
                }
                /* get page src */
                while (pageQuery.moveToNext()){
                    String pageId = pageQuery.getString(0);
                    String imgSrc = pageQuery.getString(PageConstants.SRC_INDEX);
                    String pageUrl = pageQuery.getString(PageConstants.URL_INDEX);
                    String nl = pageQuery.getString(PageConstants.NEWLINK_INDEX);
                    if(TextUtils.isEmpty(imgSrc)){
                        imgSrc = DatabaseUtils.getPageData(this, pageId, pageUrl, nl);
                    }
                    waitingPagesQueue.add(imgSrc);
                }
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterByStatus(DownloadManager.STATUS_PENDING);
                while (!waitingPagesQueue.isEmpty()){
                    int pendingCount = downloadManager.query(query).getCount();
                    if(pendingCount <= 4){
                        String imgSrc = waitingPagesQueue.poll();
                            Log.i(LOG_TAG, "Start download page " + imgSrc);
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imgSrc));
                        String fileName = imgSrc.substring(imgSrc.lastIndexOf("/"));
                        request.setDestinationInExternalFilesDir(this, null, title + File.pathSeparator + fileName);
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        downloadManager.enqueue(request);
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Log.d(LOG_TAG, "error while downloading files" + e.getMessage());
                        }
                    }
                }

            }else{
                /* TODO: db error, book not found*/
            }
        }else{
            waitingBooksQueue.add(bookUrl);
        }
    }

    private Cursor bookQuery(String bookUrl) {
        return getContentResolver().query(BookProvider.BOOKS_CONTENT_URI, BookConstants.PROJECTION,
                BookConstants.URL + "=?", new String[]{bookUrl}, null);
    }

    private Cursor pageQuery(String bookUrl) {
        return getContentResolver().query(BookProvider.PAGES_CONTENT_URI, PageConstants.PROJECTION,
                PageConstants.BOOK_URL + "=?", new String[]{bookUrl}, null);
    }

    private void doStopDownloadBook(String bookUrl) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
