package com.zhr.util;

import com.zhr.findcomic.R.color;

import android.R.string;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月9日
 * @description
 */
public class Util {
	
	public static int getScreenHeight(Context context)
	{
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		Point point = new Point();
		display.getSize(point);
		return point.y;		
	}
	
	public static int getScreenWidth(Context context)
	{
		
		
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		Point point = new Point();
		display.getSize(point);
		return point.x;	
	}
	
	
	public static int px2dip(Context context, float pxValue) {
	    final float scale = context.getResources().getDisplayMetrics().density;
	    return (int) (pxValue / scale + 0.5f);
	}
	
	public static int dip2px(Context context, float dpValue) {
	    final float scale = context.getResources().getDisplayMetrics().density;
	    return (int) (dpValue * scale + 0.5f);
	}
}
