package com.zhr.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.http.Header;


import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.zhr.database.DBNewsHelper;
import com.zhr.setting.AppSetting;


import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月11日
 * @description
 */
public class LoadAndDisplayTask implements Runnable{
	private ImageView targetView;
	private String imagePath = "";
	private boolean thumbnail;
	private boolean cacheToMemory;
	private boolean isComicPage;
	private Handler handler;
	
	private Bitmap bitmap = null;
	
	public LoadAndDisplayTask(ImageView targetView,String imagePath
			,boolean thumbnail,Handler handler,boolean cacheToMemory
			,boolean isComicPage)
	{
		this.targetView = targetView;
		this.imagePath = imagePath;
		this.thumbnail = thumbnail;
		this.cacheToMemory = cacheToMemory;		
		this.handler = handler;
		this.isComicPage = isComicPage;
	}
	
	public LoadAndDisplayTask(ImageView targetView,String imagePath
			,Bitmap bitmap,Handler handler,boolean isComicPage)
	{
		this.targetView = targetView;
		this.imagePath = imagePath;
		this.bitmap = bitmap;
		this.handler = handler;
		this.isComicPage = isComicPage;
		this.cacheToMemory = true;
	}
	
	
	public void run() {
		try 
		{
			if(bitmap == null||bitmap.isRecycled())
			{	//从网络获取图片
				if(imagePath.startsWith("http://")&&targetView != null)
				{	
					//采用同步方法，因为这里已经在一个线程中，不能再异步调用
					//Log一直报警告，说强制采用同步方法
					SyncHttpClient client = new SyncHttpClient();
					client.setEnableRedirects(true);
					client.setTimeout(2000);
					client.get(imagePath, new BinaryHttpResponseHandler() {
						
						@Override
						public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
							if(isViewReused())
								return;
							BitmapFactory.Options options = new BitmapFactory.Options();
							options.inJustDecodeBounds = true;
							bitmap = BitmapFactory.decodeByteArray(arg2, 0, arg2.length);
							options.inSampleSize = calculateInSampleSize(options, 150,
									100);
							options.inJustDecodeBounds = false;
							bitmap = BitmapFactory.decodeByteArray(arg2, 0, arg2.length);
							if(isViewReused())
								return;
							loadImage();							
						}
						
						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
							Log.d("Comic","Path:" + imagePath);
							
						}
					});
				}
				//从本地获取图片
				else if(imagePath != "")
				{
					if(isViewReused())
						return;
					File file = new File(imagePath);
					if(!file.exists())
						return;
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					if(thumbnail)
					{
						bitmap = BitmapFactory.decodeFile(imagePath,options);
						options.inSampleSize = calculateInSampleSize(options, 40, 40);
						options.inJustDecodeBounds = false;
						bitmap = BitmapFactory.decodeFile(imagePath,options);
					}
					else
					{
						bitmap = BitmapFactory.decodeFile(imagePath,options);
						//Bitmap宽高都大于2000进行压缩
						if(options.outHeight > 2000&&options.outWidth > 2000)
							options.inSampleSize = calculateInSampleSize(options, 720, 
									1280);
						options.inJustDecodeBounds = false;
						bitmap = BitmapFactory.decodeFile(imagePath,options);
//						bitmap = Bitmap.createBitmap(bitmap, 0, 0, targetView.getMeasuredWidth()
//								,targetView.getMeasuredHeight()); 
					}
					if(isViewReused())
						return;
				
					loadImage();
				}
			}
			//有缓存就直接加载
			else
			{
				if(isViewReused())
					return;
				loadImage();
			}
			
		} catch (OutOfMemoryError e) {
			bitmap = null;
			Log.d("BitMapError", "Bitmap is too big to load");
			//直接重新加载一次
			run();
		}
		finally
		{
//			targetView = null;
//			bitmap = null;
//			handler = null;
		}
	}
	
	private void loadImage()
	{
		if(!isComicPage&&cacheToMemory&&AppSetting.getInstance() != null)
			AppSetting.getInstance().getCache().put(imagePath, bitmap);
		else if(isComicPage&&AppSetting.getInstance() != null)
		{
			Log.d("Comic", "cache comic page");
//			AppSetting.getInstance().getComicCache().put(imagePath, bitmap);
		}

		if(isViewReused())
			return;
		if(bitmap != null&&!bitmap.isRecycled()&&targetView != null)
		{
			if(Thread.currentThread() != Looper.getMainLooper().getThread())
				handler.post(new Runnable() {						
					public void run() {
//						FrameLayout.LayoutParams params = (LayoutParams) targetView.getLayoutParams();
//						params.height = (int) (params.width * bitmap.getHeight() / (float)bitmap.getWidth());
//						targetView.setLayoutParams(params);
						if(bitmap != null&&!bitmap.isRecycled()&&!isViewReused())
						{
							targetView.setImageBitmap(bitmap);
						}
					}
				});
			else 
			{
				targetView.setImageBitmap(bitmap);
			}
		}
		//没有必要缓存图片到本地，导致问题
//		if(cacheToDisk&&imagePath.startsWith("http://"))
//		{
//			int sep = imagePath.lastIndexOf("/");
//			int point = imagePath.lastIndexOf(".");
//			if(sep != -1&&point != -1&&sep < point&&sep < imagePath.length() - 1)
//			{
//				String imageName = imagePath.substring(sep + 1,point);
//				imageName = Constants.DISKCACHE_FILENAME + File.separator + imageName;
//				File imageFile = new File(imageName);
//				if(imageFile.exists())
//					return;
//				try
//				{
//					FileOutputStream stream = new FileOutputStream(imageName);
//					
//					if(DBNewsHelper.getDbNewsHelper() != null)
//					{		
//						bitmap.compress(CompressFormat.JPEG, 30, stream);
//						DBNewsHelper.getDbNewsHelper().alertNews(imagePath,imageName);
//					}
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//				}				
//			}
//		}
	}
	
	
	private int calculateInSampleSize(BitmapFactory.Options options,  
	        int reqWidth, int reqHeight)
	{
	    // Raw height and width of image  
	    final int height = options.outHeight;  
	    final int width = options.outWidth;  
	    int inSampleSize = 1;
	    
	    //先根据宽度进行缩小  
	    while (width / inSampleSize > reqWidth) {  
	        inSampleSize++;  
	    }  
	    //然后根据高度进行缩小  
	    while (height / inSampleSize > reqHeight) {  
	        inSampleSize++;  
	    }  
	    
	    return inSampleSize;  
	}

	//检测ImageView是否被重用，如被重用则取消加载
	private boolean isViewReused()
	{
		String currentPath = BitmapLoader.getInstance().getLoadingUriFromView(targetView);
		//imagePath为""表示缓存任务,也返回false.
		if(currentPath == null||currentPath.equals(imagePath)||imagePath.equals(""))
		{
			return false;
		}
		else 
		{
			targetView = null;
			bitmap = null;
			handler = null;
			Log.d("Comic", "cache cache");
			return true;
		}

	}
	
}