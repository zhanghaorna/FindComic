package com.zhr.findcomic;

import java.io.File;
import java.util.List;

import com.tencent.bugly.crashreport.CrashReport;
import com.zhr.database.DBComicDownloadDetailHelper;
import com.zhr.database.DBComicDownloadHelper;
import com.zhr.database.DBNewsHelper;
import com.zhr.setting.AppSetting;
import com.zhr.sqlitedao.ComicDownload;
import com.zhr.sqlitedao.ComicDownloadDetail;
import com.zhr.util.BitmapLoader;
import com.zhr.util.Constants;
import com.zhr.util.CrashHandler;
import com.zhr.util.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

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
		
		//true表示在调试阶段
		CrashReport.initCrashReport(getApplicationContext(),Constants.BUGLY_ID , true);
		//只是为了顺利退出
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
		
		//初始化系统设置
		AppSetting.getInstance(getApplicationContext());
		BitmapLoader.getInstance();
		//确保存放缓存的文件夹存在
		Util.createFile();
		//初始化数据库
		DBNewsHelper.getInstance(getApplicationContext()).deleteAllNews();
		DBComicDownloadHelper.getInstance(getApplicationContext());
		
		//创建初始化文件夹
		File saveFile = new File(AppSetting.getInstance(this).getDownloadPath());
		if(!saveFile.exists())
		{
			saveFile.mkdirs();
		}
		

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			
			public void run() {
				
				Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
				startActivity(intent);
				finish();
			}
		}, 2000);
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d("Comic", "onResume");
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d("Comic", "onPause");
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d("Comic", "onStop");
	}

}
