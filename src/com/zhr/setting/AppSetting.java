package com.zhr.setting;

import com.zhr.findcomic.R;

import android.R.integer;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月7日
 * @description
 */
public class AppSetting {

	public static final String PREFS_NAME = "pref_app_setting";
	
	public static final String[] screen_orientations = new String[]{"横屏","竖屏"};
	public static final int[] screen_orientations_src = new int[]{R.drawable.land_mode,R.drawable.port_mode};
	public static final String[] page_turn_orientations = new String[]{"横向翻页","纵向翻页"};
	public static final int[] page_turn_orientation_src = new int[]{R.drawable.reader_port_mode_h,R.drawable.reader_port_mode_v};
	public static final String[] page_turn_hands = new String[]{"左手翻页","右手翻页"};
	public static final int[] page_turn_hand_src = new int[]{R.drawable.reader_left_hand_mode,R.drawable.reader_right_hand_mode};
	
	
	private final String SCREEN_ORIENTATION = "screen_orientation";
	private final String PAGE_TURN_ORIENTATION = "page_turn_orientation";
	private final String PAGE_TURN_HAND = "page_turn_mode";
	private final String PAGEOVER_BY_VOLUME = "pageOver_by_volume";
	private final String HIDE_VIRTUAL_KEY = "hide_virtual_key";
	private final String SHOW_TIME_BATTERY = "show_time_battery";
	private final String KEEP_SCREEN_ON = "keep_screen_on";
	private final String NIGHT_MODE = "night_mode";
	private final String LAST_READ_LOCAL = "last_read_local";
	
	private static AppSetting instance = null;
	

	//屏幕方向
	//横屏or竖屏
	private int screen_orientation;
	//竖屏阅读模式 从左往右or从上往下
	private int page_turn_orientation;
	//竖屏左手翻页or右手翻页
	private int page_turn_hand;
	//音量键翻页
	private boolean pageOver_by_volume;
	//隐藏虚拟按键
	private boolean hide_virtual_key;
	
	private boolean show_time_battery;
	private boolean keep_screen_on;
	
	private SharedPreferences setting;
	//ͨ通知设置
	
	//夜间模式
	private boolean night_mode;
	//本地阅读上次位置
	private String last_read_local;
	
	//下载文件路径
	private String downloadFile;
	//全局的缓存，主要用来存放Bitmap
	private LruCache<String, Bitmap> cache;
	
	
	public synchronized static AppSetting getInstance(Context context)
	{
		if(instance == null)
		{
			instance = new AppSetting(context);
		}
		return instance;
	}
	
	public static AppSetting getInstance()
	{
		if(instance == null)
			throw new IllegalStateException("You must call getInstance(Context context) before");
		return instance;
	}
	
	private AppSetting()
	{

	}
	
	public LruCache<String, Bitmap> getCache()
	{
		return this.cache;
	}
	
	//此处不用commit,由于commit是同步，apply是异步
	public void setScreenOrientation(int orientation)
	{
		screen_orientation = orientation;
	}
	
	public void changeScreenOrientation()
	{
		if(screen_orientation == HORIZONTAL_ORIENTATION)
			screen_orientation = VERTICAL_ORIENTATION;
		else
			screen_orientation = HORIZONTAL_ORIENTATION;
	}
	
	public void setPage_turn_hand(int page_turn_hand) {
		this.page_turn_hand = page_turn_hand;
	}
	
	public void changePage_turn_hand()
	{
		if(page_turn_hand == LEFT_HAND)
			page_turn_hand = RIGHT_HAND;
		else
			page_turn_hand = LEFT_HAND;
	}

	public void setPage_turn_orientation(int page_turn_orientation) {
		this.page_turn_orientation = page_turn_orientation;
	}
	
	public void changePage_turn_orientation()
	{
		if(page_turn_orientation == MODE_IN_VERTICAL_LEFT_RIGHT)
			page_turn_orientation = MODE_IN_VERTICAL_UP_DOWN;
		else {
			page_turn_orientation = MODE_IN_VERTICAL_LEFT_RIGHT;
		}
	}
	
	public void setPageOverByVolume(boolean pageOver)
	{
		pageOver_by_volume = pageOver;
	}
	
