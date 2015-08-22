package com.zhr.download;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import com.zhr.database.DBComicDownloadDetailHelper;
import com.zhr.database.DBComicDownloadHelper;
import com.zhr.database.DBNewsHelper;
import com.zhr.findcomic.R;
import com.zhr.findcomic.R.id;
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
import android.os.Binder;
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
	private int error_notifyId = 3;
	
	//接受一个章节下载完成的广播
	private LocalBroadcastManager lbManager;
	private DownloadBroadcast downloadBroadcast;
	
	public static final String CHAPTER_FINISHING_OR_PAUSED = "download_chapter_finished_or_paused";
	public static final String NETWORK_ERROR = "network_error";
	
	
	private final IBinder mBinder = new LocalBinder();
	
	public class LocalBinder extends Binder
	{
		DownloadService getService()
		{
			return DownloadService.this;
		}
	}
	
	public boolean isDownloading()
	{
		return downloadComics.size() > 0;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
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
		intentFilter.addAction(NETWORK_ERROR);
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
	
	public void startDownload(String comicName)
	{
		List<ComicDownloadDetail> cDetails = DBComicDownloadDetailHelper.getInstance(getApplicationContext())
				.getUnfinishedDownloadDetails(comicName);
		if(cDetails != null&&cDetails.size() != 0)
		{
			for(ComicDownloadDetail cDetail:cDetails)
			{
				cDetail.setStatus(Constants.WAITING);
				ComicDownloadThread downloadThread = new ComicDownloadThread(cDetail, DownloadService.this);
				downloadComics.add(downloadThread);
				singleThreadPool.submit(downloadThread);
				//是否保存，待验证
				
			}
		}
		ComicDownload cDownload = DBComicDownloadHelper.getInstance(getApplicationContext())
				.getComicDownload(comicName);
		cDownload.setStatus(Constants.WAITING);
		DBComicDownloadHelper.getInstance(getApplicationContext()).saveComicDownload(cDownload);
	}
	
	public void pauseDownload(String comicName)
	{
		int size = downloadComics.size();
		List<ComicDownloadDetail> cDetails = new ArrayList<ComicDownloadDetail>();
		for(int i = 0;i < size;i++)
		{
			ComicDownloadThread cThread = downloadComics.poll();
			if(cThread == null)
				break;
			
			if(cThread.getDownloadDetail().getComicName().equals(comicName))
			{
				cThread.pauseDownload();
				cThread.getDownloadDetail().setStatus(Constants.PAUSED);
				cDetails.add(cThread.getDownloadDetail());
				cThread.clear();
			}
			else 
			{
				downloadComics.add(cThread);
			}
		}
		//统一保存
		if(cDetails.size() > 0)
			DBComicDownloadDetailHelper.getInstance(getApplicationContext())
				.saveComicDownloadDetails(cDetails);
		ComicDownload cDownload = DBComicDownloadHelper.getInstance(getApplicationContext())
				.getComicDownload(comicName);
		cDownload.setStatus(Constants.PAUSED);
		DBComicDownloadHelper.getInstance(getApplicationContext()).saveComicDownload(cDownload);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null)
		{
			Bundle bundle = intent.getExtras();
			//直接从漫画页选择下载
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
					comicDownload.setChapterNum(downloadChapterNum + comicDownload.getChapterNum());
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
						ComicDownloadThread downloadThread = new ComicDownloadThread(cDetail, DownloadService.this);
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
			//从漫画也
//			else if(bundle != null&&bundle.getString("from").equals("download_manage"))
		}
	
		return START_STICKY;
	}
	
	private int checkComicStatus(String comicName)
	{
		List<ComicDownloadDetail> cDetails = DBComicDownloadDetailHelper.getInstance(getApplicationContext()).getComicDownloadDetails(comicName);
		boolean downloading = false;
		boolean paused = false;
		boolean waiting = false;
		
		if(cDetails != null&&cDetails.size() != 0)
		{
			for(int i = 0;i < cDetails.size();i++)
			{
				if(cDetails.get(i).getStatus() == Constants.DOWNLOADING)
				{
					downloading = true;
					break;
				}
				else if(cDetails.get(i).getStatus() == Constants.PAUSED)
				{
					paused = true;
				}
				else if(cDetails.get(i).getStatus() == Constants.WAITING)
				{
					waiting = true;
				}
			}
			
		}
		if(downloading)
		{
			setComicStatus(comicName, Constants.DOWNLOADING);
			return Constants.DOWNLOADING;
		}
		else if(paused)
		{
			setComicStatus(comicName, Constants.PAUSED);
			return Constants.PAUSED;
		}
		else if(waiting)
		{
			setComicStatus(comicName, Constants.WAITING);
			return Constants.WAITING;
		}
		else
		{
			setComicStatus(comicName, Constants.FINISHED);
			return Constants.FINISHED;
		}		
	}
	
	private void setComicStatus(String comicName,int status)
	{
		ComicDownload cDownload = DBComicDownloadHelper.getInstance(getApplicationContext()).getComicDownload(comicName);
		if(cDownload != null&&cDownload.getStatus() != status)
		{
			cDownload.setStatus(status);
			DBComicDownloadHelper.getInstance(getApplicationContext()).saveComicDownload(cDownload);
		}

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
				//逻辑有问题
				if(intent.getAction().equals(CHAPTER_FINISHING_OR_PAUSED))
				{
					int size = downloadComics.size();
					int waiting_or_downloading = 0;
					for(int i = 0;i < size;i++)
					{
						ComicDownloadThread cThread = downloadComics.poll();
						if(cThread == null)
							break;
						int status = cThread.getDownloadStatus();
						if(status != Constants.FINISHED&&status != Constants.PAUSED)
						{
							downloadComics.add(cThread);
						}
						else 
						{
							if(status == Constants.PAUSED)
							{
								cThread.clear();
							}
							waiting_or_downloading++;
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
					checkComicStatus(intent.getStringExtra("comicName"));
				}
				else if(intent.getAction().equals(NETWORK_ERROR))
				{
				
					remoteViews.setTextViewText(R.id.content, intent.getStringExtra("comicName")
							+ intent.getStringExtra("chapterName") + "下载失败");
					nManager.notify(error_notifyId,downloadNotification);
					
					boolean downloading = false;
					
					Iterator<ComicDownloadThread> iterator = downloadComics.iterator();
					while(iterator.hasNext())
					{
						int status = iterator.next().getDownloadStatus();
						if(status == Constants.WAITING
								||status == Constants.DOWNLOADING)
						{
							downloading = true;
							break;
						}
					}
					if(!downloading)
					{
						downloadComics.clear();
						remoteViews.setTextViewText(R.id.content, "下载已停止");
						nManager.notify(notifyId, downloadNotification);
					}
					checkComicStatus(intent.getStringExtra("comicName"));
					
				}
			}
			
		}
		
	}
	
}
