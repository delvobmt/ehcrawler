package com.ntk.ehcrawler.model;

import java.io.Serializable;

public class Book implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2065000192736021013L;
	private String title;
	private String imageSrc;
	private int fileCount;
	private int rate;
	private String type;
	
	public Book(String title, String imageSrc, int fileCount, int rate, String type) {
		this.title = title;
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
	
}
