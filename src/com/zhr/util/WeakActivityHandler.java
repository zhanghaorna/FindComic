package com.zhr.util;

import java.lang.ref.WeakReference;

import android.app.Activity;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月29日
 * @description
 */
public class WeakActivityHandler <T extends Activity>{
	protected WeakReference<T> mReference;
	
	public WeakActivityHandler()
	{
		
	}
}
