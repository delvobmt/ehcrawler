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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.ntk.R;
import com.ntk.reactor.adapter.PostAdapter;

import org.jsoup.helper.StringUtil;

import java.util.List;

public class ReactorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Object>, View.OnClickListener {

    private static final String LOG_TAG = "LOG_" + ReactorActivity.class.getSimpleName();
    public static final String PAGE_ARG = "PAGE";
    private PostAdapter mPostAdapter;

    private static int POST_LOADER_ID = 1;
    private int mCurrentIndex = 1;
    private String mCurrentTag = "";
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reactor);
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        ContextHolder.setWidth(point.x);
        ContextHolder.setHeight(point.y);
        RecyclerView contentView = (RecyclerView) findViewById(R.id.reactor_content_view);
        mPostAdapter = new PostAdapter(this);
        contentView.setAdapter(mPostAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        contentView.setLayoutManager(layoutManager);
        contentView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page) {
                loadMore(page);
            }
        });

        findViewById(R.id.fab).setOnClickListener(this);

        mPreferences = getSharedPreferences(ReactorConstants.PREF_KEY, MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        loadPrefs();
        super.onResume();
    }

    @Override
    public Loader<Object> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<Object>(this) {
            @Override
            public Object loadInBackground() {
                return ReactorUtils.load(mCurrentTag, mCurrentIndex);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        List newPosts = (List) data;
        mPostAdapter.addPosts(newPosts);
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {
        Log.i(LOG_TAG, "Clear all posts");
        mPostAdapter.clear();
    }

    private void loadMore(final int page){
        mCurrentIndex = page + 1;
        updatePrefs();

        mPostAdapter.startLoadMore();
        Log.i(LOG_TAG, String.format("loading page %d", mCurrentIndex));
        getSupportLoaderManager().restartLoader(POST_LOADER_ID, null, this).forceLoad();
    }

    private void updatePrefs() {
        mPreferences.edit()
                .putInt(getCurrentIndexKey(), mCurrentIndex)
                .putString(ReactorConstants.TAG_KEY, mCurrentTag)
                .commit();
    }

    private void loadPrefs() {
        String nTag = mPreferences.getString(ReactorConstants.TAG_KEY, mCurrentTag);
        int nIndex = mPreferences.getInt(getCurrentIndexKey(), mCurrentIndex);
        if(!mCurrentTag.equals(nTag) || mCurrentIndex != nIndex || (mCurrentIndex == 1 && StringUtil.isBlank(mCurrentTag))) {
            mCurrentTag = nTag;
            mCurrentIndex = nIndex;
            mPostAdapter.clear();
            getSupportLoaderManager().restartLoader(POST_LOADER_ID, null, this).forceLoad();
        }
        mCurrentTag = nTag;
        mCurrentIndex = nIndex;
    }

    private String getCurrentIndexKey() {
        return (TextUtils.isEmpty(mCurrentTag))?
                ReactorConstants.INDEX_KEY
                :ReactorConstants.INDEX_KEY.concat("_").concat(mCurrentTag);
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
