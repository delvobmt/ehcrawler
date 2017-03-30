package com.ntk.ehcrawler.adapters;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.ntk.ehcrawler.fragments.PageFragment;
import com.ntk.ehcrawler.model.PageConstants;

public class SwipePageAdapter extends CursorPagerViewAdapter {
    public SwipePageAdapter(Context context, FragmentManager fm) {
        super(context, fm, null);
    }

    @Override
    public Fragment getItemFragment(Cursor data) {
        PageFragment fragment = new PageFragment();
        fragment.setArguments(new Bundle());
        String id = data.getString(0);
        String src = data.getString(PageConstants.SRC_INDEX);
        String url = data.getString(PageConstants.URL_INDEX);
        fragment.getArguments().putString(PageConstants.SRC, src);
        fragment.getArguments().putString(PageConstants.URL, url);
        fragment.getArguments().putString(PageConstants._ID, id);
        return fragment;
    }
}
