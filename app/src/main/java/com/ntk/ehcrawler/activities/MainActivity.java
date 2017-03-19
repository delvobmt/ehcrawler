package com.ntk.ehcrawler.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.adapters.BookAdapter;
import com.ntk.ehcrawler.model.BookConstants;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mBooksView;
    private RecyclerView.LayoutManager mLayoutManager;
    private BookAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBooksView = (RecyclerView) findViewById(R.id.books_rv);
        mLayoutManager = new LinearLayoutManager(this);

        mBooksView.setLayoutManager(mLayoutManager);

        mAdapter = new BookAdapter(this, null);
        mBooksView.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = this;
        Uri uri = null;
        String[] projection = BookConstants.PROJECTION;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
