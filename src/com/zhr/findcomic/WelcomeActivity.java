package com.zhr.findcomic;

import com.zhr.database.DBNewsHelper;
import com.zhr.setting.AppSetting;
import com.zhr.util.BitmapLoader;
import com.zhr.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月9日
 * @description
 */
public class WelcomeActivity extends Activity{
	
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		//初始化系统设置
		AppSetting.getInstance(getApplicationContext());
		BitmapLoader.getInstance();
		//确保存放缓存的文件夹存在
		Util.createFile();
		//初始化数据库
		DBNewsHelper.getInstance(getApplicationContext()).deleteAllNews();
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
				startActivity(intent);
				finish();
			}
		}, 2000);
		
	}

}
