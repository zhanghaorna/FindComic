package com.zhr.database;

import java.util.List;

import android.content.Context;

import com.zhr.findcomic.R.id;
import com.zhr.sqlitedao.DaoSession;
import com.zhr.sqlitedao.News;
import com.zhr.sqlitedao.NewsDao;
import com.zhr.util.ComicApplication;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月28日
 * @description
 */
public class DBNewsHelper {
	private static DBNewsHelper dbNewsHelper;
	private DaoSession daoSession;
	private NewsDao newsDao;
	
	public DBNewsHelper()
	{
		
	}
	
	public synchronized static DBNewsHelper getInstance(Context context)
	{
		if(dbNewsHelper == null)
		{
			synchronized (DBNewsHelper.class) {
				if(dbNewsHelper == null)
				{
					dbNewsHelper = new DBNewsHelper();
					dbNewsHelper.daoSession = ComicApplication.getDaoSession(context);
					dbNewsHelper.newsDao = dbNewsHelper.daoSession.getNewsDao();
				}
			}
		}
		return dbNewsHelper;
	}
	
	public long saveNews(News news)
	{
		if(newsDao.count() > 50)
		{
			newsDao.deleteAll();
		}
		return newsDao.insert(news);
	}
	
	public List<News> queryNews()
	{
		QueryBuilder<News> qBuilder = newsDao.queryBuilder();
		qBuilder.orderAsc(NewsDao.Properties.Time).limit(10);
		return qBuilder.list();
	}

}
