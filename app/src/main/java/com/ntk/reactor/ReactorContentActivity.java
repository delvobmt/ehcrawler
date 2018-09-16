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
import com.ntk.reactor.adapter.PostContentAdapter;

import java.util.List;

public class ReactorContentActivity extends AppCompatActivity{

    private static final String LOG_TAG = "LOG_" + ReactorContentActivity.class.getSimpleName();
    public static final String PAGE_ARG = "PAGE";

    private PostContentAdapter mPostAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reactor);

        int position = getIntent().getIntExtra(ReactorConstants.POSITION_KEY, 0);

        RecyclerView contentView = (RecyclerView) findViewById(R.id.reactor_content_view);
        mPostAdapter = new PostContentAdapter(this, position);
        contentView.setAdapter(mPostAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        contentView.setLayoutManager(layoutManager);
    }

}
