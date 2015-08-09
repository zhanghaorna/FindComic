package com.zhr.util;


import java.io.File;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
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
		if(networkInfo == null)
			return false;
		else
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
	
	public static void createFile()
	{
		if(Environment.isExternalStorageEmulated())
		{
			File file = new File(Constants.FILENAME);
			if(!file.exists())
			{
				file.mkdirs();
			}
			if(file.isDirectory())
			{
				File cache = new File(Constants.DISKCACHE_FILENAME);
				if(!cache.exists())
					cache.mkdirs();
			}
		}
	}
	
	public static String randomStr()
	{
		Random random = new Random();
		int value = random.nextInt(2000);
		return String.valueOf(value);
	}
	
	//从指定url中获取图片的urls
	public static String[] getImageUrlsFromInternet(byte[] bytes)
	{
		//获取漫画网的前缀
		int url_prefix_position = 0;
		Document document = Jsoup.parse(new String(bytes));					
		String text = document.data();
		if(!text.equals("")&&text.indexOf(";") != -1)
		{						
			String position = text.split(";")[1];
			text = text.split(";")[0];
			position = position.replace("var", "").replace("sPath=", "").replace("\"", "")
				.replace(" ", "");
			url_prefix_position = Integer.valueOf(position).intValue();
		}
		text = text.replace("var", "").replace("sFiles=", "").replace("\"", "")
				.replace(" ", "");
		
		//从一串代码中解析出图片地址，由于该网站采取了加密
		String x = text.substring(text.length() - 1);
		int xi = "abcdefghijklmnopqrstuvwxyz".indexOf(x) + 1;
		String sk = text.substring(text.length() - xi - 12,text.length() - xi - 1);
		text = text.substring(0,text.length() - xi - 12);
		String k = sk.substring(0,sk.length() - 1);
		String f = sk.substring(sk.length() - 1);
		for(int i = 0;i < k.length();i++)
		{
			text = text.replaceAll(k.substring(i,i+1), i + "");
		}
		String[] ss = text.split(f);
		StringBuilder builder = new StringBuilder();
		for(int i = 0;i < ss.length;i++)
		{
			builder.append((char)Integer.valueOf(ss[i]).intValue());
		}
		String[] path = builder.toString().split("\\|");
		for(int i = 0;i < path.length;i++)
		{
			path[i] = Constants.URL_PERFIX[url_prefix_position - 1] + path[i];
		}
		return path;
	}

}
