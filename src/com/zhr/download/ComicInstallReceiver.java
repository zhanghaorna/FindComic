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
			if(intent.getAction().equals("android.intent.action.PACKAGE_ADDED"))
			{
				Log.d("Comic","packageName" + intent.getDataString());
			}
		}
		
	}

}
