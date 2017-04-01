package com.ntk.ehcrawler.activities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.adapters.PageAdapter;
import com.ntk.ehcrawler.adapters.SwipePageAdapter;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;

public class FullscreenActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private SwipePageAdapter mAdapter;
    private String mURL;
    private ViewPager mContentView;
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mURL = getIntent().getStringExtra(BookConstants.URL);
        final int bookSize = getIntent().getIntExtra(BookConstants.FILE_COUNT, 0);
        mPosition = getIntent().getIntExtra(EHConstants.POSITION, 0);

        mContentView = (ViewPager) findViewById(R.id.fullscreen_content);
        mAdapter = new SwipePageAdapter(this, getSupportFragmentManager(), bookSize);
        mContentView.setAdapter(mAdapter);
        mContentView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPosition = position;
                String msg = String.valueOf(position+1).concat(" of ").concat(String.valueOf(bookSize));
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        getSupportLoaderManager().initLoader(BookProvider.PAGE_INFO_LOADER, null, this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case BookProvider.PAGE_INFO_LOADER:
                mAdapter.changeCursor(data);
                mContentView.setCurrentItem(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
