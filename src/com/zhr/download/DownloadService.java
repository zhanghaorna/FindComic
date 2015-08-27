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
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
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
//	private LocalBroadcastManager lbManager;
	private DownloadBroadcast downloadBroadcast;
	
	public static final String CHAPTER_FINISHED = "chapter_finished";
	public static final String CHAPTER_PAUSED = "chapter_paused";
	public static final String NETWORK_ERROR = "network_error";
	public static final String DOWNLOAD_STATE_CHANGE = "download_state_change";
	public static final String DOWNLOAD_PAGE_FINISHED = "download_page_finished";
	
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
		intentFilter.addAction(CHAPTER_FINISHED);
		intentFilter.addAction(CHAPTER_PAUSED);
		intentFilter.addAction(NETWORK_ERROR);
		intentFilter.setPriority(1000);
//		lbManager = LocalBroadcastManager.getInstance(getBaseContext());
//		lbManager.registerReceiver(downloadBroadcast, intentFilter);
		registerReceiver(downloadBroadcast, intentFilter);
		
		//初始化广播相关
		remoteViews = new RemoteViews(getPackageName(), R.layout.comic_download_service);
		remoteViews.setTextViewText(R.id.title, "漫画下载任务");
		remoteViews.setTextViewText(R.id.content, "漫画下载中...");
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getBaseContext())
			.setContent(remoteViews)
			.setSmallIcon(R.drawable.ic_launcher)
			.setTicker("漫画下载任务");
		
		//点击通知后进入DownloadManageActivity
		Intent dmIntent = new Intent(this,DownloadManageActivity.class);
		//点击返回后能返回原来的Activity
		dmIntent.setAction(Intent.ACTION_MAIN);
		dmIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//		dmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
//				Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent dmPendingIntent = PendingIntent.getActivity(this,
				0, dmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		
		
