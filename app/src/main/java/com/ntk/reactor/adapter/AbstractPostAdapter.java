package com.ntk.reactor.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

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
import com.ntk.R;
import com.ntk.reactor.ContextHolder;
import com.ntk.reactor.GlideApp;
import com.ntk.reactor.ReactorConstants;
import com.ntk.reactor.model.Content;
import com.ntk.reactor.model.ImageContent;
import com.ntk.reactor.model.VideoGifContent;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public abstract class AbstractPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected final Context mContext;
    protected final String LOG_TAG = "LOG_" + AbstractPostAdapter.class.getSimpleName();

    protected AbstractPostAdapter(Context context) {
        this.mContext = context;
    }

    protected void processContent(ImageView imageView, PlayerView videoView, TextView textView, ProgressBar progress, SimpleExoPlayer player, Content content) {
        if (ImageContent.class.equals(content.getClass())) {
            final String src = ((ImageContent) content).getSrc();
            videoView.setVisibility(View.GONE);
            processImage(imageView, textView, progress, src);
        } else if (VideoGifContent.class.equals(content.getClass())) {
            VideoGifContent videoGifContent = (VideoGifContent) content;
            processVideoGifContent(imageView, videoView, textView, progress, player, videoGifContent);
        }
    }

    protected void processVideoGifContent(ImageView imageView, PlayerView videoView, TextView textView, ProgressBar progressView, SimpleExoPlayer player, VideoGifContent videoGifContent) {
        final List<String> sources = videoGifContent.getSrc();
        final String SRC = chooseSrc(sources);
        final String postSrc = videoGifContent.getPostSrc();
        Log.i(LOG_TAG, "load video post " + postSrc);
        textView.setVisibility(View.VISIBLE);
        String fileName = SRC.substring(SRC.lastIndexOf("/") + 1);
        fileName = fileName.substring(fileName.lastIndexOf("-") + 1);
        final String path = mContext.getFilesDir().getAbsolutePath() + File.separator + fileName;
        File file = new File(path);
        if(file.exists()){
            if(SRC.endsWith(".gif")) {
                Log.i(LOG_TAG, "GIF src = " + path + " existed");
                processGif(imageView,  videoView, textView, progressView, path, videoGifContent);
            }else if(SRC.endsWith(".webm") || SRC.endsWith(".mp4")){
                Log.i(LOG_TAG, "VIDEO src = " + path + " existed");
                processVideo(imageView, videoView, textView, progressView, player, file);
            }
        }else {
            if(SRC.endsWith(".gif")) {
                imageView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView.setText("GIF");
            }else{
                imageView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView.setText("VIDEO");
            }
            //download file
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
                    if(SRC.endsWith(".gif")) {
                        processGif(imageView, videoView, textView, progressView, path, videoGifContent);
                    }else if(SRC.endsWith(".webm") || SRC.endsWith(".mp4")){
                        processVideo(imageView, videoView, textView, progressView, player, file);
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
            BaseDownloadTask baseDownloadTask = FileDownloader.getImpl().create(SRC).setPath(path).setListener(listener).addHeader("Referer", ReactorConstants.HOST);
            baseDownloadTask.start();
        }
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

    protected void processImage(ImageView imageView, TextView textView, View progress, String src) {
        textView.setVisibility(View.GONE);
        Log.i(LOG_TAG, "load image src " + src);
        GlideApp.with(mContext).clear(imageView);
        Picasso.with(mContext).cancelRequest(imageView);
        Picasso.with(mContext).load(src).error(R.drawable.ic_error).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                imageView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError() {
                textView.setVisibility(View.VISIBLE);
                textView.setText("IMAGE FAILURE");
                progress.setVisibility(View.INVISIBLE);
            }
        });
    }

    protected void processGif(ImageView imageView, PlayerView videoView, TextView textView, View progress, String path, VideoGifContent videoGifContent) {
        int width = ContextHolder.getWidth();
        float ratio = (float)width / videoGifContent.getWidth();
        int height = (int) (videoGifContent.getHeight() * ratio);
//        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
//        layoutParams.width = width;
//        layoutParams.height = height;
//        imageView.setLayoutParams(layoutParams);
        GlideApp.with(mContext)
                .load(path)
                .centerInside()
                .into(imageView);
        imageView.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
        progress.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
    }

    protected void processVideo(ImageView imageView, PlayerView videoView, TextView textView, View progress, SimpleExoPlayer player, File file) {
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext,
                Util.getUserAgent(mContext, "yourApplicationName"));
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(file));
        // Prepare the player with the source.
        player.prepare(videoSource);
        progress.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        player.setPlayWhenReady(true);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
    }
}
