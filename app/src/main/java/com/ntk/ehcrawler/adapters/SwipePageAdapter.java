package com.ntk.ehcrawler.adapters;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.ntk.ehcrawler.fragments.PageFragment;
import com.ntk.ehcrawler.model.PageConstants;
import com.ntk.ehcrawler.services.DatabaseService;

public class SwipePageAdapter extends CursorPagerViewAdapter {
    public SwipePageAdapter(Context context, FragmentManager fm) {
        super(context, fm, null);
    }

    @Override
    public Fragment getItemFragment(Cursor data) {
        PageFragment fragment = new PageFragment();
        String imageSrc = data.getString(PageConstants.URL_INDEX);
        if(TextUtils.isEmpty(imageSrc)){
            DatabaseService.startGetBookDetail(mContext, data.getString(PageConstants.URL_INDEX));
        }else {
            fragment.setArguments(new Bundle());
            fragment.getArguments().putString(PageConstants.SRC, imageSrc);
        }
        return fragment;
    }
}
