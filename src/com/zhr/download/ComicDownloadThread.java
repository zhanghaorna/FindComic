package com.zhr.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.Header;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.tencent.bugly.crashreport.common.strategy.c;
import com.zhr.database.DBComicDownloadDetailHelper;
import com.zhr.database.DBComicDownloadHelper;
import com.zhr.setting.AppSetting;
import com.zhr.sqlitedao.ComicDownload;
import com.zhr.sqlitedao.ComicDownloadDetail;
import com.zhr.util.Constants;
import com.zhr.util.Util;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年7月27日
 * @description
 */
public class ComicDownloadThread implements Runnable{
	
	private String[] urls = null;
	
	private ComicDownloadDetail cDetail;
	private Context context;
	
	private String dirPath;
	
	private boolean isRunning = true;

	
	public ComicDownloadThread(ComicDownloadDetail cDetail,Context context)
	{
		this.cDetail = cDetail;	
		this.context = context;
		createDirs();
	}	
	
	public ComicDownloadDetail getDownloadDetail()
	{
		return cDetail;
	}
	
	
	public void run() 
	{
		if(!isRunning)
		{
			cDetail.setStatus(Constants.PAUSED);
			DBComicDownloadDetailHelper.getInstance(context).saveComicDownloadDetail(cDetail);
			Intent intent = new Intent();
			intent.setAction(DownloadService.CHAPTER_FINISHING_OR_PAUSED);
			intent.putExtra("comicName", cDetail.getComicName());
			intent.putExtra("chapterName", cDetail.getChapter());
			intent.putExtra("status", cDetail.getStatus());
			context.sendOrderedBroadcast(intent, null);
//			LocalBroadcastManager nManager = LocalBroadcastManager.getInstance(context);
//			nManager.sendBroadcast(intent);
			return;
		}
		getImageUrl();
		try 
		{
			if(urls != null&&isRunning)
			{
				cDetail.setStatus(Constants.DOWNLOADING);
				setDownloadStatus(cDetail.getComicName(), Constants.DOWNLOADING);
				//当状态变为下载后，发送广播，通知UI更新
				Intent downloadIntent = new Intent();
				downloadIntent.setAction(DownloadService.DOWNLOAD_STATE_CHANGE);
				context.sendOrderedBroadcast(downloadIntent, null);
				
				int i = cDetail.getFinishNum();
				for(;i < urls.length;i++)
				{
					if(isRunning)
					{
						URL url = new URL(urls[i]);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setDoInput(true);
						connection.setConnectTimeout(5000);
						connection.setReadTimeout(5000);
						connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:7.0a1) Gecko/20110623 Firefox/7.0a1 Fennec/7.0a1");
						connection.setRequestMethod("GET");
						connection.connect();
						InputStream inputStream = connection.getInputStream();
						FileOutputStream outputStream = null;
						byte[] bytes = new byte[10240];
						int nums = 0;
						String fileName = "";
						if(inputStream != null)
						{								
							if(i < 10)
								fileName = "00" + i + ".jpg";
							else if(i < 100)
								fileName = "0" + i + ".jpg";
							else
								fileName = i + ".jpg";
							outputStream = new FileOutputStream(new File(dirPath,fileName));
							while((nums = inputStream.read(bytes)) != -1)
							{
								if(!isRunning)
									break;
								outputStream.write(bytes,0,nums);
							}
							
						}
						if(outputStream != null)
						{
							if(!isRunning)
							{
								outputStream.close();
								File file = new File(dirPath,fileName);
								if(file.exists())
									file.delete();
							}
							else {
								outputStream.flush();
								outputStream.close();
								cDetail.setFinishNum(i + 1);
							}								
						}
					}
				}
				if(!isRunning&&i < urls.length)
					cDetail.setStatus(Constants.PAUSED);
				else
					cDetail.setStatus(Constants.FINISHED);
				if(checkDownloadStatus(cDetail.getComicName()))
				{
					setDownloadStatus(cDetail.getComicName(), Constants.FINISHED);
				}
				if(context != null)
				{
					DBComicDownloadDetailHelper.getInstance(context).saveComicDownloadDetail(cDetail);
					Intent intent = new Intent();
					intent.setAction(DownloadService.CHAPTER_FINISHING_OR_PAUSED);
					intent.putExtra("comicName", cDetail.getComicName());
					intent.putExtra("chapterName", cDetail.getChapter());
					intent.putExtra("status", cDetail.getStatus());
					context.sendOrderedBroadcast(intent, null);
//					LocalBroadcastManager nManager = LocalBroadcastManager.getInstance(context);
//					nManager.sendBroadcast(intent);
				}
			}
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			if(cDetail.getStatus() == Constants.DOWNLOADING||cDetail.getStatus()
					== Constants.WAITING)
			{
				cDetail.setStatus(Constants.PAUSED);
				DBComicDownloadDetailHelper.getInstance(context).saveComicDownloadDetail(cDetail);
				Intent intent = new Intent();
				intent.setAction(DownloadService.NETWORK_ERROR);
				intent.putExtra("comicName", cDetail.getComicName());
				intent.putExtra("chapterName", cDetail.getChapter());
				context.sendOrderedBroadcast(intent, null);
//				LocalBroadcastManager nManager = LocalBroadcastManager.getInstance(context);
//				nManager.sendBroadcast(intent);
			}
			context = null;
		}
	}
	
	private boolean checkDownloadStatus(String comicName)
	{
		List<ComicDownloadDetail> cDetails = DBComicDownloadDetailHelper.getInstance(context).getComicDownloadDetails(comicName);
		if(cDetails != null&&cDetails.size() != 0)
		{
			for(int i = 0;i < cDetails.size();i++)
			{
				if(cDetails.get(i).getStatus() != Constants.FINISHED)
					return false;
			}
			return true;
		}
		return false;
	}
	
	private void setDownloadStatus(String comicName,int status)
	{
		ComicDownload cDownload = DBComicDownloadHelper.getInstance(context).getComicDownload(comicName);
		if(cDownload != null&&cDownload.getStatus() != status)
		{
			cDownload.setStatus(status);
			DBComicDownloadHelper.getInstance(context).saveComicDownload(cDownload);
		}

	}
	
	//清除一些变量，释放内存空间
	public void clear()
	{
		cDetail = null;
		context = null;
	}
	
	public void pauseDownload()
	{
		isRunning = false;
	}
	
	public int getDownloadStatus()
	{
		return cDetail.getStatus();
	}
	
	private void createDirs()
	{
		//findComic目录
		File saveFile = new File(AppSetting.getInstance(context).getDownloadPath());
		if(!saveFile.exists())
		{
			//可以创建多级目录，不管是否存在目录，如在a中创建 b//c(文件夹),可以创建c
			saveFile.mkdirs();
		}
		//漫画名目录
		saveFile = new File(saveFile, cDetail.getComicName());
		if(!saveFile.exists())
		{
			//只能创建一级目录,如在a中创建 b//c，则无法创建c
			saveFile.mkdir();
		}
		
		//第几话目录
		dirPath = saveFile.getAbsolutePath() + File.separator + cDetail.getChapter();
		saveFile = new File(dirPath);
		if(!saveFile.exists())
		{
			saveFile.mkdir();
		}
	}
	
	private void getImageUrl()
	{
		SyncHttpClient client = new SyncHttpClient();
		client.setUserAgent("Baiduspider+");
		client.get(cDetail.getUrl(), new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				if(arg0 == 200)
				{
					urls = Util.getImageUrlsFromInternet(arg2);
					cDetail.setPageNum(urls.length);
				}					
			}
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				if(Util.isNetWorkConnect(context))
					getImageUrl();
					
			}
		});
	}
}
