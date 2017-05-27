package com.ntk.ehcrawler;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

public class MyPreference extends SwitchPreference {
    public MyPreference(Context context) {
        super(context);
    }

    public MyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}