package com.zhr.sqlitedao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table COMIC_RECORD.
 */
public class ComicRecord {

    private Long id;
    /** Not-null value. */
    private String name;
    /** Not-null value. */
    private String chapter;
    private int page;

    public ComicRecord() {
    }

    public ComicRecord(Long id) {
        this.id = id;
    }

    public ComicRecord(Long id, String name, String chapter, int page) {
        this.id = id;
        this.name = name;
        this.chapter = chapter;
        this.page = page;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getName() {
        return name;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setName(String name) {
        this.name = name;
    }

    /** Not-null value. */
    public String getChapter() {
        return chapter;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

}