package com.zhr.util;

import java.io.File;
import java.io.FileFilter;

import com.zhr.comic.ComicReadActivity;
import com.zhr.findcomic.R;
import com.zhr.setting.AppSetting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月10日
 * @description
 */
public class LocalDirActivity extends BaseActivity implements OnClickListener
				,OnItemClickListener,OnScrollListener
{

	public static final int LAST_READ = 0;
	
	private ImageView back;
	private TextView file_pathView;
	
	private TextView last_readView;
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
	private FileFilter picFilter;
	
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
		picFilter = new FileFilter() {
			public boolean accept(File pathname) {
				if(pathname.getName().endsWith(".png")
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
		
		
		file_listView = (ListView)findViewById(R.id.file_listview);
		file_listView.setAdapter(adapter);
		file_listView.setOnItemClickListener(this);
		file_listView.setOnScrollListener(this);
		
		last_readView = (TextView)findViewById(R.id.last_read_path);
		last_readView.setText(AppSetting.getInstance(getApplicationContext()).getLastReadLocal());
		
		continue_read = (Button)findViewById(R.id.continue_read);
		continue_read.setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
			break;
		case R.id.continue_read:
			if(file_pathView.getText().toString().equals(""))
				Toast.makeText(getBaseContext(), "你还没有阅读过本地漫画", Toast.LENGTH_SHORT).show();
			else
			{
				File file = new File(AppSetting.getInstance(getApplicationContext()).getLastReadLocal());
				if(file.exists()&&(file.getAbsolutePath().endsWith(".jpg")||
						file.getAbsolutePath().endsWith(".png")))
				{
					Intent intent = new Intent(this,ComicReadActivity.class);
					File parentFile = file.getParentFile();
					File[] tempFiles = parentFile.listFiles(picFilter);
					if(tempFiles.length <= 0)
					{
						Toast.makeText(getBaseContext(), "漫画文件已不存在", Toast.LENGTH_SHORT).show();
						return;
					}
					String[] picPaths = new String[tempFiles.length];
					int index = 0;
					for(int i = 0;i < tempFiles.length;i++)
					{
						picPaths[i] = tempFiles[i].getAbsolutePath();
						if(tempFiles[i].getAbsolutePath().equals(file.getAbsolutePath()))
						{
							index = i;
						}
					}
					intent.putExtra("picPaths", picPaths);
					intent.putExtra("position", index);
					intent.putExtra("comicName", tempFiles[index].getParentFile().getName());
					startActivityForResult(intent, LAST_READ);
				}
				else {
					Toast.makeText(getBaseContext(), "漫画文件已不存在", Toast.LENGTH_SHORT).show();
				}
			}
		default:
			break;
		}
		
	}
	
	//获取position保存上次阅读的地方
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == LAST_READ)
		{
			if(resultCode == RESULT_OK)
			{
				String path = data.getStringExtra("last_read_path");
				if(path != null&&(path.endsWith(".jpg")||path.endsWith(".png")))
					AppSetting.getInstance(getApplicationContext()).setLastReadLocal(path);
				last_readView.setText(AppSetting.getInstance(getApplicationContext()).getLastReadLocal());
			}
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
				convertView.setTag(holder);	
			}
			else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.pathView.setText(files[position].getName());
			holder.iconView.setImageResource(R.drawable.local_dir);
			if(!files[position].isDirectory()&&(files[position].getAbsolutePath()
					.endsWith(".jpg")||files[position].getAbsolutePath()
					.endsWith(".png"))&&!isBusy)
			{
				Log.d("File", files[position].getAbsolutePath());
				BitmapLoader.getInstance().loadImageNoCache(holder.iconView,
						files[position].getAbsolutePath(),true);
			}

					
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
		if (files[position].getName().endsWith(".jpg")||files[position].getName().endsWith(".png")) {
			Intent intent = new Intent(this,ComicReadActivity.class);
			String[] picPaths = new String[files.length];
			for(int i = 0;i < files.length;i++)
				picPaths[i] = files[i].getAbsolutePath();
			intent.putExtra("picPaths", picPaths);
			intent.putExtra("position", position);
			intent.putExtra("comicName", files[position].getParentFile().getName());
			startActivityForResult(intent, LAST_READ);
			return;
		}
		else
		{
			currentFile = files[position];
			files = currentFile.listFiles(fileFilter);
			adapter.notifyDataSetChanged();
			file_pathView.setText(currentFile.getAbsolutePath());
		}

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
			Log.d("Comic", "scroll pause");
			int first = view.getFirstVisiblePosition();
			for(int i = 0;i <visibleItemCount;i++)
			{
				if(!files[first + i].isDirectory()&&(files[first + i].getAbsolutePath()
						.endsWith(".jpg")||files[first + i].getAbsolutePath().endsWith(".png")))
				{
					View convertView = view.getChildAt(i);
					BitmapLoader.getInstance().loadImageNoCache(((DirBaseAdapter.ViewHolder)convertView.getTag()).iconView,
							files[first + i].getAbsolutePath(),true);
				}				
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
