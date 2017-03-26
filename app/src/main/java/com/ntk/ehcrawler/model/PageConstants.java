package com.ntk.ehcrawler.model;

import android.provider.BaseColumns;

public interface PageConstants extends BaseColumns{
    String TABLE_NAME = "PAGES";
    String BOOK_URL = "book_url";
    int BOOK_URL_INDEX = 1;
    String URL = "url";
    int URL_INDEX = 2;
    String THUMB_SRC = "thumb_src";
    int THUMB_SRC_INDEX = 3;
    String SRC = "src";
    int SRC_INDEX = 4;

    String[] PROJECTION = {
            _ID,
            BOOK_URL,
            URL,
            THUMB_SRC,
            SRC
    };
}
