package com.ntk.reactor.model;

import java.util.List;

public class VideoGifContent extends Content{
    private String postSrc;
    List<String> src;

    public VideoGifContent(String postSrc, List<String> src, int width, int height) {
        super(width, height);
        this.postSrc = postSrc;
        this.src = src;
    }

    public List<String> getSrc() {
        return src;
    }

    public void setSrc(List<String> src) {
        this.src = src;
    }

    public String getPostSrc() {
        return postSrc;
    }

    public void setPostSrc(String postSrc) {
        this.postSrc = postSrc;
    }
}
