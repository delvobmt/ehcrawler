package com.ntk.reactor.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.ntk.reactor.database.PostDatabaseHelper;
import com.ntk.reactor.fragment.PostContentFragment;

public class PostContentPagerAdapter extends FragmentStatePagerAdapter {

    public PostContentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        PostContentFragment fragment = new PostContentFragment();
        return fragment;
    }

    @Override
    public int getCount() {
        return PostDatabaseHelper.size() + 1;
    }
}
