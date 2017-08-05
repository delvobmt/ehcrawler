package com.ntk.ehcrawler.model;

import android.provider.BaseColumns;

public interface BookConstants extends BaseColumns {

    String TABLE_NAME = "BOOKS";
    String TABLE_FAVORITE_NAME = "FAVORITE_BOOKS";
    String TITLE = "title";
    String URL = "url";
    String IMAGE_SRC = "image_src";
    String FILE_COUNT = "file_count";
    String RATE = "rate";
    String TYPE = "type";
    String DETAIL = "book_info_detail";
    String TAGS = "tags";

    String TABLE_BOOK_STATUS_NAME = "BOOK_STATUS";
    String CURRENT_POSITION = "current_position";
    String IS_FAVORITE = "is_favorite";
    String LAST_READ = "last_read";

    int TITLE_INDEX = 1;
    int URL_INDEX = 2;
    int IMAGE_SRC_INDEX = 3;
    int FILE_COUNT_INDEX = 4;
    int RATE_INDEX = 5;
    int TYPE_INDEX = 6;
    int DETAIL_INDEX = 7;
    int TAGS_INDEX = 8;
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

    int CURRENT_POSITION_INDEX = 2;
    int IS_FAVORITE_INDEX = 3;
    int LAST_READ_INDEX = 4;
    String[] BOOK_STATUS_PROJECTION = {
            _ID,
            URL,
            CURRENT_POSITION,
            IS_FAVORITE,
            LAST_READ
    };
}