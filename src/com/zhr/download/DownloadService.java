package com.zhr.download;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import com.zhr.database.DBComicDownloadDetailHelper;
import com.zhr.database.DBComicDownloadHelper;
import com.zhr.findcomic.R;
import com.zhr.sqlitedao.ComicDownload;
import com.zhr.sqlitedao.ComicDownloadDetail;
import com.zhr.util.Constants;





import android.R.integer;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.RemoteViews;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月29日
 * @description
 */
public class DownloadService extends Service{
	
	private Queue<ComicDownloadThread> downloadComics;
	private ExecutorService singleThreadPool;
	
	private NotificationManager nManager;
	//下载通知栏
	private Notification downloadNotification;
	//通知栏的自定义view
	private RemoteViews remoteViews;
	//通知栏ID
	private int notifyId = 2;
	//接受一个章节下载完成的广播
	private LocalBroadcastManager lbManager;
	private DownloadBroadcast downloadBroadcast;
	
	public static final String CHAPTER_FINISHING_OR_PAUSED = "download_chapter_finished_or_paused";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		downloadComics = new LinkedBlockingQueue<ComicDownloadThread>();
		//由于onReceive和onstartCommand都是在ui线程进行操作，不会产生安全问题
//		downloadComics = (Queue<ComicDownloadThread>) Collections.synchronizedCollection(downloadComics);
		singleThreadPool = Executors.newSingleThreadExecutor();
		nManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		downloadBroadcast = new DownloadBroadcast();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CHAPTER_FINISHING_OR_PAUSED);
		lbManager = LocalBroadcastManager.getInstance(getBaseContext());
		lbManager.registerReceiver(downloadBroadcast, intentFilter);
	}
	
	private void showNotification()
	{
		remoteViews = new RemoteViews(getPackageName(), R.layout.comic_download_service);
		remoteViews.setTextViewText(R.id.title, "漫画下载任务");
		remoteViews.setTextViewText(R.id.content, "漫画下载中...");
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getBaseContext())
			.setContent(remoteViews)
			.setSmallIcon(R.drawable.ic_launcher)
			.setTicker("漫画下载任务");
		downloadNotification = mBuilder.build();
		nManager.notify(notifyId, downloadNotification);
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
					comicDownload.setDownloadDate(new Date());
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
						cDetail.setStatus(Constants.WAITING);
						comicDownload.getComicDownloadDetailList().add(cDetail);
						details.add(cDetail);
						ComicDownloadThread downloadThread = new ComicDownloadThread(cDetail, getBaseContext());
						downloadComics.add(downloadThread);
						singleThreadPool.submit(downloadThread);
					}
					DBComicDownloadDetailHelper.getInstance(getBaseContext()).saveComicDownloadDetails(details);
					
					//显示通知栏
					if(downloadNotification == null)
					{
						showNotification();
					}
					else
					{
						remoteViews.setTextViewText(R.id.content, "漫画下载中...");
						nManager.notify(notifyId, downloadNotification);
					}
				}								
			}
		}
	
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		lbManager.unregisterReceiver(downloadBroadcast);
	}
	
	private class DownloadBroadcast extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent != null)
			{
				if(intent.getAction().equals(CHAPTER_FINISHING_OR_PAUSED))
				{
					int size = downloadComics.size();
					int waiting_or_downloading = 0;
					for(int i = 0;i < size;i++)
					{
						ComicDownloadThread cThread = downloadComics.poll();
						int status = cThread.getDownloadStatus();
						if(status != Constants.FINISHED)
						{
							downloadComics.add(cThread);
							if(status != Constants.PAUSED)
								waiting_or_downloading++;
						}
						else
						{
							cThread.clear();
						}
						
					}
					if(downloadComics.size() == 0)
					{
						remoteViews.setTextViewText(R.id.content, "漫画已下载完成!");
						nManager.notify(notifyId, downloadNotification);
					}
					else if(waiting_or_downloading == 0)
					{
						remoteViews.setTextViewText(R.id.content, "下载已暂停");
						nManager.notify(notifyId, downloadNotification);
					}
				}
			}
			
		}
		
	}
	
}
