package com.ntk.ehcrawler;

public class EHConstants {
	public static final String HOST = "https://e-hentai.org/";
	public static final String SEARCH_TERM = "http://g.e-hentai.org/?page={startPage?}&f_search={searchTerms}&f_apply=Apply+Filter";
	public static final String TABLE_CSS_SELECTOR = ".ido .itg";
	public static final String ITEM_CSS_SELECTOR = TABLE_CSS_SELECTOR+" .id1";
	public static final String ITEM_TITLE_CSS_SELECTOR = ".id2 a";
	public static final String ITEM_IMAGE_CSS_SELECTOR = ".id3 a img";
	public static final String ITEM_TYPE_CSS_SELECTOR = ".id4 .id41";
	public static final String ITEM_FILE_COUNT_CSS_SELECTOR = ".id4 .id42";
	public static final String ITEM_STAR_RATE_CSS_SELECTOR = ".id4 .id43";
	public static final String ITEM_TORRENT_CSS_SELECTOR = ".id4 .id44 a";
	public static final int RATE_STAR_WIDTH = 16;
	
	
}
