package com.ntk.reactor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ntk.R;
import com.ntk.reactor.adapter.PostContentAdapter;
import com.ntk.reactor.adapter.TagAdapter;

public class ReactorContentActivity extends AppCompatActivity{

    private static final String LOG_TAG = "LOG_" + ReactorContentActivity.class.getSimpleName();
    public static final String PAGE_ARG = "PAGE";

    private PostContentAdapter mPostAdapter;
    private TagAdapter mTagAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reactor_content);

        int position = getIntent().getIntExtra(ReactorConstants.POSITION_KEY, 0);

        RecyclerView contentView = findViewById(R.id.reactor_content_view);
        mPostAdapter = new PostContentAdapter(this, position);
        contentView.setAdapter(mPostAdapter);
        LinearLayoutManager lm1 = new LinearLayoutManager(this);
        contentView.setLayoutManager(lm1);

        RecyclerView tagView = findViewById(R.id.tags_view);
        mTagAdapter = new TagAdapter(this, position);
        tagView.setAdapter(mTagAdapter);
        LinearLayoutManager lm2 = new LinearLayoutManager(this);
        lm2.setOrientation(LinearLayoutManager.HORIZONTAL);
        tagView.setLayoutManager(lm2);
    }

}
