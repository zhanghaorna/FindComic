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
	
	public static String[] URL_PERFIX = new String("http://mhh.3348.net:9393/dm01/|http://mhh.3348.net:9393/dm02/|http://mhh.3348.net:9393/dm03/|"
			+ "http://mhh.3348.net:9393/dm04/|http://mhh.3348.net:9393/dm05/|http://mhh.3348.net:9393/dm06/|http://mhh.3348.net:9393/dm07/|"
			+ "http://mhh.3348.net:9393/dm08/|http://mhh.3348.net:9393/dm09/|http://mhh.3348.net:9393/dm10/|http://mhh.3348.net:9393/dm11/|"
			+ "http://mhh.3348.net:9393/dm12/|http://mhh.3348.net:9393/dm13/|http://mhh.3348.net:9393/dm14/|http://mhh.3348.net:9393/dm15/|"
			+ "http://mhh.3348.net:9393/dm16/").split("\\|");
	
	public static String BUGLY_ID = "900004158";
	
	public static String FILENAME = Environment.getExternalStorageDirectory() + File.separator + "findComic";
	public static String DISKCACHE_FILENAME = FILENAME + File.separator + "diskCache";
	
	public static final int IMAGE_LOADED = 101;
	
	public static final int WAITING = 0;
	public static final int DOWNLOADING = 1;
	public static final int PAUSED = 2;
	public static final int FINISHED = 3;
	public static final int PAUSEING = 4;
	
}
