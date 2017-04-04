package com.ntk.ehcrawler;

public interface EHConstants {
	String HOST = "https://e-hentai.org/";
	String SEARCH_TERM = "http://g.e-hentai.org/?page={startPage?}&f_search={searchTerms}&f_apply=Apply+Filter";
	String SEARCH_DOUJINSHI_KEY = "f_doujinshi";
	String SEARCH_MANGA_KEY = "f_manga";
	String SEARCH_ARTISTCG_KEY = "f_artistcg";
	String SEARCH_GAMECG_KEY = "f_gamecg";
	String SEARCH_WESTERN = "f_western";
	String SEARCH_NON_HENTAI_KEY = "f_non-h";
	String SEARCH_IMAGESET_KEY = "f_imageset";
	String SEARCH_COSPLAY_KEY = "f_cosplay";
	String SEARCH_ASIANPORN_KEY = "f_asianporn";
	String SEARCH_MISC_KEY = "f_misc";
	String SEARCH_KEY = "f_search";

	String ITEM_CSS_SELECTOR = ".id1";
	String ITEM_TITLE_CSS_SELECTOR = ".id2 a";
	String ITEM_IMAGE_CSS_SELECTOR = ".id3 img";
	String ITEM_TYPE_CSS_SELECTOR = ".id41";
	String ITEM_FILE_COUNT_CSS_SELECTOR = ".id42";
	String ITEM_STAR_RATE_CSS_SELECTOR = ".id43";

	String DETAIL_IMAGE_CSS_SELECTOR = "#gd1 div";
	String DETAIL_CSS_SELECTOR = "#gdd tr ";
	String DETAIL_TAGLIST_CSS_SELECTOR = "#taglist tr";
	String DETAIL_TAG_PRE_CSS_SELECTOR = ".tc";
	String DETAIL_TAG_CSS_SELECTOR = ".gtl a,.gt a";
	String DETAIL_KEY_CSS_SELECTOR = ".gdt1";
	String DETAIL_VALUE_CSS_SELECTOR = ".gdt2";

	String PAGE_THUMB_CSS_SELECTOR = ".gdtm div";
	String PAGE_URL_CSS_SELECTOR = ".gdtm div a";
	String PAGE_NAME_CSS_SELECTOR = ".gdtm div a img";

	String IMAGE_CSS_SELECTOR = "#img";

	int RATE_STAR_WIDTH = 16;

	int BOOKS_PER_PAGE = 36;
	int PAGES_PER_PAGE = 40;
	String PAGE_INDEX = "page_index";

	String BOOK_PAGE_PARAM = "page";
	String PAGE_PAGE_PARAM = "p";
	String POSITION = "position";

	String SEARCH_PREFERENCES = "search_references";
}
