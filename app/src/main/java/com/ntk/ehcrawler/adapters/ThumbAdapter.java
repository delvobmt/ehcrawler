package com.ntk.ehcrawler.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.activities.FullscreenActivity;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ThumbAdapter extends CursorRecyclerViewAdapter{

    private final Context mContext;

    public ThumbAdapter(Context context) {
        super(context, null);
        this.mContext = context;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        View view = viewHolder.itemView;
        final ImageView mThumb = (ImageView) view.findViewById(R.id.thumb_iv);
        final View mLoading = view.findViewById(R.id.loading);
        String thumbSrc = cursor.getString(PageConstants.THUMB_SRC_INDEX);
        final int offset = cursor.getPosition() % 20;
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                final int width = 100;
                final int height = bitmap.getHeight();
                final int x = width*offset;
                final int y = 0;
                if(x+width<=bitmap.getWidth()){
                    Bitmap image = Bitmap.createBitmap(bitmap, x, y, width, height);
                    mThumb.setImageBitmap(image);
                    mLoading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        //keep target reference with imageView
        mThumb.setTag(target);
        Picasso.with(mContext).load(thumbSrc).into(target);

        final String url = cursor.getString(PageConstants.BOOK_URL_INDEX);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FullscreenActivity.class);
                intent.putExtra(PageConstants.URL, url);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.thumb_view, null);
        return new RecyclerView.ViewHolder(view) {};
    }
}
