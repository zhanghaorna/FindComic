package com.zhr.util;

import java.lang.ref.WeakReference;


import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月29日
 * @description
 */
public class WeakFragmentHandler<T extends Fragment> extends Handler{
	protected WeakReference<T> mReference;
	
	public WeakFragmentHandler(T fragment)
	{
		mReference = new WeakReference<T>(fragment);
	}
	
	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		T fragment = mReference.get();
		if(fragment == null)
			return;
	}
}
