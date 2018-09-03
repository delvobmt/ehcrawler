package com.ntk.reactor.model;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private List<Content> contents = new ArrayList<>();

    public void addContent(Content content) {
        this.contents.add(content);
    }

    public List<Content> getContents() {
        return contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }
}
