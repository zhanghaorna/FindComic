package com.zhr.database;

import java.util.List;

import android.content.Context;

import com.zhr.sqlitedao.ComicDownloadDetail;
import com.zhr.sqlitedao.ComicDownloadDetailDao;
import com.zhr.sqlitedao.ComicDownloadDetailDao.Properties;
import com.zhr.sqlitedao.DaoSession;
import com.zhr.util.ComicApplication;
import com.zhr.util.Constants;

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
		qBuilder.where(ComicDownloadDetailDao.Properties.ComicName.eq(comicName))
				.orderAsc(ComicDownloadDetailDao.Properties.Status)
				.orderAsc(ComicDownloadDetailDao.Properties.Chapter);
		return qBuilder.list();
	}
	
	public List<ComicDownloadDetail> getUnfinishedDownloadDetails(String comicName)
	{
		QueryBuilder<ComicDownloadDetail> qBuilder = comicDownloadDetailDao.queryBuilder();
		qBuilder.where(ComicDownloadDetailDao.Properties.Status.eq(Constants.PAUSED)).orderAsc(Properties.Chapter);
		return qBuilder.list();
	}
	
	public ComicDownloadDetail getComicDownloadDetail(String comicName,String chapterName)
	{
		QueryBuilder<ComicDownloadDetail> qBuilder = comicDownloadDetailDao.queryBuilder();
		qBuilder.where(ComicDownloadDetailDao.Properties.ComicName.eq(comicName))
					.where(ComicDownloadDetailDao.Properties.Chapter.eq(chapterName)).limit(1);
		List<ComicDownloadDetail> cDetails = qBuilder.list();
		if(cDetails != null&&cDetails.size() == 1)
		{
			return cDetails.get(0);
		}
		else
		{
			return null;
		}
	}
	
	public void deleteDownloadDetails(String comicName)
	{
		List<ComicDownloadDetail> cDetails = getComicDownloadDetails(comicName);
		if(cDetails != null&&cDetails.size() != 0)
		{
			comicDownloadDetailDao.deleteInTx(cDetails);
		}
	}
	
	public void deleteChooseDetails(List<ComicDownloadDetail> cDetails)
	{
		if(cDetails != null&&cDetails.size() > 0)
			comicDownloadDetailDao.deleteInTx(cDetails);
	}
}
