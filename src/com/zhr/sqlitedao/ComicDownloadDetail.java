package com.zhr.sqlitedao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table COMIC_DOWNLOAD_DETAIL.
 */
public class ComicDownloadDetail {

    private Long id;
    /** Not-null value. */
    private String comicName;
    /** Not-null value. */
    private String chapter;
    private int pageNum;
    private int finishNum;
    private int status;
    /** Not-null value. */
    private String url;

    public ComicDownloadDetail() {
    }

    public ComicDownloadDetail(Long id) {
        this.id = id;
    }

    public ComicDownloadDetail(Long id, String comicName, String chapter, int pageNum, int finishNum, int status, String url) {
        this.id = id;
        this.comicName = comicName;
        this.chapter = chapter;
        this.pageNum = pageNum;
        this.finishNum = finishNum;
        this.status = status;
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getComicName() {
        return comicName;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setComicName(String comicName) {
        this.comicName = comicName;
    }

    /** Not-null value. */
    public String getChapter() {
        return chapter;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getFinishNum() {
        return finishNum;
    }

    public void setFinishNum(int finishNum) {
        this.finishNum = finishNum;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /** Not-null value. */
    public String getUrl() {
        return url;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setUrl(String url) {
        this.url = url;
    }

}
