package com.zhr.util;

import java.io.File;

import android.os.Environment;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月28日
 * @description
 */
public class Constants {
	public static String DMZJ = "dmzj";
	public static String DM123 = "dm123";
	public static String MSITE = "Msite";
	
	public static String FILENAME = Environment.getExternalStorageDirectory() + File.separator + "findComic";
	public static String DISKCACHE_FILENAME = FILENAME + File.separator + "diskCache";
}
