package com.zhr.download;


import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import android.R.integer;
import android.R.menu;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhr.database.DBComicDownloadDetailHelper;
import com.zhr.database.DBComicDownloadHelper;
import com.zhr.findcomic.R;
import com.zhr.setting.AppSetting;
import com.zhr.sqlitedao.ComicDownload;
import com.zhr.util.BaseActivity;
import com.zhr.util.BitmapLoader;
import com.zhr.util.Constants;
import com.zhr.util.Util;

public class DownloadManageActivity extends Activity implements OnClickListener
		,OnItemClickListener
{
	
	private TextView titleTextView;
	private ImageView back;
	
	private List<ComicDownload> comicInfos;
	
	private ComicDownload choosedDownload;
	
	private ListView comicInfosView;
	private ComicInfoAdapter mAdapter;
	
	private AlertDialog.Builder builder;
	private DownloadService dService;
	
	private String path;
	
	private TextView statusView;
	private ImageView statusImageView;
	private int status;
	
	private DownloadBroadcast downloadBroadcast;
//	private LocalBroadcastManager lbManager;
	private IntentFilter intentFilter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_manage);
		
		
		initView();
		initData();
	}
	
	private void initView()
	{
		titleTextView = (TextView)findViewById(R.id.title);
		titleTextView.setText(getResources().getString(R.string.download_manage));
		
		back = (ImageView)findViewById(R.id.back);
		back.setOnClickListener(this);
		
		comicInfosView = (ListView)findViewById(R.id.download_list);
		
		statusImageView = (ImageView)findViewById(R.id.status_icon);
		statusView = (TextView)findViewById(R.id.status);
		statusImageView.setOnClickListener(this);
		
		builder = new AlertDialog.Builder(this);
		builder.setNegativeButton("取消", null);
	}
	
	private void initData()
	{
		comicInfos = DBComicDownloadHelper.getInstance(this).getComicDownloads();
		mAdapter = new ComicInfoAdapter();
		comicInfosView.setAdapter(mAdapter);
		comicInfosView.setOnItemClickListener(this);
		
		path = AppSetting.getInstance(getApplicationContext()).getDownloadPath();
		
//		lbManager = LocalBroadcastManager.getInstance(getApplicationContext());
		intentFilter = new IntentFilter();
		intentFilter.addAction(DownloadService.CHAPTER_FINISHING_OR_PAUSED);
		intentFilter.addAction(DownloadService.NETWORK_ERROR);
		intentFilter.addAction(DownloadService.DOWNLOAD_STATE_CHANGE);
		intentFilter.setPriority(200);
		
		downloadBroadcast = new DownloadBroadcast();
		

	}
	
	private void showMenuDialog(ComicDownload cDownload)
	{
		choosedDownload = cDownload;
		builder.setTitle(cDownload.getComicName());
		switch (cDownload.getStatus()) {
		case Constants.WAITING:
		case Constants.DOWNLOADING:
			builder.setItems(getResources().getStringArray(R.array.dm_downloading),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0:
								enterChapterManager(choosedDownload.getComicName());
								break;
							case 1:
								dService.pauseDownload(choosedDownload.getComicName());
								choosedDownload.setStatus(Constants.PAUSED);
								mAdapter.notifyDataSetChanged();
								break;
							default:
								break;
							}
							
						}
					});
			break;
		case Constants.PAUSED:
			builder.setItems(getResources().getStringArray(R.array.dm_pause),
					new DialogInterface.OnClickListener() {					
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0:
								enterChapterManager(choosedDownload.getComicName());
								break;
							case 1:
								dService.startDownload(choosedDownload.getComicName());
								choosedDownload.setStatus(Constants.WAITING);
								mAdapter.notifyDataSetChanged();
								break;
							case 2:
								deleteComic(choosedDownload.getComicName());
							default:
								break;
							}
							
						}
					});
			break;
		case Constants.FINISHED:
			builder.setItems(getResources().getStringArray(R.array.dm_finished),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0:
								enterChapterManager(choosedDownload.getComicName());
								break;
							case 1:
								deleteComic(choosedDownload.getComicName());
							default:
								break;
							}							
						}
					});
			break;
		default:
			break;
		}
		
		builder.create().show();
	}
	
	private void enterChapterManager(String comicName)
	{
		Intent intent = new Intent(this,ComicManageActivity.class);
		intent.putExtra("comicName", comicName);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
	}
	
	private void deleteComic(String comicName)
	{
		comicInfos.remove(choosedDownload);
		mAdapter.notifyDataSetChanged();
		DBComicDownloadHelper.getInstance(getApplicationContext())
					.deleteComicDownload(comicName);
		DBComicDownloadDetailHelper.getInstance(getApplicationContext())
					.deleteDownloadDetails(comicName);
		File file = new File(AppSetting.getInstance(getApplicationContext())
				.getDownloadPath() + File.separator + comicName);
		Util.removeFile(file);
		checkDownloadStatus();

	}
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		bindService(new Intent(this,DownloadService.class), mConnection, BIND_AUTO_CREATE);
		registerReceiver(downloadBroadcast, intentFilter);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unbindService(mConnection);
		unregisterReceiver(downloadBroadcast);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
			break;
		case R.id.status_icon:
			if(status == Constants.PAUSED)
			{
				for(int i = 0;i < comicInfos.size();i++)
				{
					if(comicInfos.get(i).getStatus() != Constants.FINISHED)
						dService.startDownload(comicInfos.get(i).getComicName());
				}				
			}
			else if(status == Constants.DOWNLOADING)
			{
				for(int i = 0;i < comicInfos.size();i++)
				{
					if(comicInfos.get(i).getStatus() != Constants.FINISHED&&
							comicInfos.get(i).getStatus() != Constants.PAUSED)
						dService.pauseDownload(comicInfos.get(i).getComicName());
				}	
			}
			checkDownloadStatus();
			mAdapter.notifyDataSetChanged();
		default:
			break;
		}
		
	}
	
	private void checkDownloadStatus()
	{
		if(dService.isDownloading())
		{
			statusView.setText("下载中");
			statusImageView.setVisibility(View.VISIBLE);
			statusImageView.setImageResource(R.drawable.dm_paused_s);
			status = Constants.DOWNLOADING;
		}
		else
		{
			status = Constants.FINISHED;
			for(int i = 0;i < comicInfos.size();i++)
			{
				if(comicInfos.get(i).getStatus() != Constants.FINISHED)
				{
					status = Constants.PAUSED;
					break;
				}
			}
			if(status == Constants.FINISHED)
			{
				statusView.setText("已完成");
				statusImageView.setVisibility(View.GONE);
			}
			else
			{
				statusView.setText("已暂停");
				statusImageView.setVisibility(View.VISIBLE);
				statusImageView.setImageResource(R.drawable.dm_resume_s);
			}
		}
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			dService = null;
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {		
			dService = ((DownloadService.LocalBinder)service).getService();
			checkDownloadStatus();
		}
	};
	
	private class DownloadBroadcast extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("Comic", "dm finish");
			if(intent != null)
			{				
				mAdapter.notifyDataSetInvalidated();
				checkDownloadStatus();				
			}
			
		}
		
	}
	
	private class ComicInfoAdapter extends BaseAdapter
	{

		@Override
		public int getCount() {
			return comicInfos.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if(convertView == null)
			{
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.download_manage_listview_item, parent,false);
				viewHolder = new ViewHolder();
				viewHolder.imageView = (ImageView) convertView.findViewById(R.id.comic_cover);
				viewHolder.titleView = (TextView) convertView.findViewById(R.id.comic_title);
				viewHolder.statusView = (TextView)convertView.findViewById(R.id.comic_status);
				viewHolder.menuButton = (Button) convertView.findViewById(R.id.comic_menu);
				convertView.setTag(viewHolder);
				viewHolder.menuButton.setOnClickListener(new OnMenuClickListener(convertView));
				
			}
			else
			{
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.titleView.setText(comicInfos.get(position).getComicName());
			if(comicInfos.get(position).getStatus() == Constants.DOWNLOADING)
			{
				viewHolder.statusView.setText("下载中");
			}
			else if(comicInfos.get(position).getStatus() == Constants.FINISHED)
			{
				viewHolder.statusView.setText("下载完成(" + comicInfos.get(position).getChapterNum() + ")");
			}
			else if(comicInfos.get(position).getStatus() == Constants.WAITING)
			{
				viewHolder.statusView.setText("等待中");
			}
			else
			{
				viewHolder.statusView.setText("已暂停");
			}
			BitmapLoader.getInstance().loadImageNoCache(viewHolder.imageView,
					path + File.separator + comicInfos.get(position).getComicName()
					+ File.separator + comicInfos.get(position).getComicName() + ".jpg" , false);
			convertView.setTag(R.id.dm_adapter_convertview,position);
			
			
			return convertView;
		}
		
		class ViewHolder
		{
			ImageView imageView;
			TextView titleView;
			TextView statusView;
			Button menuButton;
		}
		
		private class OnMenuClickListener implements OnClickListener
		{
			View convertView;
			public OnMenuClickListener(View convertView)
			{
				this.convertView = convertView;
			}
			
			@Override
			public void onClick(View v) {
				int position = (Integer) convertView.getTag(R.id.dm_adapter_convertview);
				showMenuDialog(comicInfos.get(position));
			}
			
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		enterChapterManager(comicInfos.get(position).getComicName());		
	}
}
