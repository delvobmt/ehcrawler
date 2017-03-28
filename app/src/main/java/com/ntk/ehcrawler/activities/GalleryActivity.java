package com.ntk.ehcrawler.activities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.adapters.ThumbAdapter;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;
import com.ntk.ehcrawler.services.DatabaseService;
import com.squareup.picasso.Picasso;

public class GalleryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private String mURL;
    private RecyclerView mThumbView;
    private ThumbAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        mURL = getIntent().getStringExtra(BookConstants.URL);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mThumbView = (RecyclerView) findViewById(R.id.gallery_thumbnails_rv);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        mThumbView.setLayoutManager(layoutManager);
        mAdapter = new ThumbAdapter(this);
        mThumbView.setAdapter(mAdapter);
        getSupportLoaderManager().initLoader(BookProvider.BOOK_INFO_LOADER, null, this);
        getSupportLoaderManager().initLoader(BookProvider.PAGE_INFO_LOADER, null, this);
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
                uri = BookProvider.BOOKS_CONTENT_URI;
                projection = BookConstants.PROJECTION;
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
            case BookProvider.PAGE_INFO_LOADER:
                mAdapter.changeCursor(data);
                break;
        }

    }

    private void setInfoForBook(Cursor data) {
        if(!data.moveToPosition(0)) return;
        String detail = data.getString(BookConstants.DETAIL_INDEX);
        String tags = data.getString(BookConstants.TAGS_INDEX);
        if (TextUtils.isEmpty(detail)) {
            getBookDetail();
        } else {
            final ImageView mCover = (ImageView) findViewById(R.id.cover_iv);
            final TextView mDetail = (TextView) findViewById(R.id.details_tv);
            final TextView mTags = (TextView) findViewById(R.id.tags_tv);

            final String imageSrc = data.getString(BookConstants.IMAGE_SRC_INDEX);
            mCover.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    final int width = mCover.getMeasuredWidth();
                    final int height = 16*width/9;
                    Picasso.with(mCover.getContext()).load(imageSrc).resize(width, height).centerCrop()
                            .into(mCover);
                    return true;
                }
            });

            mDetail.setText(detail);
            mTags.setText(tags);
        }
    }

    private void getBookDetail() {
        DatabaseService.startGetBookDetail(this, mURL);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
