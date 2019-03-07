package com.ntk.reactor.model;

import java.util.List;

public class VideoGifContent extends Content{
    private String postSrc;
    List<String> src;

    public VideoGifContent() {
    }

    public VideoGifContent(String postSrc, List<String> src) {
        this.postSrc = postSrc;
        this.src = src;
    }

    public VideoGifContent(String postSrc) {
        this.postSrc = postSrc;
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
