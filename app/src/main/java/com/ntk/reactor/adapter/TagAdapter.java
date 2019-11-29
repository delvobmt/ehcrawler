package com.ntk.reactor.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ntk.R;
import com.ntk.reactor.GlideApp;
import com.ntk.reactor.ReactorConstants;
import com.ntk.reactor.database.PostDatabaseHelper;
import com.ntk.reactor.model.Content;
import com.ntk.reactor.model.ImageContent;
import com.ntk.reactor.model.VideoGifContent;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class TagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = "LOG_" + TagAdapter.class.getSimpleName();

    private final Context mContext;
    private final View.OnClickListener mOnClickListener;

    List<String> mTags;

    public TagAdapter(Context context, int position, View.OnClickListener onClickListener) {
        this.mContext = context;
        mTags = PostDatabaseHelper.getTagsAt(position);
        mOnClickListener = onClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.tag_view, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Button view = (Button) holder.itemView;
        String tag = mTags.get(position);
        view.setText(tag);
        view.setOnClickListener(mOnClickListener);
        view.setTag(tag);
    }

    @Override
    public int getItemCount() {
        return mTags.size();
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        ItemViewHolder(View itemView) {
            super(itemView);
        }
    }
}