package com.zhr.searchcomic;

import java.io.Serializable;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月4日
 * @description
 */
public class ComicChapter{


	//漫画第几话
	private String chapter;
	//改话详细的地址
	private String url;
	//是否选中下载
	private Boolean choose;
	

	public String getChapter() {
		return chapter;
	}

	public void setChapter(String chapter) {
		this.chapter = chapter;
	}

	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean getChoose() {
		return choose;
	}

	public void setChoose(Boolean choose) {
		this.choose = choose;
	}
	
	public void changeChoose()
	{
		this.choose = !this.choose;
	}
	
	
}
