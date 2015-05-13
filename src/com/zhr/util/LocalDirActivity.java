package com.zhr.util;

import java.io.File;
import java.io.FileFilter;

import com.zhr.findcomic.R;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月10日
 * @description
 */
public class LocalDirActivity extends BaseActivity implements OnClickListener
				,OnItemClickListener,OnScrollListener
{

	private ImageView back;
	private RelativeLayout last_readLayout;
	private TextView file_pathView;
	private Button continue_read;
	//初始化时图片有错位现象，由于Listview初始化多次调用getView导致(由于listview高度设为wrap_content,需要
	//在屏幕控件加载完全后才知道显示多少行数据，因此listview会尝试进行计算view高度),将listview设为固定高度或
	//match_parent可解决此问题
	private ListView file_listView;
	
	//listview是否在滚屏，在滚屏就不加载数据
	private boolean isBusy;
	private int visibleItemCount;
	
	private File[] files;
	private File currentFile;
	//文件过滤器，只保留文件夹或者以.jpg,.png
	private FileFilter fileFilter;
	
	private DirBaseAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_dir);
		initData();
		initView();
		
	}
	
	private void initData()
	{
		fileFilter = new FileFilter() {
			
			public boolean accept(File pathname) {
				if(pathname.isDirectory()||pathname.getName().endsWith(".png")
						||pathname.getName().endsWith(".jpg"))
				{
					return true;
				}
				return false;
			}
		};
		currentFile = new File("/mnt");
		files = currentFile.listFiles(fileFilter);
		

		
		adapter = new DirBaseAdapter();
	}
	
	private void initView()
	{
		back = (ImageView)findViewById(R.id.back);
		back.setOnClickListener(this);
		
		file_pathView = (TextView)findViewById(R.id.path);
		file_pathView.setText(currentFile.getAbsolutePath());
		
		last_readLayout = (RelativeLayout)findViewById(R.id.last_read);
		
		file_listView = (ListView)findViewById(R.id.file_listview);
		file_listView.setAdapter(adapter);
		file_listView.setOnItemClickListener(this);
		file_listView.setOnScrollListener(this);
		
	}

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
	
	private class DirBaseAdapter extends BaseAdapter
	{
//		private LruCache<String, Bitmap> mMemoryCache;
		
		public DirBaseAdapter()
		{
//			int maxMemory = (int) (Runtime.getRuntime().maxMemory()/1024);
//			int cacheSize = maxMemory / 8;
//			mMemoryCache = new LruCache<String, Bitmap>(cacheSize)
//					{
//						protected int sizeOf(String key, Bitmap value) {
//							return value.getByteCount()/1024;
//						};
//						
//						@Override
//						protected void entryRemoved(boolean evicted, String key,
//								Bitmap oldValue, Bitmap newValue) {
//							// TODO Auto-generated method stub
//							oldValue.recycle();
//							oldValue = null;
//						}
//					};
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return files != null ? files.length : 0;
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if(convertView == null)
			{
				holder = new ViewHolder();
				LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = layoutInflater.inflate(R.layout.local_dir_listview_item, parent,false);
				holder.iconView = (ImageView)view.findViewById(R.id.icon_dir);
				holder.pathView = (TextView)view.findViewById(R.id.path);
				convertView = view;				
			}
			else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.pathView.setText(files[position].getName());
			if(!files[position].isDirectory()&&!isBusy)
			{
				Log.d("File", files[position].getAbsolutePath());
				BitmapLoader.getInstance().loadImageNoCache(holder.iconView,
						files[position].getAbsolutePath());
			}
			else {
				holder.iconView.setImageResource(R.drawable.local_dir);
			}
			convertView.setTag(holder);			
			return convertView;
		}
		
		public class ViewHolder
		{
			TextView pathView;
			ImageView iconView;
		}
		
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		currentFile = files[position];
		files = currentFile.listFiles(fileFilter);
		adapter.notifyDataSetChanged();
		file_pathView.setText(currentFile.getAbsolutePath());
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(currentFile.getAbsolutePath().equals("/mnt"))
		{
			finish();
			overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
		}
		else
		{
			currentFile = currentFile.getParentFile();
			files = currentFile.listFiles(fileFilter);
			adapter.notifyDataSetChanged();
			file_pathView.setText(currentFile.getAbsolutePath());
		}
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_IDLE:
			isBusy = false;
			int first = view.getFirstVisiblePosition();
			for(int i = 0;i <visibleItemCount;i++)
			{
				View convertView = view.getChildAt(i);
				BitmapLoader.getInstance().loadImageNoCache(((DirBaseAdapter.ViewHolder)convertView.getTag()).iconView,
						files[first + i].getAbsolutePath());
				
			}
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			isBusy = true;
			break;
		case OnScrollListener.SCROLL_STATE_FLING:
			isBusy = true;
			break;
		default:
			break;
		}
	}

	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		this.visibleItemCount = visibleItemCount;
	}


}
