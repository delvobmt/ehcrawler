package com.ntk.reactor.adapter;

import android.content.Context;
import android.content.Loader;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ntk.R;
import com.ntk.reactor.ReactorUtils;
import com.ntk.reactor.model.Content;
import com.ntk.reactor.model.ImageContent;
import com.ntk.reactor.model.Post;
import com.ntk.reactor.model.VideoGifContent;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = "LOG_" + PostAdapter.class.getSimpleName();
    List<Post> posts = new ArrayList<>();

    private final Context mContext;

    public PostAdapter(Context context) {
        this.mContext = context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_view, null);
        return new RecyclerView.ViewHolder(view) {};
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        View view = holder.itemView;
        ImageView imageView = (ImageView) view.findViewById(R.id.image_iv);
        Post post = posts.get(position);
        Content content = post.getContents().get(0);
        if (ImageContent.class.equals(content.getClass())){
            String src = ((ImageContent) content).getSrc();
            Picasso.with(mContext).load(src).into(imageView);
        } else if (VideoGifContent.class.equals(content.getClass())) {
            String src = ((VideoGifContent) content).getPostSrc();
            Picasso.with(mContext).load(src).into(imageView);
        }else{

        }

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void addPosts(List<Post> newPosts){
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }
}
