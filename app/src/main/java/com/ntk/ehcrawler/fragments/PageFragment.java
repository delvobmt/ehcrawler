package com.ntk.ehcrawler.fragments;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.TheHolder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class PageFragment extends Fragment {

    private ImageView mImage;
    private View mLoading;

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
        if (mImage != null && mLoading != null) {
            bindView();
        }
    }

    private String imageSrc;

    public PageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_view, null);
        mImage = (ImageView) view.findViewById(R.id.image_iv);
        mLoading = view.findViewById(R.id.loading);
        if(imageSrc !=null){
            bindView();
        }
        return view;
    }

    private void bindView() {
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
}
