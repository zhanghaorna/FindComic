package com.zhr.setting;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.tencent.bugly.proguard.m;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月28日
 * @description
 */
public class UpdateVersion {
	//apk存放的地址
	private String apk_url;
	//服务器的version版本
	private String ser_version;
	//服务器的地址
	public final String URL = "http://zhanghaorna.github.io/";
	//目前App的版本号
	private String cur_version;
	
	private final String UNKNOWN_VERSION = "版本号未知"; 
	
	private AsyncHttpClient client;
	private Context mContext;
	
	public OnUpdateListener listener;
	
	
	public UpdateVersion(Context context)
	{
		mContext = context;
		client = new AsyncHttpClient();
		cur_version = getVersionName();
	}
	
	//检查更新以回调方式进行检测
	public void checkUpdate()
	{
		client.get(URL, new JsonHttpResponseHandler()
		{
			public void onSuccess(int statusCode, Header[] headers, JSONObject response)
			{
				try
				{	
					ser_version = response.getString("version");
					apk_url = response.getString("apk_url");
					if(needUpdate(cur_version,ser_version)&&listener != null)
					{
						listener.onWithUpdate(ser_version,apk_url);
					}
					else if(listener!= null)
					{
						listener.onWithOutUpdate();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONArray errorResponse) {
				if(listener != null)
					listener.onWithOutUpdate();
			}
		});
	}
	//是否需要更新，appVersion本地版本，serverVersion服务器版本
	public boolean needUpdate(String appVersion,String serverVersion)
	{
		if(cur_version.endsWith(UNKNOWN_VERSION))
			return true;
		String[] cur_versions = appVersion.split("\\.");
		String[] ser_versions = serverVersion.split("\\.");
		try 
		{
			for(int i = 0;i < cur_versions.length;i++)
			{
				int cur_num = Integer.valueOf(cur_versions[i]);
				int ser_num = Integer.valueOf(ser_versions[i]);
				if(cur_num < ser_num)
				{
					return true;
				}
			}
			
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
		
		return false;
	}
	
	public String getVersionName()
	{	
		try 
		{
			PackageManager manager = mContext.getPackageManager();
			PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
			return info.versionName.replace("v", "");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return UNKNOWN_VERSION;
		}
	}
	
	public void setOnUpdateListener(OnUpdateListener listener)
	{
		this.listener = listener;
	}
	
	public interface OnUpdateListener
	{
		//需要更新
		void onWithUpdate(String version,String url);
		//不需要更新
		void onWithOutUpdate();
	}
	
	
	

}
