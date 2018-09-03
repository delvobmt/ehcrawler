package com.ntk.ehcrawler.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ntk.R;
import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.activities.FullscreenActivity;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;
import com.ntk.ehcrawler.services.DatabaseService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ThumbAdapter extends CursorRecyclerViewAdapter{

    private static final String LOG_TAG = "LOG_" + ThumbAdapter.class.getName();
    private final Context mContext;
    private final int mSize;

    public ThumbAdapter(Context context, int size) {
        super(context, null);
        this.mContext = context;
        this.mSize = size;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        final int position = cursor.getPosition();
        final int count = cursor.getCount();
        final String id = cursor.getString(0);
        final String url = cursor.getString(PageConstants.BOOK_URL_INDEX);
        final String thumbSrc = cursor.getString(PageConstants.THUMB_SRC_INDEX);
        final String src = cursor.getString(PageConstants.SRC_INDEX);
        if (position + 1 == count && count < mSize) {
            int pageIndex = (position / EHConstants.PAGES_PER_PAGE) + 1;
            DatabaseService.startGetBookDetail(mContext, id, url, pageIndex);
        }

        final View view = viewHolder.itemView;
        final ImageView mThumb = (ImageView) view.findViewById(R.id.thumb_iv);
        final ImageView mIcSave = (ImageView) view.findViewById(R.id.save_iv);
        final View mLoading = view.findViewById(R.id.loading);

        if (!TextUtils.isEmpty(src) && src.startsWith("file://")){
            /* offline file available */
            mIcSave.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(src).into(mThumb);
            mLoading.setVisibility(View.GONE);
            Log.d(LOG_TAG, "load thumb offline "+src);
        }else {
            mIcSave.setVisibility(View.GONE);
            Log.d(LOG_TAG, "load thumb online "+thumbSrc);
            final int offset = cursor.getPosition() % 20;
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    final int width = 100;
                    final int height = bitmap.getHeight();
                    final int x = width * offset;
                    final int y = 0;
                    if (x + width <= bitmap.getWidth()) {
                        Bitmap image = Bitmap.createBitmap(bitmap, x, y, width, height);
                        mThumb.setImageBitmap(image);
                        mLoading.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    mThumb.setImageDrawable(errorDrawable);
                    mLoading.setVisibility(View.GONE);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    mThumb.setImageDrawable(placeHolderDrawable);
                }
            };
            //keep target reference with imageView
            mThumb.setTag(target);
            Picasso.with(mContext).load(thumbSrc).into(target);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FullscreenActivity.class);
                intent.putExtra(BookConstants.URL, url);
                intent.putExtra(BookConstants.FILE_COUNT, mSize);
                intent.putExtra(EHConstants.POSITION, position);
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
