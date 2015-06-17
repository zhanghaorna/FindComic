package com.zhr.util;

import com.zhr.findcomic.R.id;
import com.zhr.sqlitedao.DaoMaster;
import com.zhr.sqlitedao.DaoMaster.OpenHelper;
import com.zhr.sqlitedao.DaoSession;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月28日
 * @description
 */
public class ComicApplication extends Application{
	private static DaoSession daoSession;
	private static DaoMaster daoMaster;
	
	//ComicApplication可能会多次调用onCreate()，由于每有一个进程就会初始化一次Application
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

	}
	
	public static DaoSession getDaoSession(Context context)
	{
		if(daoSession == null)
		{
			if(daoMaster == null)
			{
				daoMaster = getDaoMaster(context);
			}
			synchronized (ComicApplication.class) {
				if(daoSession == null)
					daoSession = daoMaster.newSession();
			}

		}
		return daoSession;
	}
	
	public static DaoMaster getDaoMaster(Context context)
	{
		if(daoMaster == null)
		{
			synchronized (ComicApplication.class) {
				if(daoMaster == null)
				{
					OpenHelper helper = new DaoMaster.DevOpenHelper(context, "comic_db", null);
					daoMaster = new DaoMaster(helper.getWritableDatabase());
				}
			}
		}
		return daoMaster;
	}
}
