package com.zhr.sqlitedao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.zhr.sqlitedao.ChapterInfo;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table CHAPTER_INFO.
*/
public class ChapterInfoDao extends AbstractDao<ChapterInfo, Long> {

    public static final String TABLENAME = "CHAPTER_INFO";

    /**
     * Properties of entity ChapterInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property ComicName = new Property(1, String.class, "comicName", false, "COMIC_NAME");
        public final static Property ChapterName = new Property(2, String.class, "chapterName", false, "CHAPTER_NAME");
        public final static Property ComicUrl = new Property(3, String.class, "comicUrl", false, "COMIC_URL");
    };


    public ChapterInfoDao(DaoConfig config) {
        super(config);
    }
    
    public ChapterInfoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'CHAPTER_INFO' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'COMIC_NAME' TEXT NOT NULL ," + // 1: comicName
                "'CHAPTER_NAME' TEXT NOT NULL ," + // 2: chapterName
                "'COMIC_URL' TEXT NOT NULL );"); // 3: comicUrl
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'CHAPTER_INFO'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, ChapterInfo entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getComicName());
        stmt.bindString(3, entity.getChapterName());
        stmt.bindString(4, entity.getComicUrl());
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public ChapterInfo readEntity(Cursor cursor, int offset) {
        ChapterInfo entity = new ChapterInfo( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // comicName
            cursor.getString(offset + 2), // chapterName
            cursor.getString(offset + 3) // comicUrl
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, ChapterInfo entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setComicName(cursor.getString(offset + 1));
        entity.setChapterName(cursor.getString(offset + 2));
        entity.setComicUrl(cursor.getString(offset + 3));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(ChapterInfo entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(ChapterInfo entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
