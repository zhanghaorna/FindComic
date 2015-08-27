package com.zhr.database;

import java.util.List;

import android.content.Context;

import com.zhr.sqlitedao.ChapterInfo;
import com.zhr.sqlitedao.ChapterInfoDao;
import com.zhr.sqlitedao.DaoSession;
import com.zhr.util.ComicApplication;

import de.greenrobot.dao.query.QueryBuilder;

public class DBChapterInfoHelper {
	private static DBChapterInfoHelper instance;
	private DaoSession daoSession;
	private ChapterInfoDao chapterInfoDao;
	
	private DBChapterInfoHelper()
	{
		
	}
	
	public static DBChapterInfoHelper getInstance(Context context)
	{
		if(instance == null)
		{
			synchronized (DBChapterInfoHelper.class)
			{
				if(instance == null)
				{
					instance = new DBChapterInfoHelper();
					instance.daoSession = ComicApplication.getDaoSession(context);
					instance.chapterInfoDao = instance.daoSession.getChapterInfoDao();
				}
			}
		}
		return instance;
	}
	
	public void saveChapterInfo(List<ChapterInfo> chapterInfos)
	{
		chapterInfoDao.insertInTx(chapterInfos);
	}
	
	public List<ChapterInfo> getChapterInfos(String comicName)
	{
		QueryBuilder<ChapterInfo> qBuilder = chapterInfoDao.queryBuilder();
		qBuilder.where(ChapterInfoDao.Properties.ComicName.eq(comicName));
		if(qBuilder.list() == null||qBuilder.list().size() == 0)
			return null;
		else
			return qBuilder.list();
	}
	
	public void deleteChapterInfos(String comicName)
	{
		QueryBuilder<ChapterInfo> qBuilder = chapterInfoDao.queryBuilder();
		qBuilder.where(ChapterInfoDao.Properties.ComicName.eq(comicName));
		List<ChapterInfo> cInfos = qBuilder.list();
		if(cInfos != null&&cInfos.size() > 0)
		{
			chapterInfoDao.deleteInTx(cInfos);
		}
	}
}
