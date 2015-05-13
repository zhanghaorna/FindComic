package com.zhr.setting;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月7日
 * @description
 */
public class AppSetting {

	public static final String PREFS_NAME = "pref_app_setting";
	
	private final String SCREEN_ORIENTATION = "screen_orientation";
	private final String MODE_IN_VETICAL = "mode_in_vertical";
	private final String PAGEOVER_BY_VOLUME = "pageOver_by_volume";
	private final String HIDE_VIRTUAL_KEY = "hide_virtual_key";
	private final String SHOW_TIME_BATTERY = "show_time_battery";
	private final String KEEP_SCREEN_ON = "keep_screen_on";
	private final String NIGHT_MODE = "night_mode";
	
	private static AppSetting instance = null;
	
	//屏幕方向
	//横屏or竖屏
	private int screen_orientation;
	//竖屏阅读模式 从左往右or从上往下
	private int mode_in_vertical;
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
	
	//下载文件路径
	private String downloadFile;
	
	
	public synchronized static AppSetting getInstance(Context context)
	{
		if(instance == null)
		{
			instance = new AppSetting(context);
		}
		return instance;
	}
	
	private AppSetting()
	{
		
	}
	
	//此处不用commit,由于commit是同步，apply是异步
	public void setScreenOrientation(int orientation)
	{
		screen_orientation = orientation;
	}
	
	public void setModeInVertical(int mode)
	{
		mode_in_vertical = mode;
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
	
	
	public void commitAllAlter()
	{
		setting.edit().putInt(SCREEN_ORIENTATION, screen_orientation);
		setting.edit().putInt(MODE_IN_VETICAL, mode_in_vertical);
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
		mode_in_vertical = setting.getInt(MODE_IN_VETICAL, MODE_IN_VERTICAL_LEFT_RIGHT);
		pageOver_by_volume = setting.getBoolean(PAGEOVER_BY_VOLUME, false);
		hide_virtual_key = setting.getBoolean(HIDE_VIRTUAL_KEY, false);
		
		show_time_battery = setting.getBoolean(SHOW_TIME_BATTERY, true);
		keep_screen_on = setting.getBoolean(KEEP_SCREEN_ON, true);
		night_mode = setting.getBoolean(NIGHT_MODE, false);
		
	}
	
	
	
	public int getScreen_orientation() {
		return screen_orientation;
	}

	public int getMode_in_vertical() {
		return mode_in_vertical;
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


	public final static int HORIZONTAL_ORIENTATION = 0;
	public final static int VERTICAL_ORIENTATION = 1;
	
	public final static int MODE_IN_VERTICAL_LEFT_RIGHT = 0;
	public final static int MODE_IN_VERTICAL_UP_DOWN = 1;
	
	 
}
