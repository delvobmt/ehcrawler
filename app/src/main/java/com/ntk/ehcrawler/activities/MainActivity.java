
package com.ntk.ehcrawler.activities;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.TheHolder;
import com.ntk.ehcrawler.adapters.BookAdapter;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.services.DatabaseService;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mBooksView;
    private RecyclerView.LayoutManager mLayoutManager;
    private BookAdapter mAdapter;
    private boolean loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBooksView = (RecyclerView) findViewById(R.id.books_rv);
        mLayoutManager = new LinearLayoutManager(this);

        mBooksView.setLayoutManager(mLayoutManager);

        mAdapter = new BookAdapter(this);
        mBooksView.setAdapter(mAdapter);
        getSupportLoaderManager().initLoader(BookProvider.BOOKS_LOADER, null, this);

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        TheHolder.setWidth(point.x);
        TheHolder.setHeight(point.y);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = this;
        Uri uri = BookProvider.BOOKS_CONTENT_URI;
        String[] projection = BookConstants.PROJECTION;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        return new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
        if(loaded){
            //prevent second load
            return;
        }
        if(data == null || data.getCount() == 0){
            //There is data yet, should call request to download content from service
            getNewData();
        }else{

        }
        loaded = true;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void getNewData(){
        DatabaseService.startGetBook(this);
    }

}
