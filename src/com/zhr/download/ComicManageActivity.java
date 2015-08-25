package com.zhr.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.zhr.database.DBComicDownloadDetailHelper;
import com.zhr.database.DBComicDownloadHelper;
import com.zhr.findcomic.R;
import com.zhr.setting.AppSetting;
import com.zhr.sqlitedao.ComicDownload;
import com.zhr.sqlitedao.ComicDownloadDetail;
import com.zhr.util.Constants;
import com.zhr.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ComicManageActivity extends Activity implements OnClickListener{
	
	private final int UNFINISH_MODE = 0;
	private final int FINISH_MODE = 1;
	private final int EDIT_MODE = 2;
	private final int NONE_MODE = 3;
	private int mode = 0;
	
	private ImageView backView;
	
	private TextView leftView;
	private TextView middleView;
	private TextView rightView;
	private View leftDivideView;
	private View rightDivideView;
	//删除text的color drawable
	private ColorStateList delStateList;
	private ColorStateList dirStateList;
	
	private ListView comicDetaiListView;
	private ComicDownloadDetailAdapter mAdapter;
	
	private String comicName;
	private List<ComicDownloadDetail> cDetails;
	private boolean[] choose;
	
	private DownloadService dService;
	
	private DownloadBroadcast downloadBroadcast;
	private IntentFilter intentFilter;
	
	private int chooseCount = 0;
	
	//检测是否在下载中
	private boolean downloading = false;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comic_manage);
		initView();
		initData();
	}
	
	private void initView()
	{
		leftView = (TextView)findViewById(R.id.left);
		leftView.setOnClickListener(this);
		middleView = (TextView)findViewById(R.id.middle);
		middleView.setOnClickListener(this);
		rightView = (TextView)findViewById(R.id.right);
		rightView.setOnClickListener(this);
		leftDivideView = (View)findViewById(R.id.divider_left);
		rightDivideView = (View)findViewById(R.id.divider_right);
		
		backView = (ImageView)findViewById(R.id.back);
		backView.setOnClickListener(this);
		
		comicDetaiListView = (ListView)findViewById(R.id.chapter_listview);
		comicDetaiListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		
		delStateList = getResources().getColorStateList(R.drawable.cm_del_text_color);
		dirStateList = getResources().getColorStateList(R.drawable.textview_color_click);
	}
	
	private void initData()
	{
		Intent intent = getIntent();
		if(intent.getStringExtra("comicName") != null)
		{
			comicName = intent.getStringExtra("comicName");
			cDetails = DBComicDownloadDetailHelper.getInstance(getApplicationContext())
					.getComicDownloadDetails(comicName);
			choose = new boolean[cDetails.size()];
			changeModeView();
			checkDownloadStatus();
			mAdapter = new ComicDownloadDetailAdapter();
			comicDetaiListView.setAdapter(mAdapter);
			
			intentFilter = new IntentFilter();
			intentFilter.addAction(DownloadService.CHAPTER_FINISHING_OR_PAUSED);
			intentFilter.addAction(DownloadService.NETWORK_ERROR);
			intentFilter.addAction(DownloadService.DOWNLOAD_PAGE_FINISHED);
			intentFilter.addAction(DownloadService.DOWNLOAD_STATE_CHANGE);
			intentFilter.setPriority(200);
			
			downloadBroadcast = new DownloadBroadcast();
			bindService(new Intent(this,DownloadService.class), mConnection, BIND_AUTO_CREATE);
			registerReceiver(downloadBroadcast, intentFilter);
		}
	}
	
	//检测是否在下载
	private void checkDownloadStatus()
	{
		downloading = false;
		for(int i = 0;i < cDetails.size();i++)
		{
			if(cDetails.get(i).getStatus() == Constants.DOWNLOADING
					||cDetails.get(i).getStatus() == Constants.WAITING)
			{
				downloading = true;
				break;
			}
		}
		if(downloading)
			middleView.setText("全部暂停");
		else 
			middleView.setText("全部开始");
		
	}
	
	//根据不同的mode显示不同的view
	private void changeModeView()
	{
		//如果mode不为EDIT_MODE则，自动查询所有下载检测一遍
		if(mode != EDIT_MODE&&mode != NONE_MODE)
		{
			mode = FINISH_MODE;
			for(int i = 0;i < cDetails.size();i++)
			{
				if(cDetails.get(i).getStatus() != Constants.FINISHED)
				{
					mode = UNFINISH_MODE;
					break;
				}
			}
		}

		if(mode == FINISH_MODE)
		{
			leftView.setText("目录");
			rightView.setText("编辑");
			middleView.setVisibility(View.GONE);
			leftDivideView.setVisibility(View.GONE);
			leftView.setTextColor(dirStateList);
		}
		else if(mode == UNFINISH_MODE)
		{
			middleView.setVisibility(View.VISIBLE);
			leftDivideView.setVisibility(View.VISIBLE);
			leftView.setText("目录");
			leftView.setTextColor(dirStateList);
			middleView.setText("全部开始");
			rightView.setText("编辑");
			
		}
		else if(mode == EDIT_MODE)
		{
			middleView.setVisibility(View.VISIBLE);
			leftDivideView.setVisibility(View.VISIBLE);
			leftView.setText("删除");
			leftView.setTextColor(delStateList);
			leftView.setEnabled(false);
			middleView.setText("全选");
			rightView.setText("完成");		
		}
		else if(mode == NONE_MODE)
		{
			leftView.setVisibility(View.GONE);
			middleView.setVisibility(View.GONE);
			rightView.setVisibility(View.GONE);
			leftDivideView.setVisibility(View.GONE);
			rightDivideView.setVisibility(View.GONE);
		}
	}
	
	private void delChooseChapter()
	{
		List<ComicDownloadDetail> delDetails = new ArrayList<ComicDownloadDetail>();
		for(int i = 0;i < choose.length;i++)
		{
			if(choose[i])
			{
				ComicDownloadDetail cDetail = cDetails.get(i);
				delDetails.add(cDetail);
				File file = new File(AppSetting.getInstance(getApplicationContext())
						.getDownloadPath() + File.separator + comicName 
						+ File.separator + cDetail.getChapter());
				Util.removeFile(file);
			}
		}
		DBComicDownloadDetailHelper.getInstance(getApplicationContext())
				.deleteChooseDetails(delDetails);
		cDetails = DBComicDownloadDetailHelper.getInstance(getApplicationContext())
				.getComicDownloadDetails(comicName);
		
		ComicDownload comicDownload = DBComicDownloadHelper.getInstance(getApplicationContext())
				.getComicDownload(comicName);
		if(cDetails == null||cDetails.size() == 0)
		{
			mode = NONE_MODE;
			DBComicDownloadHelper.getInstance(getApplicationContext())
					.deleteComicDownload(comicName);
			Toast.makeText(this, "该漫画已经全部删除", Toast.LENGTH_SHORT).show();
		}
		else
		{
			mode = UNFINISH_MODE;
			choose = new boolean[cDetails.size()];
			comicDownload.setChapterNum(comicDownload.getChapterNum() - chooseCount);
			DBComicDownloadHelper.getInstance(getApplicationContext())
					.saveComicDownload(comicDownload);
		}	
		
		changeModeView();
		mAdapter.notifyDataSetChanged();
	}
	
	private void startDownload(ComicDownloadDetail cDetail)
	{
		cDetail.setStatus(Constants.WAITING);
		dService.startDownload(cDetail);
	}
	
	private void pauseDownload(ComicDownloadDetail cDetail)
	{
		if(cDetail.getStatus() == Constants.DOWNLOADING)
			cDetail.setStatus(Constants.PAUSEING);
		else if(cDetail.getStatus() == Constants.WAITING) 
			cDetail.setStatus(Constants.PAUSED);
		dService.pauseDownload(cDetail);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unbindService(mConnection);
		unregisterReceiver(downloadBroadcast);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.right:
			if(mode != EDIT_MODE)
			{
				if(downloading)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage("正在下载中，请先暂停")
						   .setPositiveButton("确定", null)
						   .create().show();
					return;
				}
				mode = EDIT_MODE;
				for(int i = 0;i < choose.length;i++)
					choose[i] = false;
				chooseCount = 0;
				changeModeView();
				mAdapter.notifyDataSetChanged();
			}
			else
			{
				mode = FINISH_MODE;
				changeModeView();
				mAdapter.notifyDataSetChanged();
			}
			break;
		case R.id.middle:
			if(mode != EDIT_MODE)
			{
				//如在下载，则是暂停全部，否则是全部开始
				if(downloading)
				{
					for(int i = 0;i < cDetails.size();i++)
					{
						if(cDetails.get(i).getStatus() == Constants.DOWNLOADING
								||cDetails.get(i).getStatus() == Constants.WAITING)
						{
							pauseDownload(cDetails.get(i));
						}
					}
				}
				else
				{
					for(int i = 0;i < cDetails.size();i++)
					{
						if(cDetails.get(i).getStatus() == Constants.PAUSED)
						{
							startDownload(cDetails.get(i));
						}
					}
				}
				checkDownloadStatus();
				mAdapter.notifyDataSetChanged();
			}
			else
			{
				for(int i = 0;i < choose.length;i++)
					choose[i] = true;
				chooseCount = choose.length;
				leftView.setEnabled(true);
				mAdapter.notifyDataSetInvalidated();
			}
			break;
		case R.id.left:
			//目录，暂时不好存储数据库记录，因此暂时并不跳转过去
			if(mode != EDIT_MODE)
			{
				
			}
			else
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("确定删除所选择的漫画章节?")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							delChooseChapter();
						}
					})
					.setNegativeButton("取消", null);
				builder.create().show();
			}
			break;
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
	
	private class DownloadBroadcast extends BroadcastReceiver
	{
		public void onReceive(Context context, Intent intent) {
			Log.d("Comic", "dm finish");
			if(intent != null)
			{				
				if(intent.getStringExtra("comicName").equals(comicName))
				{
					checkDownloadStatus();
					mAdapter.notifyDataSetInvalidated();			
				}
			}			
		}		
	}
	
	private class ComicDownloadDetailAdapter extends BaseAdapter
	{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return cDetails.size() + 1;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position	;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int type = getItemViewType(position);
			ViewHolder viewHolder;
			if(convertView == null)
			{
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				switch (type) {
				case 0:
					convertView = inflater.inflate(R.layout.comic_manage_listview_item_1, parent,false);
					break;
				case 1:
					convertView = inflater.inflate(R.layout.comic_manage_listview_item_2, parent,false);
					viewHolder = new ViewHolder();
					viewHolder.chapterTextView = (TextView) convertView.findViewById(R.id.chapter);
					viewHolder.statusTextView = (TextView) convertView.findViewById(R.id.status);
					viewHolder.progressTextView = (TextView) convertView.findViewById(R.id.progress_text);
					viewHolder.progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);
					viewHolder.statusImageView = (ImageView) convertView.findViewById(R.id.status_icon);
					convertView.setTag(viewHolder);
					viewHolder.statusImageView.setOnClickListener(new OnStatusClickListener(convertView));
					break;
				default:
					break;
				}
			}
			//由于0是显示漫画名字的位置，因此实际必须先将position减1后在获取位置
			convertView.setTag(R.id.cm_adapter_convertview, position - 1);
			switch (type) {
			case 0:
				TextView view = (TextView) convertView.findViewById(R.id.title);
				view.setText(comicName);
				break;
			case 1:
				viewHolder = (ViewHolder) convertView.getTag();
				viewHolder.chapterTextView.setText(cDetails.get(position - 1).getChapter());
				viewHolder.progressTextView.setText(cDetails.get(position - 1).getFinishNum()
						+ "/" + cDetails.get(position - 1).getPageNum());
				
				if(cDetails.get(position - 1).getFinishNum() == 0)
					viewHolder.progressBar.setProgress(0);
				else 
					viewHolder.progressBar.setProgress(cDetails.get(position - 1).getFinishNum() * 100
							/cDetails.get(position - 1).getPageNum());	
				if(cDetails.get(position - 1).getStatus() == Constants.WAITING)
				{
					viewHolder.statusImageView.setImageResource(R.drawable.dm_waiting);
					viewHolder.statusTextView.setText("等待中");				
				}
				else if(cDetails.get(position - 1).getStatus() == Constants.PAUSED)
				{
					viewHolder.statusImageView.setImageResource(R.drawable.dm_resume_s);
					viewHolder.statusTextView.setText("已暂停");
				}
				else if(cDetails.get(position - 1).getStatus() == Constants.DOWNLOADING)
				{
					viewHolder.statusImageView.setImageResource(R.drawable.dm_paused_s);
					viewHolder.statusTextView.setText("下载中");
				}
				else if(cDetails.get(position - 1).getStatus() == Constants.PAUSEING)
				{
					viewHolder.statusImageView.setImageResource(R.drawable.dm_resume_s);
					viewHolder.statusTextView.setText("正在暂停...");
				}
				else {
					viewHolder.statusImageView.setImageResource(R.drawable.dm_complete);
					viewHolder.statusTextView.setText("已完成");
				}
				if(mode == EDIT_MODE)
				{
					if(choose[position - 1])
						viewHolder.statusImageView.setImageResource(R.drawable.checkboxbg_c);
					else
						viewHolder.statusImageView.setImageResource(R.drawable.checkboxbg);
				}
			default:
				break;
			}
			return convertView;
		}
		
		
		@Override
		public int getViewTypeCount() {
			// TODO Auto-generated method stub
			return 2;
		}
		//这里返回的Type必须从1开始
		@Override
		public int getItemViewType(int position) {
			// TODO Auto-generated method stub
			if(position == 0)
				return 0;
			else 
				return 1;
		}
		
		class ViewHolder
		{
			TextView chapterTextView;
			TextView statusTextView;
			TextView progressTextView;
			ProgressBar progressBar;
			ImageView statusImageView;
		}
		
		private class OnStatusClickListener implements OnClickListener
		{
			private View convertView;
			public OnStatusClickListener(View convertView)
			{
				this.convertView = convertView;
			}
			
			@Override
			public void onClick(View v) {
				int position = (Integer) convertView.getTag(R.id.cm_adapter_convertview);
				if(mode == EDIT_MODE)
				{
					choose[position] = !choose[position];
					if(!choose[position])
					{
						((ImageView)v).setImageResource(R.drawable.checkboxbg);	
						chooseCount--;
						if(chooseCount == 0)
							leftView.setEnabled(false);
					}
					else 
					{
						chooseCount++;
						if(chooseCount > 0)
							leftView.setEnabled(true);
						((ImageView)v).setImageResource(R.drawable.checkboxbg_c);	
					}
					return;
				}
				if(cDetails.get(position).getStatus() == Constants.DOWNLOADING)
				{

					pauseDownload(cDetails.get(position));
					mAdapter.notifyDataSetChanged();
				}
				else if(cDetails.get(position).getStatus() == Constants.PAUSED)
				{
					startDownload(cDetails.get(position));
					mAdapter.notifyDataSetChanged();
				}
				checkDownloadStatus();
				
			}
			
		}
		
	}
}
