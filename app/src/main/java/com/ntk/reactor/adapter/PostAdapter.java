package com.ntk.reactor.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.PlayerView;
import com.ntk.R;
import com.ntk.reactor.GlideApp;
import com.ntk.reactor.ReactorConstants;
import com.ntk.reactor.ReactorContentActivity;
import com.ntk.reactor.database.PostDatabaseHelper;
import com.ntk.reactor.model.Content;
import com.ntk.reactor.model.Post;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PostAdapter extends AbstractPostAdapter {

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_PROGRESS = 0;
    private boolean mOnLoadMoreFailed;
    private boolean mIsReachEnd;

    private final String LOG_TAG = "LOG_" + PostAdapter.class.getSimpleName();

    public PostAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_PROGRESS: {
                View view = LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview_bottom, parent, false);
                return new BottomViewHolder(view);
            }
            case TYPE_ITEM: {
                View view = LayoutInflater.from(mContext).inflate(R.layout.post_view, parent, false);
                View videoView = view.findViewById(R.id.video_view);
                view.setTag(videoView);
                return new ItemViewHolder(view);
            }
            default:
                throw new IllegalArgumentException(String.format("viewType %d is not supported", viewType));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == bottomItemPosition()) {
            return TYPE_PROGRESS;
        }
        return TYPE_ITEM;
    }

    private int bottomItemPosition() {
        return getItemCount() - 1;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof BottomViewHolder) {
            final View progressBar = holder.itemView.findViewById(R.id.progress);
            //            View buttonRetry = holder.itemView.findViewById(R.id.button_retry);
            final View layoutRetry = holder.itemView.findViewById(R.id.layout_retry);
            final View textNoMoreItem = holder.itemView.findViewById(R.id.text_no_more_item);
            textNoMoreItem.setVisibility(
                    mIsReachEnd ? View.VISIBLE : View.GONE);

            progressBar.setVisibility(
                    mIsReachEnd ? View.GONE : mOnLoadMoreFailed ? View.GONE : View.VISIBLE);
            layoutRetry.setVisibility(
                    mIsReachEnd ? View.GONE : mOnLoadMoreFailed ? View.VISIBLE : View.GONE);
        } else if (holder instanceof ItemViewHolder) {
            final View view = holder.itemView;
            final ImageView imageView = view.findViewById(R.id.image_iv);
            final TextView textView = view.findViewById(R.id.gif_text);
            final ProgressBar progress = view.findViewById(R.id.progress);
            final Post post = PostDatabaseHelper.getPostAt(position);
            final View showMoreView = view.findViewById(R.id.show_more_view);
            final TextView commentText = view.findViewById(R.id.comment_text);
            final PlayerView videoView = view.findViewById(R.id.video_view);
            final View coverView = view.findViewById(R.id.cover_view);
            GlideApp.with(mContext).clear(imageView);
            Picasso.with(mContext).cancelRequest(imageView);

            progress.setVisibility(View.VISIBLE);
            commentText.setVisibility(View.VISIBLE);
            commentText.setText(post.getCommentCount());
            final AtomicBoolean isMore = new AtomicBoolean(post.getContents().size() > 1);
            showMoreView.setVisibility(isMore.get() ? View.VISIBLE : View.GONE);
            coverView.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, ReactorContentActivity.class);
                intent.putExtra(ReactorConstants.POSITION_KEY, position);
                mContext.startActivity(intent);
            });
            imageView.setImageDrawable(null);
            final Content firstContent = post.getContents().get(0);
            processContent(imageView, videoView, textView, progress, firstContent);
        }
    }

    @Override
    public int getItemCount() {
        return PostDatabaseHelper.size()+1;
    }

    /**
     * It help visible progress when load more
     */
    public void startLoadMore() {
        mOnLoadMoreFailed = false;
    }

    /**
     * It help visible layout retry when load more failed
     */
    public void onLoadMoreFailed() {
        mOnLoadMoreFailed = true;
        notifyItemChanged(bottomItemPosition());
    }

    public void onReachEnd() {
        mIsReachEnd = true;
        notifyDataSetChanged();
    }

    public void addPosts(List<Post> newPosts) {
        mIsReachEnd = false;
        int count = 0;
        for (Post post : newPosts) {
            if (!PostDatabaseHelper.getAllPost().contains(post)) {
                PostDatabaseHelper.add(post);
                count++;
            }
        }
        Log.i(LOG_TAG, String.format("Add %d new posts", count));
        notifyDataSetChanged();
    }

    public void clear() {
        PostDatabaseHelper.clear();
        notifyDataSetChanged();
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
