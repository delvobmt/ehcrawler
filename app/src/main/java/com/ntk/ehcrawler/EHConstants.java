package com.ntk.ehcrawler;

public class EHConstants {
	public static final String HOST = "https://e-hentai.org/";
	public static final String SEARCH_TERM = "http://g.e-hentai.org/?page={startPage?}&f_search={searchTerms}&f_apply=Apply+Filter";
	public static final String ITEM_CSS_SELECTOR = ".id1";
	public static final String ITEM_TITLE_CSS_SELECTOR = ".id2 a";
	public static final String ITEM_IMAGE_CSS_SELECTOR = ".id3 img";
	public static final String ITEM_TYPE_CSS_SELECTOR = ".id41";
	public static final String ITEM_FILE_COUNT_CSS_SELECTOR = ".id42";
	public static final String ITEM_STAR_RATE_CSS_SELECTOR = ".id43";
	
	public static final String DETAIL_IMAGE_CSS_SELECTOR = "#gd1 div";
	public static final String DETAIL_CSS_SELECTOR = "#gdd tr ";
	public static final String DETAIL_TAGLIST_CSS_SELECTOR = "#taglist tr";
	public static final String DETAIL_TAG_PRE_CSS_SELECTOR = ".tc";
	public static final String DETAIL_TAG_CSS_SELECTOR = ".gtl a,.gt a";
	public static final String DETAIL_KEY_CSS_SELECTOR = ".gdt1";
	public static final String DETAIL_VALUE_CSS_SELECTOR = ".gdt2";
	
	public static final String PAGE_THUMB_CSS_SELECTOR = ".gdtm div";
	public static final String PAGE_URL_CSS_SELECTOR = ".gdtm div a";
	public static final String PAGE_NAME_CSS_SELECTOR = ".gdtm div a img";
	
	public static final String IMAGE_CSS_SELECTOR = "#img";
	
	public static final int RATE_STAR_WIDTH = 16;
	
}
