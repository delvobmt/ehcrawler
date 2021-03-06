package com.ntk.ehcrawler.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.ntk.R;
import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.adapters.ThumbAdapter;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;
import com.ntk.ehcrawler.services.DatabaseService;
import com.ntk.ehcrawler.services.DownloadService;

public class GalleryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private String mURL;
    private RecyclerView mThumbView;
    private ThumbAdapter mAdapter;
    private String mId;
    private int mCurrentPosition;
    private boolean mNeedScroll;
    private View mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Intent intent = getIntent();
        mURL = intent.getStringExtra(BookConstants.URL);
        mId = intent.getStringExtra(BookConstants._ID);
        int bookSize = intent.getIntExtra(BookConstants.FILE_COUNT, 0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mThumbView = (RecyclerView) findViewById(R.id.gallery_thumbnails_rv);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        mThumbView.setLayoutManager(layoutManager);
        mAdapter = new ThumbAdapter(this, bookSize);
        mThumbView.setAdapter(mAdapter);

        mLoading = findViewById(R.id.loading);

        getSupportLoaderManager().initLoader(BookProvider.BOOK_INFO_LOADER, null, this);
        getSupportLoaderManager().initLoader(BookProvider.PAGE_INFO_LOADER, null, this);
        getSupportLoaderManager().initLoader(BookProvider.BOOK_STATUS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = this;
        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        switch (id){
            case BookProvider.BOOK_INFO_LOADER:
                uri = Uri.withAppendedPath(BookProvider.BOOKS_CONTENT_URI, mId);
                projection = BookConstants.PROJECTION;
                selection = BookConstants.URL.concat("=?");
                selectionArgs = new String[]{mURL};
                break;
            case BookProvider.BOOK_STATUS_LOADER:
                uri = BookProvider.BOOK_STATUS_CONTENT_URI;
                projection = BookConstants.BOOK_STATUS_PROJECTION;
                selection = BookConstants.URL.concat("=?");
                selectionArgs = new String[]{mURL};
                break;
            case BookProvider.PAGE_INFO_LOADER:
                uri = BookProvider.PAGES_CONTENT_URI;
                projection = PageConstants.PROJECTION;
                selection = PageConstants.BOOK_URL.concat("=?");
                selectionArgs = new String[]{mURL};
                break;
        }
        return new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        switch (loader.getId()){
            case BookProvider.BOOK_INFO_LOADER:
                setInfoForBook(data);
                break;
            case BookProvider.BOOK_STATUS_LOADER:
                setReadStatus(data);
                mNeedScroll = true;
                break;
            case BookProvider.PAGE_INFO_LOADER:
                setPagesInfo(data);
                break;
        }
        if(mNeedScroll){
            mThumbView.scrollToPosition(mCurrentPosition);
                    /* in case data is cleared, page data need to reload step by step
                    * we cannot need to call load new data, util it scrolls to position */
            mNeedScroll = data.getCount() < mCurrentPosition;
        }
    }

    private void setPagesInfo(Cursor data) {
        if(data.getCount()<1){
            getBookDetail();
        }else {
            mAdapter.changeCursor(data);
        }
    }

    private void setReadStatus(Cursor data) {
        if(!data.moveToFirst()) return;
        mCurrentPosition = data.getInt(BookConstants.CURRENT_POSITION_INDEX);
    }

    private void setInfoForBook(Cursor data) {
        if(!data.moveToFirst()) return;
        mId = data.getString(0);
        final String url = data.getString(BookConstants.URL_INDEX);
        final int fileCount = data.getInt(BookConstants.FILE_COUNT_INDEX);
        final String title = data.getString(BookConstants.TITLE_INDEX);
        final String detail = data.getString(BookConstants.DETAIL_INDEX);
        final String tags = data.getString(BookConstants.TAGS_INDEX);
        final Boolean isFavorite = data.getInt(BookConstants.IS_FAVORITE_INDEX)==1;

        if (!TextUtils.isEmpty(detail)) {
            final TextView mTitle = (TextView) findViewById(R.id.title_tv);
            final TextView mDetail = (TextView) findViewById(R.id.details_tv);
            final TextView mTags = (TextView) findViewById(R.id.tags_tv);
            final FloatingActionButton fabFavorite = (FloatingActionButton) findViewById(R.id.fab_favorite);
            final FloatingActionButton fabDownload = (FloatingActionButton) findViewById(R.id.fab_download);
            final FloatingActionButton fabPlay = (FloatingActionButton) findViewById(R.id.fab_play);
            fabFavorite.setOnClickListener( new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    DatabaseService.startFavoriteBook(GalleryActivity.this, mId, !isFavorite);
                    fabFavorite.setImageResource(!isFavorite ? R.drawable.ic_favorite : R.drawable.ic_not_favorite);
                }
            });
            fabFavorite.setVisibility(View.VISIBLE);
            fabFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_not_favorite);

            fabDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DownloadService.startDownloadBook(GalleryActivity.this, mURL);
                    fabDownload.setVisibility(View.GONE);
                }
            });
            fabDownload.setVisibility(View.VISIBLE);

            fabPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(GalleryActivity.this, FullscreenActivity.class);
                    intent.putExtra(BookConstants.URL, url);
                    intent.putExtra(BookConstants.FILE_COUNT, fileCount);
                    intent.putExtra(EHConstants.POSITION, mCurrentPosition);
                    startActivity(intent);
                }
            });
            fabPlay.setVisibility(View.VISIBLE);

            mTitle.setText(title);
            mDetail.setText(detail);
            mTags.setText(tags);
            mLoading.setVisibility(View.GONE);
        }
    }

    private void getBookDetail() {
        mLoading.setVisibility(View.VISIBLE);
        DatabaseService.startGetBookDetail(this, mId, mURL, 0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
