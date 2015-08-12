package com.zhr.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.Header;

import android.content.Context;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.tencent.bugly.crashreport.common.strategy.c;
import com.zhr.database.DBComicDownloadDetailHelper;
import com.zhr.setting.AppSetting;
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
	
	
	private int status;
	private String[] urls = null;
	
	private ComicDownloadDetail cDetail;
	private Context context;
	
	private String dirPath;
	
	private boolean isRunning = false;
	
	public ComicDownloadThread(ComicDownloadDetail cDetail,Context context)
	{
		this.cDetail = cDetail;	
		this.context = context;
		createDirs();
	}	
	
	
	public void run() 
	{
		getImageUrl();
		try 
		{
			if(urls != null)
			{
				isRunning = true;
				cDetail.setStatus("1");
				for(int i = 0;i < urls.length;i++)
				{
					while(isRunning&&context != null)
					{

							URL url = new URL(urls[i]);
							HttpURLConnection connection = (HttpURLConnection) url.openConnection();
							connection.setDoInput(true);
							connection.setRequestProperty("User-Agent","Baiduspider+");
							connection.connect();
							InputStream inputStream = connection.getInputStream();
							FileOutputStream outputStream = null;
							byte[] bytes = new byte[1024];
							int nums = 0;
							if(inputStream != null)
							{
								
								outputStream = new FileOutputStream(new File(dirPath,"" + i + ".jpg"));
								while((nums = inputStream.read(bytes)) != -1)
								{
									outputStream.write(bytes,0,nums);
								}
							}
							if(outputStream != null)
							{
								outputStream.flush();
								outputStream.close();
								
							}
					}
				}
				cDetail.setStatus("3");
				if(context != null)
					DBComicDownloadDetailHelper.getInstance(context).saveComicDownloadDetail(cDetail);
			}
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			context = null;
		}
	}
	
	private void createDirs()
	{
		File savefile = new File(AppSetting.getInstance(context).getDownloadPath());
		if(!savefile.exists())
		{
			savefile.mkdirs();
		}
		dirPath = savefile.getAbsolutePath() + File.separator + cDetail.getChapter();
		savefile = new File(dirPath);
		if(!savefile.exists())
		{
			savefile.mkdir();
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
