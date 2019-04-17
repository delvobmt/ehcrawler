package com.ntk.reactor.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;
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
import com.volokh.danylo.video_player_manager.manager.PlayerItemChangeListener;
import com.volokh.danylo.video_player_manager.manager.SingleVideoPlayerManager;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;
import com.volokh.danylo.video_player_manager.ui.ScalableTextureView;
import com.volokh.danylo.video_player_manager.ui.SimpleMainThreadMediaPlayerListener;
import com.volokh.danylo.video_player_manager.ui.VideoPlayerView;

import java.io.File;
import java.util.List;
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

    private VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(MetaData metaData) {

        }
    });

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
            final View progress = view.findViewById(R.id.progress);
            final Post post = PostDatabaseHelper.getPostAt(position);
            final View showMoreView = view.findViewById(R.id.show_more_view);
            final TextView commentText = view.findViewById(R.id.comment_text);
            final VideoPlayerView videoView = view.findViewById(R.id.video_view);
            videoView.setScaleType(ScalableTextureView.ScaleType.FILL);
            videoView.addMediaPlayerListener(new SimpleMainThreadMediaPlayerListener(){
                @Override
                public void onVideoPreparedMainThread() {
                    // We hide the cover when video is prepared. Playback is about to start
                    imageView.setVisibility(View.INVISIBLE);
                    textView.setVisibility(View.INVISIBLE);
                    progress.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onVideoStoppedMainThread() {
                    // We show the cover when video is stopped
                    imageView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText("Paused");
                }

                @Override
                public void onVideoCompletionMainThread() {
                    // We show the cover when video is completed
                    imageView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText("Stopped");
                }
            });
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
                videoView.setVisibility(View.INVISIBLE);
                Log.i(LOG_TAG, "load image src " + src);
                GlideApp.with(mContext).clear(imageView);
                Picasso.with(mContext).cancelRequest(imageView);
                Picasso.with(mContext).load(src).error(R.drawable.ic_error).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        imageView.setVisibility(View.VISIBLE);
                        progress.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError() {
                        textView.setVisibility(View.VISIBLE);
                        textView.setText("IMAGE FAILURE");
                        progress.setVisibility(View.INVISIBLE);
                    }
                });
            } else if (VideoGifContent.class.equals(firstContent.getClass())) {
                final List<String> sources = ((VideoGifContent) firstContent).getSrc();
                String src = "";
                for(String s : sources){
                    if(s.endsWith(".webm") ){
                        src = s;
                        break;
                    }else if(s.endsWith(".gif")){
                        src = s;
                    }else if("".equals(src)){
                        src = s;
                    }
                }
                final String postSrc = ((VideoGifContent) firstContent).getPostSrc();
                Log.i(LOG_TAG, "load video post " + postSrc);
                textView.setVisibility(View.VISIBLE);
                GlideApp.with(mContext).clear(imageView);
                Picasso.with(mContext).cancelRequest(imageView);
                if(src.endsWith(".gif")) {
                    imageView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText("GIF");
                    videoView.setVisibility(View.INVISIBLE);
                    GlideApp.with(mContext)
                            .load(src)
                            .onlyRetrieveFromCache(true)
                            .error(GlideApp.with(mContext).load(postSrc).error(R.drawable.ic_error).addListener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    textView.setText("GIF FAILURE");
                                    progress.setVisibility(View.INVISIBLE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    if (postSrc.endsWith(".gif")) {
                                        textView.setVisibility(View.INVISIBLE);
                                    }
                                    progress.setVisibility(View.INVISIBLE);
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
                                    textView.setVisibility(View.INVISIBLE);
                                    progress.setVisibility(View.INVISIBLE);
                                    return false;
                                }
                            })
                            .into(imageView);
                }else if(src.endsWith(".webm") || src.endsWith(".mp4")) {
                    ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
                    int width = ContextHolder.getWidth();
                    float ratio = (float)width / firstContent.getWidth();
                    int height = (int) (firstContent.getHeight() * ratio);
                    layoutParams.width = width;
                    layoutParams.height = height;
                    videoView.setLayoutParams(layoutParams);
                    Picasso.with(mContext).load(postSrc).error(R.drawable.ic_error).into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            imageView.setVisibility(View.VISIBLE);
                            progress.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError() {
                            textView.setVisibility(View.VISIBLE);
                            textView.setText("VIDEO FAILURE");
                            progress.setVisibility(View.INVISIBLE);
                        }
                    });
                    textView.setVisibility(View.VISIBLE);
                    textView.setText("VIDEO");
                }
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
