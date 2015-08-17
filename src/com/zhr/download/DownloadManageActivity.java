package com.zhr.download;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
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
import com.zhr.sqlitedao.ComicDownload;
import com.zhr.util.BaseActivity;
import com.zhr.util.Constants;

public class DownloadManageActivity extends BaseActivity implements OnClickListener{
	
	private TextView titleTextView;
	private ImageView back;
	
	private List<ComicDownload> comicInfos;
	
	private ListView comicInfosView;
	
	private AlertDialog dialog;
	
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
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setItems(items, listener)
		
	}
	
	private void initData()
	{
		comicInfos = DBComicDownloadHelper.getInstance(this).getComicDownloads();
		
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
			final String comicName = comicInfos.get(position).getComicName();
			
			
			return convertView;
		}
		
		class ViewHolder
		{
			ImageView imageView;
			TextView titleView;
			TextView statusView;
			Button menuButton;
		}
	}
}
