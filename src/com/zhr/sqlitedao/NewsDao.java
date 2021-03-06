package com.zhr.sqlitedao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.zhr.sqlitedao.News;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table NEWS.
*/
public class NewsDao extends AbstractDao<News, Long> {

    public static final String TABLENAME = "NEWS";

    /**
     * Properties of entity News.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Tag = new Property(1, String.class, "tag", false, "TAG");
        public final static Property Time = new Property(2, java.util.Date.class, "time", false, "TIME");
        public final static Property Title = new Property(3, String.class, "title", false, "TITLE");
        public final static Property Summary = new Property(4, String.class, "summary", false, "SUMMARY");
        public final static Property ImagePath = new Property(5, String.class, "imagePath", false, "IMAGE_PATH");
        public final static Property From = new Property(6, String.class, "from", false, "FROM");
        public final static Property ContentUrl = new Property(7, String.class, "contentUrl", false, "CONTENT_URL");
    };


    public NewsDao(DaoConfig config) {
        super(config);
    }
    
    public NewsDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'NEWS' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'TAG' TEXT," + // 1: tag
                "'TIME' INTEGER NOT NULL ," + // 2: time
                "'TITLE' TEXT NOT NULL ," + // 3: title
                "'SUMMARY' TEXT," + // 4: summary
                "'IMAGE_PATH' TEXT," + // 5: imagePath
                "'FROM' TEXT NOT NULL ," + // 6: from
                "'CONTENT_URL' TEXT NOT NULL );"); // 7: contentUrl
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'NEWS'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, News entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String tag = entity.getTag();
        if (tag != null) {
            stmt.bindString(2, tag);
        }
        stmt.bindLong(3, entity.getTime().getTime());
        stmt.bindString(4, entity.getTitle());
 
        String summary = entity.getSummary();
        if (summary != null) {
            stmt.bindString(5, summary);
        }
 
        String imagePath = entity.getImagePath();
        if (imagePath != null) {
            stmt.bindString(6, imagePath);
        }
        stmt.bindString(7, entity.getFrom());
        stmt.bindString(8, entity.getContentUrl());
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public News readEntity(Cursor cursor, int offset) {
        News entity = new News( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // tag
            new java.util.Date(cursor.getLong(offset + 2)), // time
            cursor.getString(offset + 3), // title
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // summary
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // imagePath
            cursor.getString(offset + 6), // from
            cursor.getString(offset + 7) // contentUrl
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, News entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTag(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setTime(new java.util.Date(cursor.getLong(offset + 2)));
        entity.setTitle(cursor.getString(offset + 3));
        entity.setSummary(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setImagePath(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setFrom(cursor.getString(offset + 6));
        entity.setContentUrl(cursor.getString(offset + 7));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(News entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(News entity) {
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
