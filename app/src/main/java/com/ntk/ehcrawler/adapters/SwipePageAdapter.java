package com.ntk.ehcrawler.adapters;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.fragments.PageFragment;
import com.ntk.ehcrawler.model.PageConstants;
import com.ntk.ehcrawler.services.DatabaseService;

public class SwipePageAdapter extends CursorPagerViewAdapter {

    private final int mSize;

    public SwipePageAdapter(Context context, FragmentManager fm, int size) {
        super(context, fm, null);
        this.mSize = size;
    }

    @Override
    public Fragment getItemFragment(Cursor cursor) {
        PageFragment fragment = new PageFragment();
        fragment.setArguments(new Bundle());
        final String id = cursor.getString(0);
        final String src = cursor.getString(PageConstants.SRC_INDEX);
        final String url = cursor.getString(PageConstants.URL_INDEX);
        final String nl = cursor.getString(PageConstants.NEWLINK_INDEX);
        int position = cursor.getPosition()+1;
        int count = cursor.getCount();
        final String bookUrl = cursor.getString(PageConstants.BOOK_URL_INDEX);
        if (position == count && count < mSize) {
            int pageIndex = (position / EHConstants.PAGES_PER_PAGE);
            DatabaseService.startGetBookDetail(mContext, id, bookUrl, pageIndex);
        }
        fragment.getArguments().putString(PageConstants._ID, id);
        fragment.getArguments().putString(PageConstants.SRC, src);
        fragment.getArguments().putString(PageConstants.URL, url);
        fragment.getArguments().putString(PageConstants.NEWLINK, nl);
        return fragment;
    }
}
