package com.ntk.ehcrawler.fragments;


import android.content.ContentValues;
import android.database.Cursor;
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

import com.github.chrisbanes.photoview.EHPhotoView;
import com.ntk.R;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.PageConstants;
import com.ntk.ehcrawler.services.DatabaseService;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

public class PageFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final String LOG_TAG = "LOG_"+PageFragment.class.getSimpleName();
    private EHPhotoView mImage;
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
        mImage = (EHPhotoView) view.findViewById(R.id.image_iv);
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
        Log.d(LOG_TAG, "load img " + imageSrc);
        Picasso.with(getContext()).load(imageSrc)
                .into(mImage, new Callback() {
            @Override
            public void onSuccess() {
                mLoading.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                mLoading.setVisibility(View.GONE);
                if (imageSrc.startsWith("file://")){
                    ContentValues values = new ContentValues();
                    values.put(PageConstants.SRC, "");
                    getContext().getContentResolver().update(BookProvider.PAGES_CONTENT_URI, values, PageConstants._ID + "=?", new String[]{mId});
                    if(new File(imageSrc).delete()){
                        Log.e(LOG_TAG, "IMAGE ERROR! deleted file " + imageSrc);
                    }
                }
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
