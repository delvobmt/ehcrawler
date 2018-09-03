package com.ntk.reactor;

import android.util.Log;

import com.ntk.reactor.model.ImageContent;
import com.ntk.reactor.model.Post;
import com.ntk.reactor.model.ReactorContentTypes;
import com.ntk.reactor.model.VideoGifContent;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ReactorUtils {

    private static final String LOG_TAG = "LOG_" + ReactorUtils.class.getSimpleName();

    public static List<Post> load(String tag, int index){
        try {
            prepareCookies();
            String url = ReactorConstants.HOST;
            url = StringUtil.isBlank(tag) ? url : url.concat("/tag/").concat(tag);
            url = index == 0 ? url : url.concat("/").concat(String.valueOf(index));

            Document document = Jsoup.connect(url).cookies(ContextHolder.getCookies()).get();
            return load(document);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private static List<Post> load(Document document) {
        List<Post> posts = new ArrayList<>();
        int maxPage = getMaxPage(document);
        System.out.println(maxPage);
        Elements elements = document.select(".postContainer");
        for (Element e: elements) {
            Post post = new Post();
            Elements contents = e.select(".post_content .image");
            for(Element content :contents) {
                String className = content.child(0).className();
                className = StringUtil.isBlank(className) ? "prettyPhotoLink" : className;

                switch (ReactorContentTypes.valueOf(className)){
                    case prettyPhotoLink: {
                        String src = content.select("img").attr("src");
                        post.addContent(new ImageContent(src));
                    }
                    break;
                    case video_gif_holder: {
                        String posterSrc = content.select("video").attr("poster");
                        if(StringUtil.isBlank(posterSrc)){
                            posterSrc = content.select("img").attr("src");
                        }
                        String ImageSrc = content.select(".video_gif_source").attr("href");
                        post.addContent(new VideoGifContent(posterSrc, ImageSrc));
                    }
                    break;
                    default:
                        Log.i(LOG_TAG, String.format("class name %s is not supported", className));
                }
            }
            if(!post.getContents().isEmpty())
                posts.add(post);
        }
        return posts;
    }

    private static boolean isNext(Document document) {
        return document.select(".prev").isEmpty();
    }

    private static int getMaxPage(Document document) {
        try {
            return Integer.valueOf(document.select(".pagination_expanded").get(0).child(0).text());
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public static Map<String, String> prepareCookies() throws IOException {
        Map<String, String> cookies = ContextHolder.getCookies();
        if (cookies == null || cookies.isEmpty()) {
            cookies = getCookies();
        }
        ContextHolder.setCookies(cookies);
        return cookies;
    }

    public static Map<String, String> getCookies() throws IOException {
        Connection connection = Jsoup.connect(ReactorConstants.HOST);
        Map<String, String> cookies = connection.execute().cookies();
        Log.i(LOG_TAG, String.format("_getCookies_ return %s", cookies));
        return cookies;
    }
}
