package com.ntk.ehcrawler.fragments;


import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.ContextHolder;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.PageConstants;
import com.ntk.ehcrawler.services.DatabaseService;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class PageFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final String LOG_TAG = "LOG_"+PageFragment.class.getSimpleName();
    private ImageView mImage;
    private View mLoading;
    private String mUrl;
    private String mSrc;
    private String mId;
    private String mNl;

    public PageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_view, null);
        mImage = (ImageView) view.findViewById(R.id.image_iv);
        view.findViewById(R.id.reload).setOnClickListener(this);
        mLoading = view.findViewById(R.id.loading);
        mSrc = getArguments().getString(PageConstants.SRC);
        mUrl = getArguments().getString(PageConstants.URL);
        mId = getArguments().getString(PageConstants._ID);
        mNl = getArguments().getString(PageConstants.NEWLINK);
        if(!TextUtils.isEmpty(mSrc)){
            bindView(mSrc);
        }else{
            DatabaseService.startGetPageData(getContext(), mId, mUrl);
            getLoaderManager().initLoader(BookProvider.PAGE_INFO_LOADER, null, this);
        }
        return view;
    }

    private void bindView(final String imageSrc) {
        final int imageWidth;
        final int targetWidth = ContextHolder.getScreenWidth();
        final int targetHeight = ContextHolder.getScreenHeight();
        //landscape force screen
        if(targetWidth>targetHeight) {
            imageWidth = targetWidth;
            mImage.setMinimumWidth(targetWidth);
            mImage.setMinimumHeight(targetHeight);
        }else{
            imageWidth = targetHeight;
            mImage.setMinimumWidth(targetHeight);
            mImage.setMinimumHeight(targetWidth);
        }
        Transformation transformation = new Transformation() {
            @Override
            public Bitmap transform(Bitmap source) {
                double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                int imageHeight = (int) (imageWidth * aspectRatio);
                Bitmap result = Bitmap.createScaledBitmap(source, imageWidth, imageHeight, true);
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
        Picasso.with(getContext()).load(imageSrc)
                .transform(transformation)
                .into(mImage, new Callback() {
            @Override
            public void onSuccess() {
                mLoading.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                mLoading.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.withAppendedPath(BookProvider.PAGES_CONTENT_URI, mId);
        String[] projection = PageConstants.PROJECTION;
        String selection = PageConstants.URL+"=?";
        String[] selectionArgs = {mUrl};
        String sortOrder = null;
        return new CursorLoader(getContext(), uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()) {
            mSrc = data.getString(PageConstants.SRC_INDEX);
            mNl = data.getString(PageConstants.NEWLINK_INDEX);
            if (!TextUtils.isEmpty(mSrc)) {
                bindView(mSrc);
                Log.d(LOG_TAG, "bindView() " + mSrc);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.reload:{
                DatabaseService.startGetPageData(getContext(), mId, mUrl, mNl);
            } break;
        }
    }
}
