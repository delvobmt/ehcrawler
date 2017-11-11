package com.ntk.ehcrawler.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.Book;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class DownloadService extends IntentService {
    private static final String LOG_TAG = "LOG_" + DownloadManager.class.getSimpleName();

    public static final String ACTION_START = "START_DOWNLOAD";
    public static final String ACTION_STOP = "STOP_DOWNLOAD";

    private Queue<String> waitingBooksQueue = new LinkedBlockingDeque<>();
    private Queue<Page> waitingPagesQueue = new LinkedBlockingDeque<>();
    private Book pendingBook = null;

    private Map<Long, String> downloadingMap = new HashMap<>();
    private DownloadManager downloadManager = null;
    private BroadcastReceiver broadcastReceiver;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                downloadComplete(downloadId);
            }
        };
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
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
            updatePendingBook(bookUrl);
            int totalPages = pendingBook.getFileCount();
            String bookId = pendingBook.getId();

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
                waitingPagesQueue.add(new Page(pageId, pageUrl, imgSrc, nl));
            }
            while (downloadingMap.size() < 4){
                downloadNext();
            }
        }else{
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
        if(!waitingPagesQueue.isEmpty()) {
            String title = pendingBook.getTitle();
            Page page = waitingPagesQueue.poll();
            String imgSrc = page.getSrc();
            if(TextUtils.isEmpty(imgSrc)){
                imgSrc = DatabaseUtils.getPageData(this, page.getId(), page.getUrl(), page.getNewLink());
            }
            String fileName = imgSrc.substring(imgSrc.lastIndexOf("/")+1);
            Log.i(LOG_TAG, "Start downloadingMap page " + imgSrc);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imgSrc));
            request.setDestinationInExternalFilesDir(this, null , title + File.pathSeparator + fileName);
            request.setVisibleInDownloadsUi(true);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            long downloadId = downloadManager.enqueue(request);
            downloadingMap.put(downloadId, imgSrc);
        }else{
            if(pendingBook != null){
                Log.d(LOG_TAG, "download finish book " + pendingBook);
                String nextBookUrl = waitingBooksQueue.poll();
                if (nextBookUrl != null) {
                    updatePendingBook(nextBookUrl);
                }

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
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void downloadComplete(long downloadId) {
        String imgSrc = downloadingMap.remove(downloadId);
        Log.d(LOG_TAG, "Download complete " + imgSrc);
        downloadNext();
    }

    class Page{
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
    }
}
