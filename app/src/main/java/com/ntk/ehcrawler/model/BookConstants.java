package com.ntk.ehcrawler.model;

import android.provider.BaseColumns;

public interface BookConstants extends BaseColumns {

    String TABLE_NAME = "BOOKS";
    String TABLE_FAVORITE_NAME = "FAVORITE_BOOKS";
    String TABLE_CACHE = "BOOKS_CACHE";
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
    String DETAIL = "book_info_detail";
    int DETAIL_INDEX = 7;
    String TAGS = "tags";
    int TAGS_INDEX = 8;
    String CURRENT_POSITION = "current_position";
    int CURRENT_POSITION_INDEX = 1;

    String[] PROJECTION = {
            _ID,
            TITLE,
            URL,
            IMAGE_SRC,
            FILE_COUNT,
            RATE,
            TYPE,
            DETAIL,
            TAGS
    };

    String[] CACHE_PROJECTION = {
            _ID,
            URL,
            CURRENT_POSITION
    };
}