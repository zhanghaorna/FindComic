package com.zhr.database;

import java.util.List;

import android.content.Context;




import com.zhr.sqlitedao.ComicDownload;
import com.zhr.sqlitedao.ComicDownloadDao;
import com.zhr.sqlitedao.DaoSession;
import com.zhr.util.ComicApplication;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年8月10日
 * @description
 */
public class DBComicDownloadHelper {
	private static DBComicDownloadHelper dbComicDownloadHelper;
	private DaoSession daoSession;
	private ComicDownloadDao comicDownloadDao;
	
	public static DBComicDownloadHelper getDbComicDownloadHelper()
	{
		return dbComicDownloadHelper;
	}
	
	private DBComicDownloadHelper()
	{
		
	}
	
	public static DBComicDownloadHelper getInstance(Context context)
	{
		if(dbComicDownloadHelper == null)
		{
			synchronized (DBComicDownloadHelper.class)
			{
				if(dbComicDownloadHelper == null)
				{
					dbComicDownloadHelper = new DBComicDownloadHelper();
					dbComicDownloadHelper.daoSession = ComicApplication.getDaoSession(context);
					dbComicDownloadHelper.comicDownloadDao = dbComicDownloadHelper.daoSession.getComicDownloadDao();
				}
			}
		}
		return dbComicDownloadHelper;
	}
	
	public long saveComicDownload(ComicDownload entity)
	{
		return comicDownloadDao.insertOrReplace(entity);
	}
	
	public ComicDownload getComicDownload(String comicName)
	{
		ComicDownload comicDownload = null;
		QueryBuilder<ComicDownload> qBuilder = comicDownloadDao.queryBuilder();
		qBuilder.where(ComicDownloadDao.Properties.ComicName.eq(comicName)).limit(1);
		List<ComicDownload> comicDownloads = qBuilder.list();
		if(comicDownloads != null&&comicDownloads.size() == 1)
		{
			comicDownload = comicDownloads.get(0);
		}
		if(comicDownload == null)
			comicDownload = new ComicDownload();
		return comicDownload;
	}
	
	public List<ComicDownload> getComicDownloads()
	{
		QueryBuilder<ComicDownload> qBuilder = comicDownloadDao.queryBuilder();
		return qBuilder.list();
	}
	
	public void deleteComicDownload(String comicName)
	{
		comicDownloadDao.deleteByKey(comicName);
	}
	
}
