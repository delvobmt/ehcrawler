package com.ntk.reactor;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.ntk.R;
import com.ntk.reactor.adapter.PostAdapter;

import java.util.List;

public class ReactorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Object> {

    private static final String LOG_TAG = "LOG_" + ReactorActivity.class.getSimpleName();

    private PostAdapter mPostAdapter;

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
        contentView.setLayoutManager(new LinearLayoutManager(this));

        getSupportLoaderManager().initLoader(0, null, this).forceLoad();

    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Object>(this) {
            @Override
            public Object loadInBackground() {
                return ReactorUtils.load(null, 0);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        List newPosts = (List) data;
        Log.i(LOG_TAG, String.format("Add %d new posts", newPosts.size()));
        mPostAdapter.addPosts(newPosts);
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {
        Log.i(LOG_TAG, "Clear all posts");
        mPostAdapter.clear();
    }
}
