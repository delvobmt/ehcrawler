package com.ntk.ehcrawler.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.adapters.SwipePageAdapter;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;
import com.ntk.ehcrawler.services.DatabaseService;

public class FullscreenActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private String LOG_TAG = "LOG_" + FullscreenActivity.class.getName();

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private SwipePageAdapter mAdapter;
    private String mURL;
    private ViewPager mContentView;
    private int mPosition;
    private TextView mCurrentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mURL = getIntent().getStringExtra(BookConstants.URL);
        final int bookSize = getIntent().getIntExtra(BookConstants.FILE_COUNT, 0);
        mPosition = getIntent().getIntExtra(EHConstants.POSITION, 0);

        mCurrentPage = (TextView) findViewById(R.id.current_page);

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
                String msg = String.valueOf(position+1).concat("/").concat(String.valueOf(bookSize));
                mCurrentPage.setText(msg);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mVisible = true;
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        getSupportLoaderManager().initLoader(BookProvider.PAGE_INFO_LOADER, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mVisible){
            show();
        }else {
            hide();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(LOG_TAG, keyCode + " key code" + event.getKeyCode());
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:{
                movePage(1);
            } break;
            case KeyEvent.KEYCODE_VOLUME_UP:{
                movePage(-1);
            } break;
            default:
                return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    private void movePage(int number) {
        int currentItem = mContentView.getCurrentItem();
        mContentView.setCurrentItem(currentItem + number);
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

    @Override
    protected void onStop() {
        DatabaseService.startUpdateBookPosition(this ,mURL, mPosition);
        super.onStop();
    }
}
