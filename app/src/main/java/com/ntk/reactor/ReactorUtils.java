package com.ntk.reactor;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.ntk.reactor.model.Content;
import com.ntk.reactor.model.ImageContent;
import com.ntk.reactor.model.Post;
import com.ntk.reactor.model.VideoGifContent;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ReactorUtils {

    private static final String LOG_TAG = "LOG_" + ReactorUtils.class.getSimpleName();

    public static String getCurrentIndexKey(String tag) {
        return (TextUtils.isEmpty(tag))?
                ReactorConstants.INDEX_KEY
                :ReactorConstants.INDEX_KEY.concat("_").concat(tag);
    }

    public static List load(String tag, int index){
        Log.i(LOG_TAG, String.format("call load (%s, %s)", tag, index));
        try {
            prepareCookies();
            String url = ReactorConstants.HOST;
            if (!StringUtil.isBlank(tag)){
                Uri uri = Uri.parse(ReactorConstants.HOST);
                String host = uri.getHost();
                String scheme = uri.getScheme();
                url = scheme + "://" + host + "/tag/" + tag;
            }
            url = index == 0 ? url : url.concat("/").concat(String.valueOf(index));
            ReactorActivity.setDebugTextView(String.format("loading uri %s", url));
            Document document = Jsoup.connect(url).cookies(ContextHolder.getCookies()).get();
            int maxPage = getMaxPage(document);
            ReactorActivity.setDebugTextView(String.format("tag: %s idx: %d max %d", tag, index, maxPage));
            List<Post> posts = load(document);
            return Arrays.asList(posts, maxPage);

        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private static List<Post> load(Element document) {
        List<Post> posts = new ArrayList<>();
        Elements elements = document.select(".postContainer");
        for (int i = 0; i < elements.size(); i++) {
            Element e = elements.get(i);
            Post post = new Post(e.attr("id"));
            Elements select = e.select(".commentnum.toggleComments");
            String url = select.attr("href");
            String text = select.text();
            post.setCommentCount(text);
            post.setUrl(url);
            collectPostContent(e, post);
            collectPostTags(e, post);
            ReactorActivity.setDebugTextView(String.format("loading %s %d/%d", post.getUrl(), i, elements.size()));
            if(!post.getContents().isEmpty()) {
                if (!text.contains("0")) {
                    loadPost(post);
                }
                posts.add(post);
            }
        }
        return posts;
    }

    public static Post loadPost(Post post) {
        List<Content> contents = post.getContents();
        if(contents.isEmpty() || contents.size() == 1) {
            Log.i(LOG_TAG, String.format("load post Url=%s", post.getUrl()));
            String url = post.getUrl();
            if (!StringUtil.isBlank(url)) {
                try {
                    Uri uri = Uri.parse(ReactorConstants.HOST);
                    String host = uri.getHost();
                    String scheme = uri.getScheme();
                    url = scheme + "://" + host + url;
                    Document document = Jsoup.connect(url).cookies(ContextHolder.getCookies()).get();
                    List<Content> comments = collectPostComments(document);
                    post.addContent(comments);
                    return post;
//                Elements commentLists = document.select(".post_comment_list");
//                for(Element commentList : commentLists) {
//                    collectPostComments(commentList);
//                }
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
        }
        return post;
    }

    private static List<Content> collectPostComments(Element element) {
        ArrayList<Content> commentList = new ArrayList<>();
        Elements commentElements = element.select(".comment");
        for (Element commentElement : commentElements) {
            Elements imgageElements = commentElement.select(".image");
//            Elements children;
//            if(!txtElement.isEmpty()){
//                children = txtElement.get(0).children();
//            }else{
//                children = commentElement.children();
//            }
            for (Element e : imgageElements) {
                if(e.children().isEmpty()) continue;
                Element child = e.child(0);
                Content con = parseContent(child);
                if (con != null) {
                    commentList.add(con);
                }
            }
        }
        return commentList;
    }

    private static void collectPostTags(Element element, Post post) {
        Elements tagsElement = element.select(".taglist a");
        for (Element tag : tagsElement) {
            post.addTag(tag.text());
        }
    }

    private static void collectPostContent(Element element, Post post) {
        Elements contents = element.select(".post_content .image");
        for(Element content :contents) {
            Element child = content.child(0);
            post.addContent(parseContent(child));
        }
    }

    private static Content parseContent(Element content) {
        String className = content.className();
        if(StringUtil.isBlank(className) || "prettyPhotoLink".equals(className)) {
            return parseImageContent(content);
        } else if ("video_gif_holder".equals(className)) {
            return parseVideoContent(content);
        }
        return null;
    }

    private static ImageContent parseImageContent(Element child) {
        Elements img = child.select("img");
        String src = img.attr("src");
        int width = 0, height = 0;
        try {
            width = Integer.parseInt(child.attr("width"));
            height = Integer.parseInt(child.attr("height"));
        }catch (NumberFormatException e){
            Log.e(LOG_TAG, e.getMessage());
        }
        if(!StringUtil.isBlank(src)) {
            return new ImageContent(src, Integer.valueOf(width), Integer.valueOf(height));
        }
        return null;
    }

    private static VideoGifContent parseVideoContent(Element child) {
        String posterSrc = "";
        List<String> videoSrc = new ArrayList<>(3);

        int width = 0,height = 0;
        for(Element c : child.children()){
            if (c.tag().getName().equals("video")) {
                Elements img = c.select("img");
                posterSrc = img == null ? img.attr("src"): c.attr("poster");
                if(videoSrc.size() < 3) {
                    Elements sources = c.select("source");
                    for (Element source : sources){
                        videoSrc.add(source.attr("src"));
                    }
                }
                try {
                    width = Integer.parseInt(c.attr("width"));
                    height = Integer.parseInt(c.attr("height"));
                }catch (NumberFormatException e){
                    Log.e(LOG_TAG, e.getMessage());
                }

            }
            if(videoSrc.isEmpty()) {
                videoSrc.add(c.attr("href"));
            }
        }
        if(!videoSrc.isEmpty()) {
            return new VideoGifContent(posterSrc, videoSrc, width, height);
        }
        return null;
    }

    private static boolean isNext(Document document) {
        return document.select(".prev").isEmpty();
    }

    private static int getMaxPage(Document document) {
        try {
            return Integer.valueOf(document.select(".pagination_expanded").get(0).child(0).text());
        }catch (Exception e){
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
