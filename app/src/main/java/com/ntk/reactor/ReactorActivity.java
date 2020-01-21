package com.ntk.reactor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;

import com.liulishuo.filedownloader.FileDownloader;
import com.ntk.R;
import com.ntk.reactor.adapter.PostAdapter;
import com.ntk.reactor.database.PostDatabaseHelper;

import org.jsoup.helper.StringUtil;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReactorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List>, View.OnClickListener {

    private static final String LOG_TAG = "LOG_" + ReactorActivity.class.getSimpleName();
    public static final String PAGE_ARG = "PAGE";
    private PostAdapter mPostAdapter;

    private static int POST_LOADER_ID = 1;
    private int mCurrentIndex = 1;
    private String mCurrentTag = "";
    private SharedPreferences mPreferences;
    private int mMaxPage;
    private boolean mLoading;

    private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
    private RecyclerView mContentView;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileDownloader.setup(this);
        setContentView(R.layout.activity_reactor);
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        ContextHolder.setWidth(point.x);
        ContextHolder.setHeight(point.y);
        mContentView = findViewById(R.id.reactor_content_view);
        mPostAdapter = new PostAdapter(this);
        mContentView.setAdapter(mPostAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mContentView.setLayoutManager(mLayoutManager);
        mContentView.addOnScrollListener(new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page) {
                if(!mLoading)
                    loadMore(page);
            }
        });


        findViewById(R.id.fab).setOnClickListener(this);

        mPreferences = getSharedPreferences(ReactorConstants.PREF_KEY, MODE_PRIVATE);

        purge();
    }

    private void purge() {
        String filesDir = getFilesDir().getAbsolutePath();
        File parentFileDir = new File(filesDir);
        for (File file : parentFileDir.listFiles()) {
            long oldTime = System.currentTimeMillis() - file.lastModified();
            if(oldTime > TimeUnit.DAYS.toMillis(1)) {
                file.delete();
            }
        }
    }

    @Override
    protected void onResume() {
        loadPrefs();
        mPostAdapter.notifyDataSetChanged();
        super.onResume();
        reloadActiveVideo();
    }

    private void reloadActiveVideo() {
        if(!PostDatabaseHelper.isEmpty()){
            // need to call this method from list view handler in order to have filled list
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public Loader<List> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<List>(this) {
            @Override
            public List loadInBackground() {
                return ReactorUtils.load(mCurrentTag, mCurrentIndex);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List> loader, List data) {
        if(data!=null && !data.isEmpty()) {
            List newPosts = (List) data.get(0);
            mMaxPage = (int) data.get(1);
            mPostAdapter.addPosts(newPosts);
            mLoading = false;
            reloadActiveVideo();
        }
    }

    @Override
    public void onLoaderReset(Loader<List> loader) {
        Log.i(LOG_TAG, "Clear all posts");
        mPostAdapter.clear();
    }

    private void loadMore(final int page) {
        if (mCurrentIndex < mMaxPage) {
            Log.i(LOG_TAG, String.format("load page %s", page));
            mCurrentIndex++;
            updatePrefs();

            mPostAdapter.startLoadMore();
            Log.i(LOG_TAG, String.format("loading page %d", mCurrentIndex));
            mLoading = true;
            getSupportLoaderManager().restartLoader(POST_LOADER_ID, null, this).forceLoad();
        } else {
            mPostAdapter.onReachEnd();
        }
    }

    private void updatePrefs() {
        mPreferences.edit()
                .putInt(ReactorUtils.getCurrentIndexKey(mCurrentTag), mCurrentIndex-1)
                .putString(ReactorConstants.TAG_KEY, mCurrentTag)
                .commit();
    }

    private void loadPrefs() {
        String nTag = mPreferences.getString(ReactorConstants.TAG_KEY, mCurrentTag);
        int nIndex = mPreferences.getInt(ReactorUtils.getCurrentIndexKey(nTag), mCurrentIndex);
        if (!mCurrentTag.equals(nTag) ||
                Math.abs(mCurrentIndex - nIndex) > 1 ||
                (mCurrentIndex == 1 && StringUtil.isBlank(mCurrentTag) && PostDatabaseHelper.isEmpty())) {
            mCurrentTag = nTag;
            mCurrentIndex = nIndex > 1 ? nIndex : nIndex;
            mPostAdapter.clear();
            mLoading = true;
            getSupportLoaderManager().restartLoader(POST_LOADER_ID, null, this).forceLoad();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                Intent intent = new Intent(this, com.ntk.reactor.SearchActivity.class);
                startActivity(intent);
                break;
        }
    }
}
