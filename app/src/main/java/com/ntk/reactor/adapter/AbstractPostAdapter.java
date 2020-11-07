package com.ntk.reactor.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.ntk.reactor.GlideApp;
import com.ntk.reactor.ReactorConstants;
import com.ntk.reactor.model.Content;
import com.ntk.reactor.model.ImageContent;
import com.ntk.reactor.model.VideoGifContent;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Player.EventListener {

    protected static final int TYPE_ITEM = 1;
    protected static final int TYPE_PROGRESS = 0;
    protected final Context mContext;
    protected final String LOG_TAG = "LOG_" + AbstractPostAdapter.class.getSimpleName();
    private static final int MAX_PLAYERS = 4;
    private static final SimpleExoPlayer[] PLAYERS = new SimpleExoPlayer[MAX_PLAYERS];
    private static final AtomicInteger PLAYER_INDEX = new AtomicInteger(0);

    protected AbstractPostAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == bottomItemPosition()) {
            return TYPE_PROGRESS;
        }
        return TYPE_ITEM;
    }

    protected int bottomItemPosition() {
        return getItemCount() - 1;
    }

    protected void processContent(ImageView imageView, PlayerView videoView, TextView textView, ProgressBar progress, Content content) {
        if (ImageContent.class.equals(content.getClass())) {
            final ImageContent imageContent = ((ImageContent) content);
            processImage(imageView, videoView, textView, progress, imageContent);
        } else if (VideoGifContent.class.equals(content.getClass())) {
            VideoGifContent videoGifContent = (VideoGifContent) content;
            processVideoGifContent(imageView, videoView, textView, progress, videoGifContent);
        }
    }

    protected void processVideoGifContent(ImageView imageView, PlayerView videoView, TextView textView, ProgressBar progressView, VideoGifContent videoGifContent) {
        final List<String> sources = videoGifContent.getSrc();
        final String SRC = chooseSrc(sources);
        final String postSrc = videoGifContent.getPostSrc();
        Log.i(LOG_TAG, "load video post " + postSrc);
        textView.setVisibility(View.VISIBLE);
        final String path = getFile(SRC);
        File file = new File(path);
        boolean isGif = SRC.endsWith(".gif");
        boolean isVideo = SRC.endsWith(".webm") || SRC.endsWith(".mp4");

        if(file.exists()){
            if(isGif) {
                Log.i(LOG_TAG, "GIF src = " + path + " existed");
                processGifImage(imageView,  videoView, textView, progressView, path);
            }else if(isVideo){
                Log.i(LOG_TAG, "VIDEO src = " + path + " existed");
                processVideo(imageView, videoView, textView, progressView, file);
            }
        }else {
            String text = "PIC";
            if(isGif) text = "GIF";
            else if (isVideo) text = "VIDEO";
            imageView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            textView.setText(text);
            //download file
            downloadFile(imageView, videoView, textView, progressView, SRC, path);
        }
    }

    @NonNull
    private String getFile(String SRC) {
        String fileName = SRC.substring(SRC.lastIndexOf("/") + 1);
        fileName = fileName.substring(fileName.lastIndexOf("-") + 1);
        return mContext.getFilesDir().getAbsolutePath() + File.separator + fileName;
    }

    private void downloadFile(ImageView imageView, PlayerView videoView, TextView textView, ProgressBar progressView, String src, String path) {
        File file = new File(path);
        FileDownloadListener listener = new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask baseDownloadTask, int progress, int total) {
                Log.v(LOG_TAG, "pending "+ baseDownloadTask.getFilename() + "[" + progress + "/" + total + "]");
            }

            @Override
            protected void progress(BaseDownloadTask baseDownloadTask, int progress, int total) {
                Log.v(LOG_TAG, "progress "+ baseDownloadTask.getFilename() + "[" + progress + "/" + total + "]");
                progressView.setProgress(progress);
                progressView.setMax(total);
            }

            @Override
            protected void completed(BaseDownloadTask baseDownloadTask) {
                Log.i(LOG_TAG, "completed "+ baseDownloadTask.getFilename());

                if(src.endsWith(".webm") || src.endsWith(".mp4")){
                    processVideo(imageView, videoView, textView, progressView, file);
                }else{
                    processGifImage(imageView, videoView, textView, progressView, path);
                }
            }

            @Override
            protected void paused(BaseDownloadTask baseDownloadTask, int progress, int total) {
                Log.i(LOG_TAG, "paused "+ baseDownloadTask.getFilename() + "[" + progress + "/" + total + "]");
            }

            @Override
            protected void error(BaseDownloadTask baseDownloadTask, Throwable throwable) {
                Log.e(LOG_TAG, "error "+ baseDownloadTask.getUrl(), throwable);
                textView.setText("GIF FAILURE");
                progressView.setVisibility(View.INVISIBLE);
            }

            @Override
            protected void warn(BaseDownloadTask baseDownloadTask) {
                Log.w(LOG_TAG, "warn "+ baseDownloadTask.getFilename());
            }
        };
        BaseDownloadTask baseDownloadTask = FileDownloader.getImpl().create(src).setPath(path).setListener(listener).addHeader("Referer", ReactorConstants.HOST);
        baseDownloadTask.start();
    }

    protected String chooseSrc(List<String> sources) {
        String tmp = "";
        for(String s : sources){
            if(s.endsWith(".webm") ){
                tmp = s;
                break;
            }else if(s.endsWith(".gif")){
                tmp = s;
            }else if("".equals(tmp)){
                tmp = s;
            }
        }
        return tmp;
    }

    protected void processImage(ImageView imageView, PlayerView videoView, TextView textView, ProgressBar progressView, ImageContent imageContent) {
        videoView.setVisibility(View.GONE);
        String src = imageContent.getSrc();
        final String path = getFile(src);
        File file = new File(path);
        if(file.exists()){
            processGifImage(imageView,  videoView, textView, progressView, path);
        }else {
            imageView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            textView.setText("IMAGE");
            //download file
            downloadFile(imageView, videoView, textView, progressView, src, path);
        }
    }

    protected void processGifImage(ImageView imageView, PlayerView videoView, TextView textView, View progress, String path) {
        GlideApp.with(mContext)
                .load(path)
                .centerInside()
                .into(imageView);
        imageView.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
        progress.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
    }

    protected void processVideo(ImageView imageView, PlayerView videoView, TextView textView, View progress, File file) {
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext,
                Util.getUserAgent(mContext, "reactor"));
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(file));
        // Prepare the player with the source.

        int index = PLAYER_INDEX.incrementAndGet();
        SimpleExoPlayer player = PLAYERS[(index % MAX_PLAYERS)];
        if(player == null){
            player = ExoPlayerFactory.newSimpleInstance(mContext);
            player.addListener(this);
            PLAYERS[(index % MAX_PLAYERS)] = player;

        }
        Log.i(LOG_TAG, player.toString());
        videoView.setPlayer(player);

        player.prepare(videoSource);
        progress.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        player.setPlayWhenReady(true);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.e(LOG_TAG, error.getMessage(), error);
    }
}
