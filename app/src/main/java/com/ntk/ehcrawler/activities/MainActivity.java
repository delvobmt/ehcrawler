
package com.ntk.ehcrawler.activities;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.TheHolder;
import com.ntk.ehcrawler.fragments.BookFragment;
import com.ntk.ehcrawler.fragments.FavoriteBookFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        TheHolder.setWidth(point.x);
        TheHolder.setHeight(point.y);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new BookTabPageAdapter(getSupportFragmentManager()));

        View fabSearch = findViewById(R.id.fab_search);
        fabSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
        }
    }

    class BookTabPageAdapter extends FragmentPagerAdapter{

        public BookTabPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new BookFragment();
                case 1:
                    return new FavoriteBookFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "NEW";
                case 1:
                    return "FAVORITE";
            }
            return super.getPageTitle(position);
        }
    }
}