	public void setHideVirtualKey(boolean hideKey)
	{
		hide_virtual_key = hideKey;
	}
	
	public void setShowTimeBattery(boolean show)
	{
		show_time_battery = show;
	}
	
	public void setkeepScreenOn(boolean keepOn)
	{
		keep_screen_on = keepOn;
	}
	
	public void setNightMode(boolean on)
	{
		night_mode = on;
	}
	
	public void setLastReadLocal(String path)
	{
		last_read_local = path;
		setting.edit().putString(LAST_READ_LOCAL, last_read_local).apply();
	}
	
	
	public void commitAllAlter()
	{
		setting.edit().putInt(SCREEN_ORIENTATION, screen_orientation);
		setting.edit().putInt(PAGE_TURN_ORIENTATION, page_turn_orientation);
		setting.edit().putInt(PAGE_TURN_HAND, page_turn_hand);
		setting.edit().putBoolean(PAGEOVER_BY_VOLUME, pageOver_by_volume);
		setting.edit().putBoolean(HIDE_VIRTUAL_KEY, hide_virtual_key);
		setting.edit().putBoolean(SHOW_TIME_BATTERY, show_time_battery);
		setting.edit().putBoolean(KEEP_SCREEN_ON, keep_screen_on);
		setting.edit().putBoolean(NIGHT_MODE, night_mode);
		setting.edit().apply();
	}
	
	private AppSetting(Context context)
	{
		setting = context.getSharedPreferences(PREFS_NAME, 0);
		screen_orientation = setting.getInt(SCREEN_ORIENTATION, HORIZONTAL_ORIENTATION);
		page_turn_orientation = setting.getInt(PAGE_TURN_ORIENTATION, MODE_IN_VERTICAL_LEFT_RIGHT);
		page_turn_hand = setting.getInt(PAGE_TURN_HAND, LEFT_HAND);
		pageOver_by_volume = setting.getBoolean(PAGEOVER_BY_VOLUME, false);
		hide_virtual_key = setting.getBoolean(HIDE_VIRTUAL_KEY, false);
		
		show_time_battery = setting.getBoolean(SHOW_TIME_BATTERY, true);
		keep_screen_on = setting.getBoolean(KEEP_SCREEN_ON, true);
		night_mode = setting.getBoolean(NIGHT_MODE, false);
		
		last_read_local = setting.getString(LAST_READ_LOCAL, "");		
		
		int maxMemory = (int) (Runtime.getRuntime().maxMemory()/1024);
		Log.d("Comic","maxMemory:" + maxMemory / 1024 + "MB");
		cache = new LruCache<String, Bitmap>(maxMemory / 8)
				{
					protected int sizeOf(String key, Bitmap value) {
						return value.getByteCount()/1024;
					};
					
					@Override
					protected void entryRemoved(boolean evicted, String key,
							Bitmap oldValue, Bitmap newValue) {
						if(evicted)
						{
//							oldValue.recycle();
						}
					}
				};
	}
		
	
	public int getScreen_orientation() {
		return screen_orientation;
	}
	
	public int getPage_turn_hand() {
		return page_turn_hand;
	}
	
	
	public int getPage_turn_orientation() {
		return page_turn_orientation;
	}


	public boolean isPageOver_by_volume() {
		return pageOver_by_volume;
	}


	public boolean isHide_virtual_key() {
		return hide_virtual_key;
	}


	public boolean isShow_time_battery() {
		return show_time_battery;
	}


	public boolean isKeep_screen_on() {
		return keep_screen_on;
	}


	public boolean isNight_mode() {
		return night_mode;
	}
	
	public String getLastReadLocal()
	{
		return last_read_local;
	}

	public final static int HORIZONTAL_ORIENTATION = Configuration.ORIENTATION_LANDSCAPE;
	public final static int VERTICAL_ORIENTATION = Configuration.ORIENTATION_PORTRAIT;
	
	public final static int MODE_IN_VERTICAL_LEFT_RIGHT = 0;
	public final static int MODE_IN_VERTICAL_UP_DOWN = 1;
	
	public final static int LEFT_HAND = 0;
	public final static int RIGHT_HAND = 1;
	
	 
}
