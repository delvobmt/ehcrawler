package com.ntk.ehcrawler.model;

import android.provider.BaseColumns;

public interface BookConstants extends BaseColumns {

    String TABLE_NAME = "Books";
    String TITLE = "title";
    int TITLE_INDEX = 1;
    String URL = "url";
    int URL_INDEX = 2;
    String IMAGE_SRC = "image_src";
    int IMAGE_SRC_INDEX = 3;
    String FILE_COUNT = "file_count";
    int FILE_COUNT_INDEX = 4;
    String RATE = "rate";
    int RATE_INDEX = 5;
    String TYPE = "type";
    int TYPE_INDEX = 6;

    String[] PROJECTION = {
            _ID,
            TITLE,
            URL,
            IMAGE_SRC,
            FILE_COUNT,
            RATE,
            TYPE
    };
}
