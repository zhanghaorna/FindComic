package com.zhr.searchcomic;

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
	//下载状态，默认为-1，代表未下载,从数据库中获取，或者广播更改数据
	//其他状态与Sql中保持一致
	private int download_status = -1;
	//页面数，从广播接收数据(章节页面数)
	private int page = 0;

	

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

	public int getDownload_status() {
		return download_status;
	}

	public void setDownload_status(int download_status) {
		this.download_status = download_status;
	}

	

}
