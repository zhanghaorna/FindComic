package com.zhr.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.zhr.findcomic.R;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月29日
 * @description
 */
public class UpdateService extends IntentService{
	public UpdateService() {
		super("Update Service");
		// TODO Auto-generated constructor stub
	}

	private NotificationManager nManager;
	private HttpURLConnection connection;
	
	private final int notifyId = 1;
	//通知栏自定义view
	private RemoteViews remoteViews;
	//通知栏
	private Notification updateNotification;

	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	private void showNotification()
	{
		remoteViews = new RemoteViews(getPackageName(),
				R.layout.update_service_download);
		remoteViews.setTextViewText(R.id.text, "下载中");
		//构造notification
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getBaseContext())
					.setContent(remoteViews)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("下载中");
		updateNotification = mBuilder.build();
		nManager.notify(notifyId, updateNotification);
		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if(intent.getStringExtra("apk_url") != null)
		{	
			int max_progress = 0;
			//notify太频繁导致通知栏卡顿，每过5%，发出一次更新通知
			float notify_pre = 0.05f;
			showNotification();
			URL url;
			try
			{
				url = new URL(intent.getStringExtra("apk_url"));
				connection = (HttpURLConnection) url.openConnection();
				connection.setDoInput(true);
				//模拟浏览器，否则github下载很慢
				connection.setRequestProperty("User-Agent", 
						"Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
				connection.connect();
				InputStream stream = connection.getInputStream();
				max_progress = connection.getContentLength();
				Log.d("Comic", "max_progress:" + max_progress);
				remoteViews.setProgressBar(R.id.progress,max_progress, 0, false);
				FileOutputStream outputStream = null;
				if(stream != null)
				{
					File file = new File(Environment.getExternalStorageDirectory(),
							"FindComic.apk");
					outputStream = new FileOutputStream(file);
					byte[] buf = new byte[10240];
					int index = -1;
					int progress = 0;
					while((index = stream.read(buf)) != -1)
					{
						outputStream.write(buf, 0, index);
						progress += index;
//						Log.d("Comic", "" + progress);						
						if(progress / (float) max_progress > notify_pre)
						{
							notify_pre += 0.05;
							remoteViews.setProgressBar(R.id.progress,max_progress, progress,false);
							updateNotification.contentView = remoteViews;
							nManager.notify(notifyId, updateNotification);
						}

					}
				}
				if(outputStream != null)
				{
					outputStream.flush();
					outputStream.close();
					update();					
					nManager.cancel(notifyId);
				}
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
		}
		
	}
	
	private void update()
	{
		
		File file = new File(Environment.getExternalStorageDirectory(),"FindComic.apk");
		if(file.exists())
		{
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.fromFile(file), 
					"application/vnd.android.package-archive");
			startActivity(intent);
		}
	}

}
