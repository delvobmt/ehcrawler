package com.ntk.ehcrawler.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.BookConstants;

public class FavoriteBookFragment extends BookFragment {

    @Override
    protected void initLoader() {
        getLoaderManager().initLoader(BookProvider.FAVORITE_BOOKS_LOADER, null, this);
    }

    @Override
    protected void destroyLoader() {
        getLoaderManager().destroyLoader(BookProvider.FAVORITE_BOOKS_LOADER);
    }

    @Override
    protected void loadFilter() {
        //do nothing
    }

    @Override
    protected boolean isLoadAtEndingPage() {
        return false;
    }

    @Override
    public void onRefresh() {
        getLoaderManager().restartLoader(BookProvider.FAVORITE_BOOKS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = getContext();
        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        if (BookProvider.FAVORITE_BOOKS_LOADER == id){
            uri = BookProvider.BOOKS_CONTENT_URI;
            projection = BookConstants.PROJECTION;
            selection = BookConstants.IS_FAVORITE + "=?";
            selectionArgs = new String[]{"1"};
        }
        return new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (BookProvider.FAVORITE_BOOKS_LOADER == loader.getId()) {
            mAdapter.changeCursor(data);
        }
    }
}
