package com.ntk.ehcrawler.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.ContextHolder;
import com.ntk.ehcrawler.model.PageConstants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class PageAdapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {
    private final Context mContext;

    public PageAdapter(Context context) {
        super(context, null);
        this.mContext = context;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        View view = viewHolder.itemView;
        final View mLoading = view.findViewById(R.id.loading);
        final ImageView mImage = (ImageView) view.findViewById(R.id.image_iv);
        final int targetWidth = ContextHolder.getScreenWidth();
        final int targetHeight = ContextHolder.getScreenHeight();
        //landscape force screen
        if(targetWidth>targetHeight) {
            mImage.setMinimumWidth(targetWidth);
            mImage.setMinimumHeight(targetHeight);
        }else{
            mImage.setMinimumWidth(targetHeight);
            mImage.setMinimumHeight(targetWidth);
        }
        final String imageSrc = cursor.getString(PageConstants.SRC_INDEX);
        if (TextUtils.isEmpty(imageSrc)) {
            //get image src
            String url = cursor.getString(PageConstants.URL_INDEX);
//            DatabaseService.startGetPageData(mContext, url);
            return;
        }
        Transformation transformation = new Transformation() {
            @Override
            public Bitmap transform(Bitmap source) {
                double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                int targetHeight = (int) (targetWidth * aspectRatio);
                Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                if (result != source) {
                    source.recycle();
                }
                return result;
            }

            @Override
            public String key() {
                return imageSrc;
            }
        };
        Picasso.with(mContext).load(imageSrc).transform(transformation).into(mImage, new Callback() {
            @Override
            public void onSuccess() {
                mLoading.setVisibility(View.GONE);
            }

            @Override
            public void onError() {

            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.page_view, null);
        return new RecyclerView.ViewHolder(view) {};
    }
}
