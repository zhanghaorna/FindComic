package com.zhr.download;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import com.zhr.sqlitedao.DownloadComic;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月29日
 * @description
 */
public class DownloadService extends Service{
	
	private Queue<DownloadComic> downloadComics;
	

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		downloadComics = new LinkedBlockingDeque<DownloadComic>();
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null)
		{
			Bundle bundle = intent.getExtras();
			if(bundle != null)
			{
				DownloadComic download = new DownloadComic();
				
			}
		}
		
		
		return START_STICKY;
	}
	
	

	
}
