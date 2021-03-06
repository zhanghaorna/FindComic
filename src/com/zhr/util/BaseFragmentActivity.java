package com.zhr.util;

import com.zhr.findcomic.R;
import com.zhr.setting.AppSetting;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月9日
 * @description
 */
public class BaseFragmentActivity extends FragmentActivity{
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams lp;
	private TextView coverView;
	private boolean isAddView = false;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		lp = new WindowManager.LayoutParams(  
				WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT,  
                WindowManager.LayoutParams.TYPE_APPLICATION,  
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE  
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,  
                PixelFormat.TRANSLUCENT);
		coverView = new TextView(this);
		//背景设为透明黑色
		coverView.setBackgroundColor(getResources().getColor(R.color.tran_black));
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(isAddView)
		{
			mWindowManager.removeView(coverView);
			isAddView = false;
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		changeNightMode();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();		
	}
	
	protected void changeNightMode()
	{
		if(AppSetting.getInstance(getApplicationContext()).isNight_mode()&&!isAddView)
		{
			mWindowManager.addView(coverView, lp);
			isAddView = true;
		}
		else 
		{
			if(isAddView&&!AppSetting.getInstance(getApplicationContext()).isNight_mode())
				mWindowManager.removeView(coverView);
			isAddView = false;
		}	
	}
	
	

}
