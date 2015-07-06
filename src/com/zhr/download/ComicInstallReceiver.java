package com.zhr.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ComicInstallReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent != null)
		{	
//			Log.d("Comic", intent.getAction());
			//app安装和卸载，系统都会发送广播，PACKAGE_ADDED(安装)
			//PACKAGE_REMOVED(卸载),但app无法收到自身被安装或卸载的广播
			//但覆盖安装能收到发送的PACKAGE_REPLACED广播
			if(intent.getAction().equals("android.intent.action.PACKAGE_REPLACED"))
			{
				Log.d("Comic","packageName" + intent.getDataString());
			}
			
		}
		
	}

}
