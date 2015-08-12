package com.zhr.database;

import java.util.List;

import android.content.Context;

import com.zhr.sqlitedao.ComicDownloadDetail;
import com.zhr.sqlitedao.ComicDownloadDetailDao;
import com.zhr.sqlitedao.DaoSession;
import com.zhr.util.ComicApplication;

import de.greenrobot.dao.query.QueryBuilder;

public class DBComicDownloadDetailHelper {
	private static DBComicDownloadDetailHelper instance;
	private DaoSession daoSession;
	private ComicDownloadDetailDao comicDownloadDetailDao;
	
	private DBComicDownloadDetailHelper()
	{
		
	}
	
	public static DBComicDownloadDetailHelper getInstance(Context context)
	{
		if(instance == null)
		{
			synchronized (DBComicDownloadDetailHelper.class) 
			{
				if(instance == null)
				{
					instance = new DBComicDownloadDetailHelper();
					instance.daoSession = ComicApplication.getDaoSession(context);
					instance.comicDownloadDetailDao = instance.daoSession.getComicDownloadDetailDao();
				}
			}
		}
		return instance;
	}
	
	public void saveComicDownloadDetail(ComicDownloadDetail detail)
	{
		comicDownloadDetailDao.insertOrReplace(detail);
	}
	
	public void saveComicDownloadDetails(List<ComicDownloadDetail> details)
	{
		comicDownloadDetailDao.insertOrReplaceInTx(details);
	}
	
	public List<ComicDownloadDetail> getComicDownloadDetails(String comicName)
	{
		QueryBuilder<ComicDownloadDetail> qBuilder = comicDownloadDetailDao.queryBuilder();
		qBuilder.where(ComicDownloadDetailDao.Properties.ComicName.eq(comicName));
		return qBuilder.list();
	}
	
	
}
