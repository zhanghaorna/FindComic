package com.zhr.download;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import com.zhr.database.DBComicDownloadDetailHelper;
import com.zhr.database.DBComicDownloadHelper;
import com.zhr.sqlitedao.ComicDownload;
import com.zhr.sqlitedao.ComicDownloadDetail;
import com.zhr.util.Constants;




import android.app.IntentService;
import android.app.Service;
import android.content.Context;
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
	
	private Queue<Runnable> downloadComics;
	private ExecutorService singleThreadPool;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		downloadComics = new LinkedBlockingDeque<Runnable>();
		singleThreadPool = Executors.newSingleThreadExecutor();
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null)
		{
			Bundle bundle = intent.getExtras();
			if(bundle != null)
			{
				String comicName = bundle.getString("comicName");
				int downloadChapterNum = bundle.getInt("downloadChapterNum",0);
				String[] chapters = bundle.getStringArray("chapters");
				String[] urls = bundle.getStringArray("urls");
				ComicDownload comicDownload = null;
				if(comicName != null)
					comicDownload = DBComicDownloadHelper
						.getInstance(DownloadService.this).getComicDownload(comicName);
				if(comicDownload.getComicName() == null||comicDownload.getComicName().equals(""))
				{
					comicDownload.setComicName(comicName);
					comicDownload.setChapterNum(downloadChapterNum);
					comicDownload.setStatus(Constants.WAITING);	
					DBComicDownloadHelper.getInstance(getBaseContext()).saveComicDownload(comicDownload);
				}
				if(chapters != null&&urls != null)
				{
					List<ComicDownloadDetail> details = new ArrayList<ComicDownloadDetail>();
					for(int i = 0;i < chapters.length;i++)
					{
						ComicDownloadDetail cDetail = new ComicDownloadDetail();
						cDetail.setComicDownload(comicDownload);
						cDetail.setChapter(chapters[i]);
						cDetail.setComicName(comicName);
						cDetail.setUrl(urls[0]);	
						cDetail.setFinishNum(0);
						cDetail.setPageNum(0);
						cDetail.setStatus("0");
						comicDownload.getComicDownloadDetailList().add(cDetail);
						details.add(cDetail);
						ComicDownloadThread downloadThread = new ComicDownloadThread(cDetail, getBaseContext());
						downloadComics.add(downloadThread);
						singleThreadPool.submit(downloadThread);
					}
					DBComicDownloadDetailHelper.getInstance(getBaseContext()).saveComicDownloadDetails(details);
				}
				
				
			}
		}
	
		return START_STICKY;
	}
	
	
	

	
}
