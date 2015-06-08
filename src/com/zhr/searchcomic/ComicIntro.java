package com.zhr.searchcomic;
/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月2日
 * @description
 */
public class ComicIntro {
	//漫画图片URL
	private String imageUrl;
	//漫画名字
	private String title;
	//漫画作者
	private String author;
	//漫画更新到多少话
	private String update;
	//是否完结
	private boolean finished;
	//漫画详情页URL
	private String introUrl;
	
	
	
	public String getIntroUrl() {
		return introUrl;
	}
	public void setIntroUrl(String introUrl) {
		this.introUrl = introUrl;
	}
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
