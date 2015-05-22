package com.zhr.util;

import com.zhr.comic.ComicReadActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.text.Layout;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月11日
 * @description
 */
public class LoadAndDisplayTask implements Runnable{
	private ImageView targetView;
	private String imagePath;
	private boolean thumbnail;
	private boolean isCache;
	private Handler handler;
	
	private Bitmap bitmap = null;
	
	public LoadAndDisplayTask(ImageView targetView,String imagePath
			,boolean thumbnail,Handler handler,boolean isCache)
	{
		this.targetView = targetView;
		this.imagePath = imagePath;
		this.thumbnail = thumbnail;
		this.isCache = isCache;
		
		this.handler = handler;
	}
	
	
	
	public void run() {
		try 
		{
			//获取内存是否有缓存,应该是全局的缓存
//			if(cache != null)
//				bitmap = cache.get(imagePath);
			if(bitmap == null||bitmap.isRecycled())
			{
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				if(thumbnail)
				{
					bitmap = BitmapFactory.decodeFile(imagePath,options);
					options.inSampleSize = calculateInSampleSize(options, 40, 40);
					options.inJustDecodeBounds = false;
					bitmap = BitmapFactory.decodeFile(imagePath,options);
//					if(isCache && cache != null)
//						cache.put(imagePath, bitmap);
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
//					bitmap = Bitmap.createBitmap(bitmap, 0, 0, targetView.getMeasuredWidth()
//							,targetView.getMeasuredHeight()); 
//					if(isCache && cache != null)
//						cache.put(imagePath, bitmap);
				}
			}
			if(bitmap != null&&targetView != null)
			{
				if(Thread.currentThread() != Looper.getMainLooper().getThread())
					handler.post(new Runnable() {						
						public void run() {
//							FrameLayout.LayoutParams params = (LayoutParams) targetView.getLayoutParams();
//							params.height = (int) (params.width * bitmap.getHeight() / (float)bitmap.getWidth());
//							targetView.setLayoutParams(params);	
							targetView.setImageBitmap(bitmap);
						}
					});
				else 
				{
					targetView.setImageBitmap(bitmap);
				}
			}
		} catch (OutOfMemoryError e) {
			bitmap = null;
			Log.d("BitMapError", "Bitmap is too big to load");
		}

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
}