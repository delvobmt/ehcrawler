package com.ntk.reactor.model;

public class VideoGifContent extends Content{
    private String postSrc, src;

    public VideoGifContent() {
    }

    public VideoGifContent(String postSrc, String src) {
        this.postSrc = postSrc;
        this.src = src;
    }

    public VideoGifContent(String postSrc) {
        this.postSrc = postSrc;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getPostSrc() {
        return postSrc;
    }

    public void setPostSrc(String postSrc) {
        this.postSrc = postSrc;
    }
}
