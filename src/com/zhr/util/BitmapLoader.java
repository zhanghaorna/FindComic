package com.zhr.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.zhr.setting.AppSetting;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore.Video;
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
	//缓存正在加载ImageView，有ImageView正在加载,解决AbsView中getView循环利用View时，ImageView显示
	//图片频繁跳动问题。显示图片时，检查HashMap中View对应的URL地址是否是自己的，如不是，则不加载图片。
	private final Map<Integer, String> cacheKeyForImage = Collections.synchronizedMap(
			new HashMap<Integer,String>());
	//漫画页下载页面的缓存
	private final Set<String> comicPageCache = Collections.synchronizedSet(
			new HashSet<String>());
	
	public void addImageTask(String path)
	{
		comicPageCache.add(path);
	}
	
	public boolean hashImageTask(String path)
	{
		return comicPageCache.contains(path);
	}
	
	public void removeImageTask(String path)
	{
		comicPageCache.remove(path);
	}
	
	public void prepareDisplayTask(ImageView imageView,String path)
	{
		cacheKeyForImage.put(imageView.hashCode(), path);
	}
	
	public String getLoadingUriFromView(ImageView imageView)
	{
		return cacheKeyForImage.get(imageView.hashCode());
	}
	
	public void cancelDisplayTask(ImageView imageView)
	{
		cacheKeyForImage.remove(imageView.hashCode());
	}
	
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
		loadImage(imageView, path,false,thumbnail,false,false);
	}
	
	public void loadImage(ImageView imageView,String path)
	{
		loadImage(imageView, path, true);
	}
	
	public void loadImage(ImageView imageView,String path,
							boolean asyn)
	{
		loadImage(imageView, path,asyn,false,true,false);
	}
	//加载漫画页，如imageView为null，则提前加载图片
	public void loadComicImage(ImageView imageView,String path)
	{
		if(path == null||path.equals(""))
			return;
		ComicLoadTask task = null;
		LruCache<String, Bitmap> cache = null;
		if(AppSetting.getInstance() != null)
		 cache = AppSetting.getInstance().getComicCache();
		//提前加载图片，只是新开线程，将任务保存
		if(imageView == null&&AppSetting.getInstance() != null)
		{
			if(cache == null||cache.get(path) == null||cache.get(path).isRecycled())
			{
				if(!hashImageTask(path))
				{
					 task = new ComicLoadTask(path);
					 comicPageCache.add(path);
				}
			}
		}
		else //加载图片，有缓存就直接加载，没有就想网页url请求加载
		{	
			if(getLoadingUriFromView(imageView) != null&&
					getLoadingUriFromView(imageView) == path)
			{				
				return;
			}
			if(cache == null||cache.get(path) == null||cache.get(path).isRecycled())
			{
//				Log.d("Comic", "download" + path);
				prepareDisplayTask(imageView, path);
				task = new ComicLoadTask(imageView,path,handler,null);
				comicPageCache.add(path);
			}
			else
			{
//				String cur_path = (String) imageView.getTag();
//				Log.d("Comic", "tag" + cur_path);
//				if(cur_path == null||!cur_path.equals(path))
//				{
					prepareDisplayTask(imageView, path);
					task = new ComicLoadTask(imageView,path,handler,cache.get(path));
//					Log.d("Comic", "cache page");
					
//				}
			}
		}
		if(task != null)
		{
			threadPool.submit(task);			
		}
			
	}
	
	public void loadImage(ImageView imageView,String path,
							boolean asyn,boolean thumbnail,boolean cacheToDisk
							,boolean isComicPage)
	{
		if(path == null||path.equals(""))
			return;
		LoadAndDisplayTask task = null;
		//获取内存是否有缓存,应该是全局的缓存
		if(AppSetting.getInstance() != null)
		{
			LruCache<String, Bitmap> cache = null;
			if(isComicPage)
			{
				 cache = AppSetting.getInstance().getComicCache();
			}
			else
			{
				cache = AppSetting.getInstance().getCache();
			}
			Bitmap bitmap = null;
			if(cache != null)
				bitmap = cache.get(path);
			if(bitmap != null&&!bitmap.isRecycled())
			{
				Log.d("Comic", "cache task");
				prepareDisplayTask(imageView, path);
				task = new LoadAndDisplayTask(imageView,path, bitmap, handler,isComicPage);
			}			
		}
		
		if(task == null)
		{
			if(getLoadingUriFromView(imageView) != null&&
					getLoadingUriFromView(imageView) == path)
			{				
				return;
			}
			else {
				prepareDisplayTask(imageView, path);
			}
			task = new LoadAndDisplayTask(imageView, path, thumbnail, handler,thumbnail != true,cacheToDisk
					,isComicPage);
		}
			
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
