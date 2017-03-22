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
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.BookConstants;
import com.squareup.picasso.Picasso;

public class GalleryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private String mURL;

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

        getSupportLoaderManager().initLoader(BookProvider.BOOK_INFO_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = this;
        Uri uri = BookProvider.BOOKS_CONTENT_URI;
        String[] projection = BookConstants.PROJECTION;
        String selection = BookConstants.URL.concat("=?");
        String[] selectionArgs = {mURL};
        String sortOrder = null;
        return new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ImageView mCover = (ImageView) findViewById(R.id.cover_iv);
        if(data.moveToNext()) {
            String imageSrc = data.getString(BookConstants.IMAGE_SRC_INDEX);
            Picasso.with(this).load(imageSrc).into(mCover);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
