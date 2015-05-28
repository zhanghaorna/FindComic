package com.zhr.util;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	
	
	public static int getImageHeight(Context context)
	{
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		Point point = new Point();
		display.getSize(point);
		if(point.x > point.y)
		{
			return (int) (point.x * point.x / (float)point.y);
		}
		else
		{
			return point.y;
		}
	}
	
	public static int px2dip(Context context, float pxValue) {
	    final float scale = context.getResources().getDisplayMetrics().density;
	    return (int) (pxValue / scale + 0.5f);
	}
	
	public static int dip2px(Context context, float dpValue) {
	    final float scale = context.getResources().getDisplayMetrics().density;
	    return (int) (dpValue * scale + 0.5f);
	}
	
	public static String getNetWorkStatus(Context context)
	{
		ConnectivityManager cManager = (ConnectivityManager)context.getSystemService(
				Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cManager.getActiveNetworkInfo();
		return networkInfo.getTypeName();
	}
	
	public static boolean isNetWorkConnect(Context context)
	{
		ConnectivityManager cManager = (ConnectivityManager)context.getSystemService(
				Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cManager.getActiveNetworkInfo();
		return networkInfo.isConnected();
	}
	
	public static Date stringToDate(String date) throws ParseException
	{
		return stringToDate(date, "yyyy-MM-dd HH:mm");
	}
	
	public static Date stringToDate(String date,String format) throws ParseException
	{
		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.parse(date);		
	}
	
	public static String dateToString(Date date)
	{
		return dateToString(date, "yyyy-MM-dd HH:mm");
	}
	
	public static String dateToString(Date date,String format)
	{
		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}
	

}
