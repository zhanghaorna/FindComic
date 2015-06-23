package com.zhr.util;

import org.apache.http.Header;

import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.zhr.setting.AppSetting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月22日
 * @description
 */
public class ComicLoadTask implements Runnable{
	private ImageView targetView;
	private String imagePath = "";
	private Handler handler;
	private Bitmap bitmap = null;
	
	public ComicLoadTask(ImageView imageView,String url,Handler handler,Bitmap bitmap)
	{
		targetView = imageView;
		imagePath = url;
		this.handler = handler;
		this.bitmap = bitmap;
	}
	
	public ComicLoadTask(String url)
	{
		imagePath = url;
		targetView = null;
		handler = null;
	}
	
	@Override
	public void run() {
		try 
		{
			if(bitmap == null||bitmap.isRecycled())
			{
				if(AppSetting.getInstance() != null)
				{
					bitmap = AppSetting.getInstance().getComicCache().get(imagePath);
				}				
				if((bitmap == null||bitmap.isRecycled())&&
						imagePath.startsWith("http://"))
				{	
					//采用同步方法，因为这里已经在一个线程中，不能再异步调用
					//Log一直报警告，说强制采用同步方法
					SyncHttpClient client = new SyncHttpClient();
					client.setEnableRedirects(true);
					client.setUserAgent("Baiduspider+");
					client.get(imagePath, new BinaryHttpResponseHandler() {						
						@Override
						public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
							
							bitmap = BitmapFactory.decodeByteArray(arg2, 0, arg2.length);
							if(handler != null)
								loadImage();								
							BitmapLoader.getInstance().removeImageTask(imagePath);
							
							if(AppSetting.getInstance() != null&&bitmap != null)
							{								
								if(targetView != null)
								{
									Log.d("Comic", "add cache " + imagePath);
									AppSetting.getInstance().getComicCache().put(imagePath, bitmap);	
								}
								else {
									if(AppSetting.getInstance().getComicCache().get(imagePath) == null
											||AppSetting.getInstance().getComicCache().get(imagePath).isRecycled())
									{
										AppSetting.getInstance().getComicCache().put(imagePath, bitmap);	
									}
								}	
							}
							
						}
						
						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
							BitmapLoader.getInstance().removeImageTask(imagePath);
						}
					});
				}			
			}
			else
				loadImage();
		} catch (Exception e) {
			
		}		
	}
	
	private void loadImage()
	{
		if(bitmap != null&&!bitmap.isRecycled()&&targetView != null)
		{
			handler.post(new Runnable() {
				public void run() {
					if(bitmap != null&&!bitmap.isRecycled())
					{
						targetView.setTag(imagePath);
						targetView.setImageBitmap(bitmap);
					}					
				}
			});
		}
	}

}
