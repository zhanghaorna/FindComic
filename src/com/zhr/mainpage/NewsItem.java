package com.zhr.mainpage;
/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月26日
 * @description
 */
public class NewsItem {
	//新闻标签
	private String tag;
	//标题
	private String title;
	//时间
	private String time;
	//网页URL
	private String contentUrl;
	//图片URL
	private String imageUrl;
	
	public NewsItem()
	{
		
	}
	

	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		
		this.time = time;
	}


	public String getContentUrl() {
		return contentUrl;
	}


	public void setContentUrl(String contentUrl) {
		this.contentUrl = contentUrl;
	}


	public String getImageUrl() {
		return imageUrl;
	}


	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
	
}
