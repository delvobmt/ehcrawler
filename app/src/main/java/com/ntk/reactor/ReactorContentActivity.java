package com.ntk.reactor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ntk.R;
import com.ntk.reactor.adapter.PostContentAdapter;
import com.ntk.reactor.adapter.TagAdapter;
import com.ntk.reactor.database.PostDatabaseHelper;
import com.ntk.reactor.model.Post;

import java.util.List;

public class ReactorContentActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Post> {

    private static final String LOG_TAG = "LOG_" + ReactorContentActivity.class.getSimpleName();
    private static final int POST_LOADER = 2;

    private PostContentAdapter mPostAdapter;
    private TagAdapter mTagAdapter;
    private SharedPreferences mPreferences;
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reactor_content);

        mPosition = getIntent().getIntExtra(ReactorConstants.POSITION_KEY, 0);
        mPreferences = getSharedPreferences(ReactorConstants.PREF_KEY, MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView contentView = findViewById(R.id.reactor_content_view);
        mPostAdapter = new PostContentAdapter(this, mPosition);
        contentView.setAdapter(mPostAdapter);
        LinearLayoutManager lm1 = new LinearLayoutManager(this);
        contentView.setLayoutManager(lm1);
        contentView.addOnScrollListener(new CloseOnEndRecyclerViewScrollListener(lm1) {
            @Override
            public void onTheEnd() {
                finish();
            }
        });

        RecyclerView tagView = findViewById(R.id.tags_view);
        mTagAdapter = new TagAdapter(this, mPosition, this);
        tagView.setAdapter(mTagAdapter);
        LinearLayoutManager lm2 = new LinearLayoutManager(this);
        lm2.setOrientation(LinearLayoutManager.HORIZONTAL);
        tagView.setLayoutManager(lm2);

        Post post = PostDatabaseHelper.getPostAt(mPosition);
        if(post != null && !post.isLoaded()){
            getSupportLoaderManager().initLoader(POST_LOADER, null, this).forceLoad();
        }
    }

    @Override
    public void onClick(View v) {
        String tag = v.getTag().toString();
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString(ReactorConstants.TAG_KEY, tag);
        edit.commit();
        Log.i(LOG_TAG, String.format("click on tag %s", tag));
        finish();
    }

    @Override
    public Loader<Post> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Post>(this) {
            @Override
            public Post loadInBackground() {
                return ReactorUtils.loadPost(PostDatabaseHelper.getPostAt(mPosition));
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Post> loader, Post post) {
        PostDatabaseHelper.updatePost(post, mPosition);
        mPostAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Post> loader) {

    }
}
