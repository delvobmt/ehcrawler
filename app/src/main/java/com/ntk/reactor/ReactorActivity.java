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

public class ReactorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Object>{

    private static final String LOG_TAG = "LOG_" + ReactorActivity.class.getSimpleName();
    public static final String PAGE_ARG = "PAGE";
    private static final String CURRENT_TAG = null;

    private PostAdapter mPostAdapter;
    private static int POST_LOADER_ID = 1;

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

        Bundle args = new Bundle();
        POST_LOADER_ID = 1;
        args.putInt(PAGE_ARG, POST_LOADER_ID);

        getSupportLoaderManager().initLoader(POST_LOADER_ID, args, this).forceLoad();

    }

    @Override
    public Loader<Object> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<Object>(this) {
            @Override
            public Object loadInBackground() {
                return ReactorUtils.load(CURRENT_TAG, args.getInt(PAGE_ARG,0));
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
        mPostAdapter.startLoadMore();
        Log.i(LOG_TAG, String.format("loading page %d", page+1));
        Bundle args = new Bundle();
        args.putInt(PAGE_ARG, page+1);
        getSupportLoaderManager().restartLoader(POST_LOADER_ID, args, this).forceLoad();


        // example read end
//        if(page == 3){
//            mPostAdapter.onReachEnd();
//            return;
//        }

        // start load more
    }
}
