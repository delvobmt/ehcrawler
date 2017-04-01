
package com.ntk.ehcrawler.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.TheHolder;
import com.ntk.ehcrawler.adapters.BookAdapter;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.services.DatabaseService;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, MenuItem.OnMenuItemClickListener {

    private RecyclerView mBooksView;
    private RecyclerView.LayoutManager mLayoutManager;
    private BookAdapter mAdapter;
    private boolean loaded = false;
    private SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBooksView = (RecyclerView) findViewById(R.id.books_rv);
        mLayoutManager = new LinearLayoutManager(this);

        mBooksView.setLayoutManager(mLayoutManager);

        mAdapter = new BookAdapter(this);
        mBooksView.setAdapter(mAdapter);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);

        getSupportLoaderManager().initLoader(BookProvider.BOOKS_LOADER, null, this);

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        TheHolder.setWidth(point.x);
        TheHolder.setHeight(point.y);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.search);
        item.setOnMenuItemClickListener(this);
        return true;
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
        mRefreshLayout.setRefreshing(false);
        if(data == null || data.getCount() == 0){
            //There is data yet, should call request to download content from service
            getNewData();
            synchronized (mRefreshLayout) {
                mRefreshLayout.setRefreshing(true);
            }
        }else{

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void getNewData(){
        DatabaseService.startGetBook(this);
    }

    @Override
    public void onRefresh() {
        getNewData();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }
}
