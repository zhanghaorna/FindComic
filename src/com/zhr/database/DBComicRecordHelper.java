package com.zhr.database;

import java.util.List;

import android.app.DownloadManager.Query;
import android.content.Context;

import com.zhr.sqlitedao.ComicRecord;
import com.zhr.sqlitedao.ComicRecordDao;
import com.zhr.sqlitedao.DaoSession;
import com.zhr.util.ComicApplication;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年7月7日
 * @description
 */
public class DBComicRecordHelper {
	private static DBComicRecordHelper dbComicRecordHelper;
	private DaoSession daoSession;
	private ComicRecordDao comicRecordDao;
	
	private DBComicRecordHelper(){}
	
	public synchronized static DBComicRecordHelper getInstance(Context context)
	{
		if(dbComicRecordHelper == null)
		{
			synchronized (DBComicRecordHelper.class) {
				if(dbComicRecordHelper == null)
				{
					dbComicRecordHelper = new DBComicRecordHelper();
					dbComicRecordHelper.daoSession = ComicApplication.getDaoSession(context);
					dbComicRecordHelper.comicRecordDao =
							dbComicRecordHelper.daoSession.getComicRecordDao();
				}
			}
		}
		return dbComicRecordHelper;
	}
	
	public void saveRecord(ComicRecord record)
	{
		QueryBuilder<ComicRecord> qBuilder = comicRecordDao.queryBuilder();
		qBuilder.where(ComicRecordDao.Properties.Name.eq(record.getName())).limit(1);
		List<ComicRecord> result = qBuilder.list();
		//如果数据库中已经存在这部漫画，则更新最新信息
		if(result != null&&result.size() == 1)
		{
			record.setId(result.get(0).getId());
			comicRecordDao.update(record);
		}
		else if(result == null||result.size() == 0)
		{
			comicRecordDao.insert(record);
		}
	}
	
	//根据漫画名找是否存在漫画记录
	public ComicRecord getComicRecord(String name)
	{
		QueryBuilder<ComicRecord> qBuilder = comicRecordDao.queryBuilder();
		qBuilder.where(ComicRecordDao.Properties.Name.eq(name)).limit(1);
		List<ComicRecord> result = qBuilder.list();
		if(result != null&&result.size() == 1)
		{
			return result.get(0);
		}
		else {
			return null;
		}
	}
		
}
