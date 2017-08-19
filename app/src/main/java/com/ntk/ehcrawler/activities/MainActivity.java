
package com.ntk.ehcrawler.activities;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.TheHolder;

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        TheHolder.setWidth(point.x);
        TheHolder.setHeight(point.y);


    }

    class BookTabPageAdapter extends FragmentPagerAdapter{

        @Override
        public Fragment getItem(int position) {
            switch ()
            return null;
        }

        @Override
        public int getCount() {
            return 0;
        }
    }
}
