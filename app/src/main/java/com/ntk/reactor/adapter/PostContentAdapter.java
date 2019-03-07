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
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.ntk.R;
import com.ntk.reactor.GlideApp;
import com.ntk.reactor.ReactorContentActivity;
import com.ntk.reactor.database.PostDatabaseHelper;
import com.ntk.reactor.model.Content;
import com.ntk.reactor.model.ImageContent;
import com.ntk.reactor.model.Post;
import com.ntk.reactor.model.VideoGifContent;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = "LOG_" + PostContentAdapter.class.getSimpleName();

    private final Context mContext;

    List<Content> mContents;

    public PostContentAdapter(Context context, int position) {
        this.mContext = context;
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
        final View progress = view.findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);
        final Content content = mContents.get(position);
        if (ImageContent.class.equals(content.getClass())) {
            final String src = ((ImageContent) content).getSrc();
            textView.setVisibility(View.GONE);
            GlideApp.with(mContext).clear(imageView);
            Picasso.with(mContext).cancelRequest(imageView);
            Picasso.with(mContext).load(src).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    textView.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText("FAILED");
                    progress.setVisibility(View.GONE);
                }
            });
        } else if (VideoGifContent.class.equals(content.getClass())) {
            final List<String> sources = ((VideoGifContent) content).getSrc();
            String src = "";
            final String postSrc = ((VideoGifContent) content).getPostSrc();
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
            textView.setVisibility(View.VISIBLE);
            GlideApp.with(mContext).clear(imageView);
            Picasso.with(mContext).cancelRequest(imageView);
            if(src.endsWith(".gif")) {
                GlideApp.with(mContext)
                        .load(src)
                        .error(GlideApp.with(mContext).load(postSrc))
                        .fitCenter()
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                textView.setText("GIF FAILURE");
                                progress.setVisibility(View.GONE);
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
            }else if(src.endsWith(".webm") || src.endsWith(".mp4")){
            }
        }
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
