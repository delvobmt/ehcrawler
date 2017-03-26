package com.ntk.ehcrawler.model;

import android.util.Pair;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Book implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 2065000192736021013L;
    private String title;
    private String url;
    private String imageSrc;
    private int fileCount;
    private int rate;
    private String type;
    private Map<String, String> infoMap;
    private Map<String, Set<String>> tagMap;
    private LinkedHashMap<String, String> pageMap;

    public Book() {

    }

    public Book(String title, String url, String imageSrc, int fileCount, int rate, String type) {
        this.title = title;
        this.url = url;
        this.imageSrc = imageSrc;
        this.fileCount = fileCount;
        this.rate = rate;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Book [title=" + title + ", imageSrc=" + imageSrc + ", fileCount=" + fileCount + ", rate=" + rate
                + ", type=" + type + "]";
    }

    public Map<String, String> getInfoMap() {
        return infoMap;
    }

    public void setInfoMap(Map<String, String> infoMap) {
        this.infoMap = infoMap;
    }

    public Map<String, Set<String>> getTagMap() {
        return tagMap;
    }

    public void setTagMap(Map<String, Set<String>> tagMap) {
        this.tagMap = tagMap;
    }

    public LinkedHashMap<String, String> getPageMap() {
        return pageMap;
    }

    public void setPageMap(LinkedHashMap<String, String> pageMap) {
        this.pageMap = pageMap;
    }

}
