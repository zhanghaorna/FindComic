package com.zhr.util;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月29日
 * @description
 */
public class WeakActivityHandler <T extends Activity> extends Handler{
	protected WeakReference<T> mReference;
	
	public WeakActivityHandler(T activty)
	{
		mReference = new WeakReference<T>(activty);
	}
	
	@Override
	public void handleMessage(Message msg) {
		T activty = mReference.get();
		if(activty == null)
			return;
	}
}
