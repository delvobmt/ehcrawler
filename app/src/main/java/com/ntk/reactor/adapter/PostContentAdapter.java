package com.ntk.reactor.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
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

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PostContentAdapter extends AbstractPostAdapter {

    private final String LOG_TAG = "LOG_" + PostContentAdapter.class.getSimpleName();

    List<Content> mContents;

    public PostContentAdapter(Context context, int position) {
        super(context);
        mContents = PostDatabaseHelper.getPostContentsAt(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_view, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final View view = holder.itemView;
        final ImageView imageView = view.findViewById(R.id.image_iv);
        final TextView textView = view.findViewById(R.id.gif_text);
        final ProgressBar progressView = view.findViewById(R.id.progress);
        progressView.setVisibility(View.VISIBLE);
        final PlayerView videoView = view.findViewById(R.id.video_view);
        GlideApp.with(mContext).clear(imageView);
        Picasso.with(mContext).cancelRequest(imageView);
        SimpleExoPlayer player;
        Object tag = view.getTag();
        if(tag instanceof SimpleExoPlayer){
            player = (SimpleExoPlayer) tag;
        }else{
            player = ExoPlayerFactory.newSimpleInstance(mContext);
            view.setTag(player);
        }
        Log.i(LOG_TAG, player.toString());
        videoView.setPlayer(player);
        imageView.setImageDrawable(null);
        final Content content = mContents.get(position);
        processContent(imageView, videoView, textView, progressView, player, content);
    }

    @Override
    public int getItemCount() {
        return mContents.size();
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        ItemViewHolder(View itemView) {
            super(itemView);
        }
    }
}
