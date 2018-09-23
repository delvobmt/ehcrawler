package com.ntk.reactor.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Post {
    private String id;
    private List<Content> contents = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private String url;
    private boolean isLoaded;

    public Post(String id) {
        this.id = id;
    }

    public void addTag(String tag){
        tags.add(tag);
    }

    public List<String> getTags() {
        return tags;
    }

    public void addContent(Content content) {
        if (content != null) {
            this.contents.add(content);
        }
    }

    public void addContent(Collection contents) {
        this.contents.addAll(contents);
    }

    public List<Content> getContents() {
        return contents;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
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
