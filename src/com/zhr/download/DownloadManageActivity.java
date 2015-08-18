package com.zhr.download;

import java.io.File;
import java.util.List;

import android.R.menu;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhr.database.DBComicDownloadHelper;
import com.zhr.findcomic.R;
import com.zhr.setting.AppSetting;
import com.zhr.sqlitedao.ComicDownload;
import com.zhr.util.BaseActivity;
import com.zhr.util.BitmapLoader;
import com.zhr.util.Constants;

public class DownloadManageActivity extends BaseActivity implements OnClickListener{
	
	private TextView titleTextView;
	private ImageView back;
	
	private List<ComicDownload> comicInfos;
	
	private ComicDownload choosedDownload;
	
	private ListView comicInfosView;
	private ComicInfoAdapter mAdapter;
	
	private AlertDialog.Builder builder;
	private DownloadService dService;
	
	private String path;
	
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
		
		
		builder = new AlertDialog.Builder(this);
		builder.setNegativeButton("取消", null);
	}
	
	private void initData()
	{
		comicInfos = DBComicDownloadHelper.getInstance(this).getComicDownloads();
		mAdapter = new ComicInfoAdapter();
		comicInfosView.setAdapter(mAdapter);
		
		path = AppSetting.getInstance(getApplicationContext()).getDownloadPath();
		
	}
	
	private void showMenuDialog(ComicDownload cDownload)
	{
		choosedDownload = cDownload;
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
		
	}
	
	private void deleteComic(String comicName)
	{
		
	}
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		bindService(new Intent(this,DownloadService.class), mConnection, BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unbindService(mConnection);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
			break;

		default:
			break;
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
		}
	};
	
	
	
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
			else {
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
}
