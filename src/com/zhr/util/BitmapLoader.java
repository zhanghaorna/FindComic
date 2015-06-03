package com.zhr.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zhr.setting.AppSetting;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月11日
 * @description
 */
public class BitmapLoader 
{
	private volatile static BitmapLoader instance;
	public static final String LOG_TAG = "BitmapLoader";
	private ExecutorService threadPool;
	private Handler handler;
	
	public static BitmapLoader getInstance()
	{
		if(instance == null)
		{
			synchronized (BitmapLoader.class) {
				if(instance == null)
					instance = new BitmapLoader();
			}
		}
		return instance;
	}
	
	private BitmapLoader()
	{
		if(Thread.currentThread() != Looper.getMainLooper().getThread())
		{
			Log.d(LOG_TAG, "不能在子线程中实例化BitmapAsynLoader");
			return;
		}
		threadPool = getDefaultThreadPool();
		handler = new Handler();
	}
	
	
	
	public void loadImageNoCache(ImageView imageView,String path,boolean thumbnail)
	{
		loadImage(imageView, path,false,thumbnail,false);
	}
	
	public void loadImage(ImageView imageView,String path)
	{
		loadImage(imageView, path, true);
	}
	
	public void loadImage(ImageView imageView,String path,
							boolean asyn)
	{
		loadImage(imageView, path,asyn,false,true);
	}
	
	public void loadImage(ImageView imageView,String path,
							boolean asyn,boolean thumbnail,boolean cacheToDisk)
	{
		LoadAndDisplayTask task = null;
		//获取内存是否有缓存,应该是全局的缓存
		if(AppSetting.getInstance() != null)
		{
			LruCache<String, Bitmap> cache = AppSetting.getInstance().getCache();
			Bitmap bitmap = null;
			if(cache != null)
				bitmap = cache.get(path);
			if(bitmap != null&&!bitmap.isRecycled())
			{
				if(bitmap.isRecycled())
					Log.d("Comic", "isRecycle");
				task = new LoadAndDisplayTask(imageView, bitmap, handler);
			}

			
		}
		if(task == null)
			task = new LoadAndDisplayTask(imageView, path, thumbnail, handler,true,cacheToDisk);
		if(!asyn)
		{
			task.run();
		}
		else
		{
			threadPool.submit(task);
		}
	}
	
	
	
	protected ExecutorService getDefaultThreadPool()
	{
		return Executors.newCachedThreadPool();
	}
	
}
