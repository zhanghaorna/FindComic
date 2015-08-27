package com.zhr.database;

import java.util.List;

import android.content.Context;

import com.zhr.sqlitedao.ComicInfo;
import com.zhr.sqlitedao.ComicInfoDao;
import com.zhr.sqlitedao.DaoSession;
import com.zhr.util.ComicApplication;

import de.greenrobot.dao.query.QueryBuilder;

public class DBComicInfoHelper {
	private static DBComicInfoHelper instance;
	private DaoSession daoSession;
	private ComicInfoDao comicInfoDao;
	
	public static DBComicInfoHelper getInstance(Context context)
	{
		if(instance == null)
		{
			synchronized (DBComicInfoHelper.class)
			{
				if(instance == null)
				{
					instance = new DBComicInfoHelper();
					instance.daoSession = ComicApplication.getDaoSession(context);
					instance.comicInfoDao = instance.daoSession.getComicInfoDao();
				}
			}
		}
		return instance;
	}
	
	private DBComicInfoHelper()
	{
		
	}
	
	public void saveComicInfo(ComicInfo comicInfo)
	{
		comicInfoDao.insert(comicInfo);
	}
	
	public ComicInfo getComicInfo(String comicName)
	{
		QueryBuilder<ComicInfo> qBuilder = comicInfoDao.queryBuilder();
		qBuilder.where(ComicInfoDao.Properties.ComicName.eq(comicName)).limit(1);
		List<ComicInfo> comicInfos = qBuilder.list();
		if(comicInfos != null&&comicInfos.size() == 1)
		{
			return comicInfos.get(0);
		}
		else
		{
			return null;
		}
	}
	
	public void deleteComicInfo(String comicName)
	{
		comicInfoDao.deleteByKey(comicName);
	}
}