//		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
////		stackBuilder.addParentStack(DownloadManageActivity.class);
//		stackBuilder.addNextIntent(dmIntent);
//		PendingIntent dmPendingIntent = stackBuilder.getPendingIntent(0,
//							PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(dmPendingIntent);
		downloadNotification = mBuilder.build();
	}
	
	private void showStartNotification()
	{
		remoteViews.setTextViewText(R.id.content, "漫画下载中...");
		nManager.notify(notifyId, downloadNotification);
	}
	
	private void showPauseNotification()
	{
		remoteViews.setTextViewText(R.id.content, "下载已暂停");
		nManager.notify(notifyId, downloadNotification);
	}
	
	public void startDownload(String comicName)
	{
		if(downloadComics.size() == 0)
		{
			showStartNotification();
		}
		List<ComicDownloadDetail> cDetails = DBComicDownloadDetailHelper.getInstance(getApplicationContext())
				.getUnfinishedDownloadDetails(comicName);
		if(cDetails != null&&cDetails.size() != 0)
		{
			for(ComicDownloadDetail cDetail:cDetails)
			{
				cDetail.setStatus(Constants.WAITING);
				ComicDownloadThread downloadThread = new ComicDownloadThread(cDetail, getApplicationContext());
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
	
	public void startDownload(ComicDownloadDetail cDetail)
	{
		if(downloadComics.size() == 0)
		{
			showStartNotification();
		}
		ComicDownloadThread downloadThread = new ComicDownloadThread(cDetail, getApplicationContext());
		downloadComics.add(downloadThread);
		singleThreadPool.submit(downloadThread);
		ComicDownload cDownload = DBComicDownloadHelper.getInstance(getApplicationContext())
				.getComicDownload(cDetail.getComicName());
		if(cDownload.getStatus() == Constants.PAUSED)
		{
			cDownload.setStatus(Constants.WAITING);
			DBComicDownloadHelper.getInstance(getApplicationContext()).saveComicDownload(cDownload);
		}
		
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
				cThread.getDownloadDetail().setStatus(Constants.PAUSED);
				cDetails.add(cThread.getDownloadDetail());
				cThread.pauseDownload();
			}
			else 
			{
				downloadComics.add(cThread);
			}
		}
		if(downloadComics.size() == 0)
		{
			showPauseNotification();
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
	
	//暂停单个下载线程
	public void pauseDownload(ComicDownloadDetail cDetail)
	{
		Iterator<ComicDownloadThread> iterator = downloadComics.iterator();
		while(iterator.hasNext())
		{
			ComicDownloadThread thread = iterator.next();
			if(thread.getDownloadDetail() == cDetail)
			{
				thread.pauseDownload();
				downloadComics.remove(thread);			
			}
		}
		
		if(downloadComics.size() == 0)
		{
			showPauseNotification();
		}
		List<ComicDownloadDetail> cDetails = DBComicDownloadDetailHelper
				.getInstance(getApplicationContext()).getComicDownloadDetails(cDetail.getComicName());
		int i = 0;
		for(;i < cDetails.size();i++)
		{
			if(cDetails.get(i).getStatus() == Constants.WAITING
					||cDetails.get(i).getStatus() == Constants.DOWNLOADING)
			{
				break;
			}
		}
		if(i == cDetails.size())
		{
			ComicDownload cDownload = DBComicDownloadHelper.getInstance(getApplicationContext())
					.getComicDownload(cDetail.getComicName());
			cDownload.setStatus(Constants.PAUSED);
			DBComicDownloadHelper.getInstance(getApplicationContext()).saveComicDownload(cDownload);
		}
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
					comicDownload.setStatus(Constants.WAITING);	
				}
				else
				{
					if(comicDownload.getStatus() == Constants.PAUSED
							||comicDownload.getStatus() == Constants.FINISHED)
						comicDownload.setStatus(Constants.WAITING);
				}
				comicDownload.setChapterNum(downloadChapterNum + comicDownload.getChapterNum());
				comicDownload.setDownloadDate(new Date());
				DBComicDownloadHelper.getInstance(getBaseContext()).saveComicDownload(comicDownload);
				if(chapters != null&&urls != null)
				{
					//如果没有正在下载的则显示下载通知
					if(chapters.length > 0&&downloadComics.size() == 0)
					{
						showStartNotification();
					}
					
					List<ComicDownloadDetail> details = new ArrayList<ComicDownloadDetail>();
					for(int i = 0;i < chapters.length;i++)
					{
						ComicDownloadDetail cDetail = new ComicDownloadDetail();
						cDetail.setChapter(chapters[i]);
						cDetail.setComicName(comicName);
						cDetail.setUrl(urls[0]);	
						cDetail.setFinishNum(0);
						cDetail.setPageNum(0);
						cDetail.setStatus(Constants.WAITING);
						details.add(cDetail);
						ComicDownloadThread downloadThread = new ComicDownloadThread(cDetail, getApplicationContext());
						downloadComics.add(downloadThread);
						singleThreadPool.submit(downloadThread);
					}
					DBComicDownloadDetailHelper.getInstance(getBaseContext()).saveComicDownloadDetails(details);
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
		unregisterReceiver(downloadBroadcast);
		Log.d("Comic", "service destory");
		
	}
	
	private class DownloadBroadcast extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent != null)
			{
				//逻辑有问题
				if(intent.getAction().equals(CHAPTER_FINISHED)
						||intent.getAction().equals(CHAPTER_PAUSED))
				{
					int size = downloadComics.size();
					boolean paused = false;
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
						else if(status == Constants.PAUSED) 
						{
							paused = true;
						}						
					}
					if(downloadComics.size() == 0)
					{
						if(paused)
						{
							showPauseNotification();
						}
						else if(intent.getAction().equals(CHAPTER_FINISHED))
						{
							remoteViews.setTextViewText(R.id.content, "下载已完成");
							nManager.notify(notifyId, downloadNotification);
						}
					}
					
					checkComicStatus(intent.getStringExtra("comicName"));
				}
				else if(intent.getAction().equals(NETWORK_ERROR))
				{
				
					remoteViews.setTextViewText(R.id.content, intent.getStringExtra("comicName")
							+ intent.getStringExtra("chapterName") + "下载失败");
					nManager.notify(error_notifyId,downloadNotification);
					
					
					int size = downloadComics.size();
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
					}
					if(downloadComics.size() == 0)
					{
						remoteViews.setTextViewText(R.id.content, "下载已停止");
						nManager.notify(notifyId, downloadNotification);
					}
					
					checkComicStatus(intent.getStringExtra("comicName"));
					
				}
			}
			
		}
		
	}
	
}
