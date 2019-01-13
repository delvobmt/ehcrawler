package com.ntk.reactor.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.felipecsl.gifimageview.library.GifImageView;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.ntk.R;
import com.ntk.reactor.ContextHolder;
import com.ntk.reactor.GlideApp;
import com.ntk.reactor.ReactorConstants;
import com.ntk.reactor.ReactorContentActivity;
import com.ntk.reactor.database.PostDatabaseHelper;
import com.ntk.reactor.model.Content;
import com.ntk.reactor.model.ImageContent;
import com.ntk.reactor.model.Post;
import com.ntk.reactor.model.VideoGifContent;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = "LOG_" + PostAdapter.class.getSimpleName();
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_PROGRESS = 0;
    private boolean mOnLoadMoreFailed;
    private boolean mIsReachEnd;

    private final Context mContext;

    public PostAdapter(Context context) {
        this.mContext = context;
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
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
            final View progress = view.findViewById(R.id.progress);
            final Post post = PostDatabaseHelper.getPostAt(position);
            final View showMoreView = view.findViewById(R.id.show_more_view);
            final TextView commentText = view.findViewById(R.id.comment_text);
            commentText.setVisibility(View.VISIBLE);
            commentText.setText(post.getCommentCount());
            final AtomicBoolean isMore = new AtomicBoolean(post.getContents().size() > 1);
            showMoreView.setVisibility(isMore.get() ? View.VISIBLE : View.GONE);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ReactorContentActivity.class);
                    intent.putExtra(ReactorConstants.POSITION_KEY, position);
                    mContext.startActivity(intent);
                }
            });
            final Content firstContent = post.getContents().get(0);
            if (ImageContent.class.equals(firstContent.getClass())) {
                final String src = ((ImageContent) firstContent).getSrc();
                textView.setVisibility(View.GONE);
                GlideApp.with(mContext).clear(imageView);
                Picasso.with(mContext).cancelRequest(imageView);
                Picasso.with(mContext).load(src).error(R.drawable.ic_error).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        progress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        textView.setVisibility(View.VISIBLE);
                        textView.setText("IMAGE FAILURE");
                        progress.setVisibility(View.GONE);
                    }
                });
            } else if (VideoGifContent.class.equals(firstContent.getClass())) {
                final String src = ((VideoGifContent) firstContent).getSrc();
                final String postSrc = ((VideoGifContent) firstContent).getPostSrc();
                textView.setVisibility(View.VISIBLE);
                GlideApp.with(mContext).clear(imageView);
                Picasso.with(mContext).cancelRequest(imageView);
                GlideApp.with(mContext)
                        .load(src)
                        .onlyRetrieveFromCache(true)
                        .error(GlideApp.with(mContext).load(postSrc).error(R.drawable.ic_error).addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                textView.setText("GIF FAILURE");
                                progress.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                if(postSrc.endsWith(".gif")){
                                    textView.setVisibility(View.GONE);
                                }
                                progress.setVisibility(View.GONE);
                                return false;
                            }
                        }))
                        .fitCenter()
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                textView.setVisibility(View.GONE);
                                progress.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(imageView);
            }
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
