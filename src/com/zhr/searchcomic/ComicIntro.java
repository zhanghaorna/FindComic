package com.zhr.searchcomic;
/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月2日
 * @description
 */
public class ComicIntro {
	private String imageUrl;
	private String title;
	private String author;
	private String update;
	private boolean finished;
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getUpdate() {
		return update;
	}
	public void setUpdate(String update) {
		this.update = update;
	}
	public boolean isFinished() {
		return finished;
	}
	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	

}
