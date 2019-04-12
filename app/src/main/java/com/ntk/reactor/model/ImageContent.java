package com.ntk.reactor.model;

public class ImageContent extends Content{
    private String src;

    public ImageContent(String src, int width, int height) {
        super(width,height);
        this.src = src;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }
}
