package com.ntk.ehcrawler.adapters;

import android.content.Context;
import android.database.Cursor;
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
    public Fragment getItemFragemnt(Cursor data) {
        PageFragment fragment = new PageFragment();
        String imageSrc = data.getString(PageConstants.SRC_INDEX);
        if(TextUtils.isEmpty(imageSrc)){
            String url = data.getString(PageConstants.BOOK_URL_INDEX);
            DatabaseService.startGetBookImageSrc(mContext, url);
        }
        return fragment;
    }
}
