package com.zhr.comic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Network;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.zhr.customview.GridViewInScrollView;
import com.zhr.customview.NoAutoScrollView;
import com.zhr.customview.TextViewWithExpand;
import com.zhr.database.DBComicDownloadDetailHelper;
import com.zhr.database.DBComicRecordHelper;
import com.zhr.download.DownloadService;
import com.zhr.findcomic.R;
import com.zhr.findcomic.R.id;
import com.zhr.searchcomic.ComicChapter;
import com.zhr.setting.AppSetting;
import com.zhr.sqlitedao.ComicDownloadDetail;
import com.zhr.sqlitedao.ComicRecord;
import com.zhr.util.BaseActivity;
import com.zhr.util.BitmapLoader;
import com.zhr.util.Constants;
import com.zhr.util.Util;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月3日
 * @description
 */
public class ComicIntroActivity extends BaseActivity implements OnClickListener
				,OnItemClickListener
{
	//下载模式和阅读模式
	public static final int READ_MODE = 0;
	public static final int DOWNLOAD_MODE = 1;
	//默认处于阅读模式
	private int mode = READ_MODE;
	
	//顶部布局
	private ImageView back;
	private TextView downloadView;
	//简介显示出来显示的进度条
	private ProgressBar progressBar;
	//漫画相关简介view
	private ImageView coverView;
	private TextView authorView;
	private TextView titleView;
	private Button readButton;
	private TextViewWithExpand introView;
	private TextView lastUpdateView;

	//漫画阅读信息
	private ComicRecord comicRecord;
	//漫画简介
	private String author;
	private String comicName;
	private String imageUrl;
	private String intro;
	private String introUrl;
	//续看按钮的text，点击后,与所有进行比较，决定载入哪个url
	private String continue_chapter = "";
	
	private View networkErrorView;
	
	//显示详细话数的gridview
	private GridViewInScrollView gridView;
	private ArrayList<ComicChapter> chapters;
	//与下载的关联起来，如果点击了下载，就保存入数据库，否则只是一般使用
	private ChapterAdapter chapterAdapter;
	
	//漫画介绍页的布局
	private RelativeLayout introLayout;
	//下载顶部的布局
	private RelativeLayout downloadLayout;
	//下载——全选
	private TextView chooseAllView;
	//下载——确认下载
	private Button confirmDownloadButton;
	//选中下载章节的数量
	private int chooseCount = 0;
	//实际可下载的数量
	private int realCount = 0;
	
	
	private ColorStateList textColorChangeList;
	
	//整个漫画简介页的内容
	private NoAutoScrollView scrollView;
	
	private AsyncHttpClient client;
	
	//接收下载状态变化的广播
	private DownloadBroadcast downloadBroadcast;
	private LocalBroadcastManager lbManager;
	private IntentFilter intentFilter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comic_intro);
		preData();
		initView();
		initData();
	}
	
	private void preData()
	{
		author = getIntent().getStringExtra("author");
		if(author == null)
			author = "";
		comicName = getIntent().getStringExtra("title");
		if(comicName == null)
			comicName = "";
		//搜索数据库找阅读记录
		if(!comicName.equals(""))
		{
			comicRecord = DBComicRecordHelper.getInstance(getBaseContext()).
					getComicRecord(comicName);
			
		}
		imageUrl = getIntent().getStringExtra("imageUrl");
		introUrl = getIntent().getStringExtra("introUrl");
		chapters = new ArrayList<ComicChapter>();
		chapterAdapter = new ChapterAdapter();
		

		textColorChangeList = getResources().getColorStateList(R.drawable.chapter_button_click);
	}
	
	private void initView()
	{
		introLayout = (RelativeLayout) findViewById(R.id.intro_layout);
		
		downloadLayout = (RelativeLayout)findViewById(R.id.download_layout);
		downloadLayout.setVisibility(View.GONE);
		
		chooseAllView = (TextView)findViewById(R.id.choose_all);
		chooseAllView.setOnClickListener(this);
		confirmDownloadButton = (Button)findViewById(R.id.confirm_download);
		confirmDownloadButton.setOnClickListener(this);
		
		back = (ImageView)findViewById(R.id.back);
		back.setOnClickListener(this);
		downloadView = (TextView)findViewById(R.id.download);
		downloadView.setOnClickListener(this);
		
		coverView = (ImageView)findViewById(R.id.cover);
		authorView = (TextView)findViewById(R.id.author);

		titleView = (TextView)findViewById(R.id.title);
		readButton = (Button)findViewById(R.id.read_button);
		readButton.setOnClickListener(this);
		
		introView = (TextViewWithExpand)findViewById(R.id.intro);
		introView.setOnClickListener(this);
		
		
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		scrollView = (NoAutoScrollView)findViewById(R.id.scrollview);
		
		lastUpdateView = (TextView)findViewById(R.id.last_update);
		gridView = (GridViewInScrollView)findViewById(R.id.chapter_num_gridview);
		gridView.setAdapter(chapterAdapter);
		//设置AbsView中点击后面出现的背景为透明(默认为黄色，显得很难看)
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setOnItemClickListener(this);
	}
	
	private void initData()
	{
		authorView.setText(author);
		titleView.setText(comicName);
		if(imageUrl != null)
			BitmapLoader.getInstance().loadImage(coverView, imageUrl, true, false,false);
		readButton.setText("开始阅读");

		
		client = new AsyncHttpClient();
		client.setUserAgent("Baiduspider+");
		client.setTimeout(2000);
		
		downloadBroadcast = new DownloadBroadcast();
		lbManager = LocalBroadcastManager.getInstance(this);
		intentFilter = new IntentFilter();
		intentFilter.addAction(DownloadService.CHAPTER_FINISHED);
		
		loadComicIntro();
		lbManager.registerReceiver(downloadBroadcast, intentFilter);
		
	}
	
	private void loadComicIntro()
	{
		client.get(introUrl, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				if(arg0 == 200)
				{
					handleIntroPage(arg2);
					
					
					if(chapters.size() == 0)
						showNetError();
					else
					{
						List<ComicDownloadDetail> cDetails = DBComicDownloadDetailHelper
								.getInstance(getBaseContext()).getComicDownloadDetails(comicName);
						realCount = chapters.size();
						if(cDetails != null&&cDetails.size() != 0)
						{							
							for(int i = 0;i < cDetails.size();i++)
							{
								String chapterName = cDetails.get(i).getChapter();
								for(ComicChapter chapter:chapters)
								{
									if(chapter.getChapter().equals(chapterName))
									{
										realCount--;
										chapter.setDownload_status(cDetails.get(i).getStatus());
										break;
									}
								}
							}
						}
						chapterAdapter.notifyDataSetChanged();
					}
				}
				progressBar.setVisibility(View.GONE);
				scrollView.setVisibility(View.VISIBLE);				
			}
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				if(!Util.isNetWorkConnect(getApplicationContext()))
					Toast.makeText(getBaseContext(), "网络连接未启用", Toast.LENGTH_SHORT).show();
				progressBar.setVisibility(View.GONE);
				showNetError();
			}	
		});
	}
	
	private void handleIntroPage(byte[] response)
	{
		Document doc = Jsoup.parse(new String(response));
		Element body = doc.body();
		intro = body.select("div#detail_block > div > p").text();
		introView.setText(intro);
		lastUpdateView.setText(body.select("div.main > div > div.pic > div > p:nth-child(6)").text());
		Elements elements = body.select("p#sort_div_p");
		if(elements.size() == 1)
		{
			for(Element element:elements.get(0).children())
			{
//              保留卷信息，并且将卷和话信息一起显示出来
//				if(element.tagName().equals("br"))
//					break;
				if(element.tagName().equals("br"))
					continue;
				ComicChapter chapter = new ComicChapter();
				chapter.setChapter(element.attr("title"));
				chapter.setUrl(element.attr("href"));
				chapters.add(chapter);
			}
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(mode == READ_MODE)
		{
			Intent intent = new Intent(this,ComicReadActivity.class);
			intent.putExtra("comicName",comicName);
			intent.putExtra("chapterName", chapters.get(position).getChapter());
			intent.putExtra("comicUrl", chapters.get(position).getUrl());
			startActivity(intent);
		}
		else if(mode == DOWNLOAD_MODE)
		{
			if(chapters.get(position).getDownload_status() != -1)
				return;
			chapters.get(position).changeChoose();
			if(chapters.get(position).getChoose())
				++chooseCount;
			else
				--chooseCount;
			if(chooseCount > 0)
				confirmDownloadButton.setEnabled(true);
			else
				confirmDownloadButton.setEnabled(false);
			if(chooseCount == realCount&&chooseCount > 0)
				chooseAllView.setText("取消");
			else 
				chooseAllView.setText("全选");
			chapterAdapter.notifyDataSetInvalidated();
		}
	}
	
	
	private void showNetError()
	{
		if(networkErrorView != null)
		{
			networkErrorView.setVisibility(View.VISIBLE);
			return;
		}
		ViewStub stub = (ViewStub) findViewById(R.id.network_error);
		networkErrorView = stub.inflate();
		Button re_get = (Button)networkErrorView.findViewById(R.id.re_get);
		re_get.setOnClickListener(this);
	}
	
	private void hideNetError()
	{
		if(networkErrorView != null)
			networkErrorView.setVisibility(View.GONE);
	}
	
	private void changeMode()
	{
		if(mode == DOWNLOAD_MODE)
		{
			downloadLayout.setVisibility(View.GONE);
			introLayout.setVisibility(View.VISIBLE);
			introView.setVisibility(View.VISIBLE);
			downloadView.setVisibility(View.VISIBLE);
			mode = READ_MODE;
			chapterAdapter.notifyDataSetInvalidated();		
		}
		else if(mode == READ_MODE)
		{
			mode = DOWNLOAD_MODE;
			downloadView.setVisibility(View.INVISIBLE);
			introLayout.setVisibility(View.GONE);
			introView.setVisibility(View.GONE);
			downloadLayout.setVisibility(View.VISIBLE);
			chapterAdapter.notifyDataSetInvalidated();
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		back.performClick();
	}
	
	@Override
	protected void onResume() {		
		super.onResume();
		if(comicRecord != null)
		{
			continue_chapter = comicRecord.getChapter();
			chapterAdapter.notifyDataSetInvalidated();
			readButton.setText("续看 " + comicRecord.getChapter());
			readButton.setBackgroundColor(getResources().getColor(R.color.green));
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		lbManager.unregisterReceiver(downloadBroadcast);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.intro:
			introView.toggle();
			break;
		case R.id.back:
			if(mode == DOWNLOAD_MODE)
			{
				changeMode();
			}
			else
			{
				finish();
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
			}
			break;
		//点击开始阅读或续看按钮
		case R.id.read_button:
			int position = 0;
			Log.d("Comic","chapter" + continue_chapter);
			if(!continue_chapter.equals(""))
			{
				for(int i = 0;i < chapters.size();i++)
				{
					if(chapters.get(i).getChapter().equals(continue_chapter))
					{
						position = i;
						break;
					}
				}
			}
			Log.d("Comic", "p" + position);
			Intent intent = new Intent(this,ComicReadActivity.class);
			intent.putExtra("comicName",comicName);
			intent.putExtra("chapterName", chapters.get(position).getChapter());
			intent.putExtra("comicUrl", chapters.get(position).getUrl());
			startActivity(intent);
			break;
		case R.id.re_get:
			loadComicIntro();
			hideNetError();
			progressBar.setVisibility(View.VISIBLE);
			break;
		case R.id.download:
			//将chapter中所有被选中下载的标记清空
			for(int i = 0;i < chapters.size();i++)
				chapters.get(i).setChoose(false);
			chooseCount = 0;
			changeMode();
			confirmDownloadButton.setEnabled(false);
			break;
		case R.id.choose_all:
			if(chooseCount < realCount)
			{
				chooseCount = 0;
				for(int i = 0;i < chapters.size();i++)
				{
					if(chapters.get(i).getDownload_status() == -1)
					{
						chapters.get(i).setChoose(true);
						++chooseCount;
					}
				}
				if(chooseCount > 0)
				{
					chooseAllView.setText("取消");
					confirmDownloadButton.setEnabled(true);
				}
			}
			else if(chooseCount == realCount)
			{
				for(int i = 0;i < chapters.size();i++)
					chapters.get(i).setChoose(false);
				chooseCount = 0;
				chooseAllView.setText("全选");
				confirmDownloadButton.setEnabled(false);
			}
			chapterAdapter.notifyDataSetInvalidated();
			break;
		//下载按钮点击后响应事件
		case R.id.confirm_download:
			Intent downloadIntent = new Intent(ComicIntroActivity.this
					,DownloadService.class);
			String[] chapterNames = new String[chooseCount];
			String[] urls = new String[chooseCount];
			int index = 0;
			for(int i = chapters.size() - 1;i >= 0;i--)
			{
				if(chapters.get(i).getChoose()&&chapters.get(i).getDownload_status() == -1)
				{
					chapters.get(i).setDownload_status(Constants.WAITING);
					chapterNames[index] = chapters.get(i).getChapter();
					urls[index] = chapters.get(i).getUrl();
					++index;
				}
			}
			
			//下载图片缩略图进行保存
			downloadThumb();
			
			Bundle data = new Bundle();
			data.putString("comicName", comicName);
			data.putStringArray("chapters", chapterNames);
			data.putStringArray("urls", urls);
			data.putInt("downloadChapterNum", chooseCount);
			downloadIntent.putExtras(data);
			startService(downloadIntent);
			changeMode();
			Toast.makeText(getBaseContext(), "添加下载任务成功", Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}		
	}
	
	private void downloadThumb()
	{
		final File file = new File(AppSetting.getInstance(ComicIntroActivity.this).getDownloadPath()
				+ File.separator + comicName + File.separator + comicName + ".jpg");
		if(!file.exists())
		{
			client.get(imageUrl, new AsyncHttpResponseHandler() {
				
				@Override
				public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
					if(arg0 == 200)
					{
						try 
						{
							FileOutputStream fStream = new FileOutputStream(file);												
							fStream.write(arg2);
							fStream.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}				
				}
				
				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					if(Util.isNetWorkConnect(getBaseContext()))
						downloadThumb();
				}
			});
		}
		
	}
	
	//接收下载完成广播，更新显示
	private class DownloadBroadcast extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent != null)
			{
				if(intent.getAction().equals(DownloadService.CHAPTER_FINISHED))
				{
					if(intent.getStringExtra("comicName") != null
							&&intent.getStringExtra("comicName").equals(comicName))
					{
						String chapterName = intent.getStringExtra("chapterName");
						if(chapterName == null)
							return;
						int status = intent.getIntExtra("status", 0);
						for(int i = 0;i < chapters.size();i++)
						{
							if(chapters.get(i).getChapter().equals(chapterName))
							{
								realCount--;
								chapters.get(i).setDownload_status(status);
								chapterAdapter.notifyDataSetChanged();
								break;
							}
						}
					}
				}
			}
			
		}
		
	}
	
	private class ChapterAdapter extends BaseAdapter
	{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return chapters.size();
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
			TextView textView = null;
			if(convertView == null)
			{
				LayoutInflater inflater = (LayoutInflater)getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.chapter_button, parent,false);
				textView = (TextView) convertView.findViewById(R.id.chapter_button);
			}
			else {
				textView = (TextView) convertView;				
			}

			textView.setText(chapters.get(position).getChapter());	
			if(mode == READ_MODE)
			{						
//				button.setEnabled(true);
				textView.setTextColor(textColorChangeList);
				if(comicRecord != null&&chapters.get(position).getChapter().
						equals(comicRecord.getChapter()))
				{
					textView.setTextColor(getResources().getColor(R.color.white));
					textView.setBackgroundResource(R.drawable.chapter_button_read);		
				}
				else if(chapters.get(position).getDownload_status() == Constants.FINISHED)
				{
					textView.setTextColor(getResources().getColor(R.color.white));
					textView.setBackgroundResource(R.drawable.chapter_button_downloaded);
				}
				else 
				{
					textView.setBackgroundResource(R.drawable.chapter_button_default);	
				}			
			}
			else if(mode == DOWNLOAD_MODE)
			{
				textView.setTextColor(getResources().getColor(R.color.black));
				if(chapters.get(position).getDownload_status() != -1)
				{
					textView.setBackgroundResource(R.drawable.chapter_button_background_unable_choose);
				}
				else
				{
					if(chapters.get(position).getChoose())
					{
						textView.setBackgroundResource(R.drawable.chapter_button_background_choose);
					}
					else
					{
						textView.setBackgroundResource(R.drawable.chapter_button_background_no_choose);
					}
				}

			}

			return textView;
		}
		
	}


}
