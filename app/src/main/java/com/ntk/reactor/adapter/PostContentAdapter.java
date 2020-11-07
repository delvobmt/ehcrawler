package com.ntk.reactor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.PlayerView;
import com.ntk.R;
import com.ntk.reactor.GlideApp;
import com.ntk.reactor.ReactorContentActivity;
import com.ntk.reactor.database.PostDatabaseHelper;
import com.ntk.reactor.model.Content;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostContentAdapter extends AbstractPostAdapter implements View.OnClickListener {

    private final String LOG_TAG = "LOG_" + PostContentAdapter.class.getSimpleName();

    List<Content> mContents;

    public PostContentAdapter(Context context, int position) {
        super(context);
        mContents = PostDatabaseHelper.getPostContentsAt(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case TYPE_PROGRESS:{
                View view = LayoutInflater.from(mContext).inflate(R.layout.close_item_recyclerview_bottom, parent, false);
                view.setOnClickListener(this);
                return new BottomViewHolder(view);
            }
            case TYPE_ITEM: {
                View view = LayoutInflater.from(mContext).inflate(R.layout.post_view, parent, false);
                return new ItemViewHolder(view);
            }
            default:
                throw new IllegalArgumentException(String.format("viewType %d is not supported", viewType));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof ItemViewHolder) {
            final View view = holder.itemView;
            final ImageView imageView = view.findViewById(R.id.image_iv);
            final TextView textView = view.findViewById(R.id.gif_text);
            final ProgressBar progressView = view.findViewById(R.id.progress);
            progressView.setVisibility(View.VISIBLE);
            final PlayerView videoView = view.findViewById(R.id.video_view);
            GlideApp.with(mContext).clear(imageView);
            Picasso.with(mContext).cancelRequest(imageView);
            imageView.setImageDrawable(null);
            final Content content = mContents.get(position);
            processContent(imageView, videoView, textView, progressView, content);
        }
    }

    @Override
    public int getItemCount() {
        return mContents.size() + 1;
    }

    @Override
    public void onClick(View view) {
        if(mContext.getClass().equals(ReactorContentActivity.class)){
            ((ReactorContentActivity)mContext).finish();
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        ItemViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class BottomViewHolder extends RecyclerView.ViewHolder {

        BottomViewHolder(View itemView) {
            super(itemView);
        }
    }
}
