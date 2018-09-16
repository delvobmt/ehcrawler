package com.ntk.reactor.model;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private String id;
    private List<Content> contents = new ArrayList<>();

    public Post(String id) {
        this.id = id;
    }

    public void addContent(Content content) {
        this.contents.add(content);
    }

    public List<Content> getContents() {
        return contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        return id != null ? id.equals(post.id) : post.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
