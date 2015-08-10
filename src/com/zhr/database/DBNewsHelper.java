package com.zhr.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.content.Context;
import android.util.Log;

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
public class DBNewsHelper{
	private static DBNewsHelper dbNewsHelper;
	private DaoSession daoSession;
	private NewsDao newsDao;
	
	public static DBNewsHelper getDbNewsHelper()
	{
		return dbNewsHelper;			
	}
	
	private DBNewsHelper()
	{
		
	}
	
	public static DBNewsHelper getInstance(Context context)
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
			deleteAllNews();
		}
		return newsDao.insert(news);
	}
	
	public void saveAllNews(List<News> news)
	{
		if(newsDao.count() > 50)
			deleteAllNews();
		
		newsDao.insertInTx(news);
	}
	
	public List<News> queryNews(String from)
	{
		QueryBuilder<News> qBuilder = newsDao.queryBuilder();
		qBuilder.where(NewsDao.Properties.From.eq(from)).orderAsc(NewsDao.Properties.Time).limit(10);
		return qBuilder.list();
	}
	
	public void alertNews(String oldPath,String newPath)
	{
		QueryBuilder<News> qBuilder = newsDao.queryBuilder();
		qBuilder.where(NewsDao.Properties.ImagePath.eq(oldPath)).limit(1);
		List<News> news = qBuilder.list();
		if(news != null&&news.size() == 1)
		{
			//这里的更改会影响DzNewsFragment里面NewsItem的更改
			news.get(0).setImagePath(newPath);
			newsDao.update(news.get(0));
		}		
	}
	
	public void deleteAllNews()
	{
		newsDao.deleteAll();
	}
	
	

}
