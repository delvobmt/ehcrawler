package com.ntk.ehcrawler.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.Book;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class DownloadService extends IntentService {
    private static final String LOG_TAG = "LOG_" + DownloadManager.class.getSimpleName();

    public static final String ACTION_START = "ACTION_START_DOWNLOAD";
    public static final String ACTION_STOP = "ACTION_STOP_DOWNLOAD";
    public static final String ACTION_DOWNLOAD_NEXT = "ACTION_DOWNLOAD_NEXT";

    private static Queue<String> waitingBooksQueue = new LinkedBlockingDeque<>();
    private static Queue<Page> waitingPagesQueue = new LinkedBlockingDeque<>();
    private static Book pendingBook = null;

    private static Map<Long, Page> downloadingMap = new HashMap<>();
    private static DownloadManager downloadManager = null;
    private static BroadcastReceiver.PendingResult pendingResult;
    private static BroadcastReceiver broadcastReceiver;

    public DownloadService() {
        super("DownloadService");
    }

    public static void startDownloadBook(Context context, String bookUrl) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_START);
        intent.putExtra(BookConstants.URL, bookUrl);
        context.startService(intent);
    }

    public static void stopDownloadBook(Context context, String bookUrl) {

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isNetworkAvailable()){
            Log.d(LOG_TAG, "Network is not available");
            Toast.makeText(this, "Network is not available", Toast.LENGTH_SHORT).show();
            clear();
            return;
        }
        if (intent != null) {
            final String action = intent.getAction();
            String bookUrl = intent.getStringExtra(BookConstants.URL);
            if (ACTION_START.equals(action)) {
                doStartDownloadBook(bookUrl);
            } else if (ACTION_DOWNLOAD_NEXT.equals(action)) {
                String pageId = intent.getStringExtra(PageConstants._ID);
                if (TextUtils.isEmpty(pageId)) {
                    downloadNext();
                } else {
                    reDownload(pageId);
                }

            }
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private void prepare() {
        if (downloadManager == null)
            downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (broadcastReceiver == null) {
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    downloadComplete(downloadId);
                    pendingResult = broadcastReceiver.goAsync();
                }
            };
            getApplicationContext().registerReceiver(broadcastReceiver, filter);
        }
    }

    private void clear() {
        Log.d(LOG_TAG, "clear download service!");
        pendingBook = null;
        if(pendingBook!=null) {
            pendingResult.finish();
        }
        downloadManager = null;
        if (broadcastReceiver != null) {
            getApplicationContext().unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
    }

    private void doStartDownloadBook(String bookUrl) {
        prepare();
        if (pendingBook == null) {
            updatePendingBook(bookUrl);
            Log.d(LOG_TAG, "start download book " + pendingBook.getTitle());
            int totalPages = pendingBook.getFileCount();
            String bookId = pendingBook.getId();

            Cursor pageQuery = pageQuery(bookUrl);
            int count = pageQuery.getCount();
            while (count < totalPages) {
                /* get page info */
                int pageIndex = count / EHConstants.PAGES_PER_PAGE;
                DatabaseUtils.getBookDetail(this, bookId, bookUrl, pageIndex);
                pageQuery.close();
                pageQuery = pageQuery(bookUrl);
                count = pageQuery.getCount();
            }
            /* get page src */
            while (pageQuery.moveToNext()) {
                String pageId = pageQuery.getString(0);
                String imgSrc = pageQuery.getString(PageConstants.SRC_INDEX);
                String pageUrl = pageQuery.getString(PageConstants.URL_INDEX);
                String nl = pageQuery.getString(PageConstants.NEWLINK_INDEX);
                waitingPagesQueue.add(new Page(pageId, pageUrl, imgSrc, nl));
            }
            while (downloadingMap.size() < 4) {
                downloadNext();
            }
        } else {
            waitingBooksQueue.add(bookUrl);
        }
    }

    private void updatePendingBook(String bookUrl) {
        pendingBook = new Book();
        Cursor bookQuery = bookQuery(bookUrl);
        if (!bookQuery.moveToFirst()) {
            return;
        }
        String bookId = bookQuery.getString(0);
        String title = bookQuery.getString(BookConstants.TITLE_INDEX);
        int totalPages = bookQuery.getInt(BookConstants.FILE_COUNT_INDEX);
        pendingBook.setId(bookId);
        pendingBook.setUrl(bookUrl);
        pendingBook.setTitle(title);
        pendingBook.setFileCount(totalPages);
    }

    private void downloadNext() {
        if (!waitingPagesQueue.isEmpty()) {
            String title = pendingBook.getTitle();
            Page page = waitingPagesQueue.poll();
            String imgSrc = page.getSrc();
            if (TextUtils.isEmpty(imgSrc)) {
                ContentValues cv = null;
                try {
                    cv = DatabaseUtils.getPageData(this, page.getId(), page.getUrl(), page.getNewLink());
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getLocalizedMessage() );
                    return;
                }
                imgSrc = cv.getAsString(PageConstants.SRC);
                page.setSrc(imgSrc);
                page.setNewLink(cv.getAsString(PageConstants.NEWLINK));
            }
            String fileName = imgSrc.substring(imgSrc.lastIndexOf("/") + 1);
            Uri uri = Uri.parse(imgSrc);
            if(!uri.getScheme().equals("file")) {
                Log.i(LOG_TAG, "Start download page " + imgSrc);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setDestinationInExternalFilesDir(this, null, File.separator + title + File.separator + fileName);
                request.setVisibleInDownloadsUi(true);
                long downloadId = downloadManager.enqueue(request);
                downloadingMap.put(downloadId, page);
            }
        } else {
            if (pendingBook != null && downloadingMap.isEmpty()) {
                Log.d(LOG_TAG, "download finish " + pendingBook);
                String nextBookUrl = waitingBooksQueue.poll();
                if (nextBookUrl != null) {
                    pendingBook = null;
                    doStartDownloadBook(nextBookUrl);
                } else {
                    clear();
                }
            }
        }
    }

    private void reDownload(String pageId) {
        String title = pendingBook.getTitle();
        /* get new image source */
        Cursor cursor = getContentResolver().query(Uri.withAppendedPath(BookProvider.PAGES_CONTENT_URI, pageId),
                new String[]{PageConstants.NEWLINK, PageConstants.URL}, null, null, null);
        if (cursor.moveToFirst()) {
            Page page = new Page(pageId, null, null, null);
            page.setNewLink(cursor.getString(0));
            page.setUrl(cursor.getString(1));
            /* check in downloading map */
            if(!downloadingMap.containsValue(page)) {
                ContentValues values = null;
                try {
                    values = DatabaseUtils.getPageData(this, page.getId(), page.getUrl(), page.getNewLink());
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getLocalizedMessage());
                    return;
                }
                String imgSrc = values.getAsString(PageConstants.SRC);
                page.setSrc(imgSrc);
                String fileName = imgSrc.substring(imgSrc.lastIndexOf("/") + 1);
                Log.i(LOG_TAG, "re-download page " + fileName);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imgSrc));
                request.setDestinationInExternalFilesDir(this, null, File.separator + title + File.separator + fileName);
                request.setVisibleInDownloadsUi(true);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                long downloadId = downloadManager.enqueue(request);
                downloadingMap.put(downloadId, page);
            }
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
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void downloadComplete(final long downloadId) {
        Page page = downloadingMap.remove(downloadId);
        if(page == null){
            Log.d(LOG_TAG, downloadId + " was processed!");
        }
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int status_index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(status_index);
            switch (status) {
                case DownloadManager.STATUS_SUCCESSFUL: {
                    Log.d(LOG_TAG, "Download successfully " + page.getSrc());
                    int localUri_index = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                    String localUrl = cursor.getString(localUri_index);
                    ContentValues values = new ContentValues();
                    values.put(PageConstants.SRC, localUrl);
                    getContentResolver().update(BookProvider.PAGES_CONTENT_URI, values, PageConstants._ID+"=?", new String[]{page.getId()});
                    if (pendingBook != null) {
                        Intent intent = new Intent(DownloadService.this, DownloadService.class);
                        intent.setAction(ACTION_DOWNLOAD_NEXT);
                        startService(intent);
                    }
                    break;
                }
                case DownloadManager.STATUS_FAILED: {
                    Log.d(LOG_TAG, "Download failed " + page.getId() + " - " + page.getSrc());
                    Intent intent = new Intent(DownloadService.this, DownloadService.class);
                    intent.setAction(ACTION_DOWNLOAD_NEXT);
                    intent.putExtra(PageConstants._ID, page.getId());
                    startService(intent);
                    break;
                }
            }
        }
        cursor.close();
    }

    class Page {
        String id;
        String url;
        String src;
        String nl;

        public Page(String id, String url, String src, String nl) {
            this.id = id;
            this.url = url;
            this.src = src;
            this.nl = nl;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public String getNewLink() {
            return nl;
        }

        public void setNewLink(String nl) {
            this.nl = nl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Page page = (Page) o;

            return id != null ? id.equals(page.id) : page.id == null;
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }
    }
}
