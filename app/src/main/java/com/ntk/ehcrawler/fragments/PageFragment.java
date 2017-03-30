package com.ntk.ehcrawler.fragments;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.TheHolder;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.model.PageConstants;
import com.ntk.ehcrawler.services.DatabaseService;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class PageFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ImageView mImage;
    private View mLoading;
    private String mUrl;
    private String mSrc;
    private String mId;

    public PageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_view, null);
        mImage = (ImageView) view.findViewById(R.id.image_iv);
        mLoading = view.findViewById(R.id.loading);
        mSrc = getArguments().getString(PageConstants.SRC);
        mUrl = getArguments().getString(PageConstants.URL);
        mId = getArguments().getString(PageConstants._ID);
        if(!TextUtils.isEmpty(mSrc)){
            bindView(mSrc);
        }else{
            DatabaseService.startGetBookImageSrc(getContext(), mId, mUrl);
            getLoaderManager().initLoader(BookProvider.PAGE_INFO_LOADER, null, this);
        }
        return view;
    }

    private void bindView(final String imageSrc) {
        final int imageWidth;
        final int targetWidth = TheHolder.getScreenWidth();
        final int targetHeight = TheHolder.getScreenHeight();
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
                Bitmap result = Bitmap.createScaledBitmap(source, imageWidth, imageHeight, false);
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
        Picasso.with(getContext()).load(imageSrc).transform(transformation).into(mImage, new Callback() {
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
            if (!TextUtils.isEmpty(mSrc)) {
                bindView(mSrc);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
