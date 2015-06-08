package com.zhr.comic;

import java.util.ArrayList;

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.umeng.socialize.net.l;
import com.zhr.customview.GridViewInScrollView;
import com.zhr.customview.NoAutoScrollView;
import com.zhr.customview.TextViewWithExpand;
import com.zhr.findcomic.R;
import com.zhr.searchcomic.ComicChapter;
import com.zhr.util.BaseActivity;
import com.zhr.util.BitmapLoader;
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
	//顶部布局
	private ImageView back;
	private TextView downloadView;
	//简介显示出来显示的进度条
	private ProgressBar progressBar;
	
	private ImageView coverView;
	private TextView authorView;
	private TextView titleView;
	private Button readButton;
	private TextViewWithExpand introView;
	private TextView lastUpdateView;
	
	private String author;
	private String title;
	private String imageUrl;
	private String intro;
	private String introUrl;
	
	//显示详细话数的gridview
	private GridViewInScrollView gridView;
	private ArrayList<ComicChapter> chapters;
	private ChapterAdapter chapterAdapter;
	
	//整个漫画简介页的内容
	private NoAutoScrollView scrollView;
	
	private AsyncHttpClient client;
	
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
		title = getIntent().getStringExtra("title");
		if(title == null)
			title = "";
		imageUrl = getIntent().getStringExtra("imageUrl");
		introUrl = getIntent().getStringExtra("introUrl");
		chapters = new ArrayList<ComicChapter>();
		chapterAdapter = new ChapterAdapter();
	}
	
	private void initView()
	{
		back = (ImageView)findViewById(R.id.back);
		back.setOnClickListener(this);
		downloadView = (TextView)findViewById(R.id.download);
		downloadView.setOnClickListener(this);
		
		coverView = (ImageView)findViewById(R.id.cover);
		authorView = (TextView)findViewById(R.id.author);

		titleView = (TextView)findViewById(R.id.title);
		readButton = (Button)findViewById(R.id.read_button  );
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
		titleView.setText(title);
		if(imageUrl != null)
			BitmapLoader.getInstance().loadImage(coverView, imageUrl, true, false, false);
		readButton.setText("开始阅读");
		
		client = new AsyncHttpClient();
		client.get(introUrl, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				if(arg0 == 200)
				{
					handleIntroPage(arg2);
					chapterAdapter.notifyDataSetChanged();
				}
				progressBar.setVisibility(View.GONE);
				scrollView.setVisibility(View.VISIBLE);				
			}
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				if(!Util.isNetWorkConnect(getApplicationContext()))
					Toast.makeText(getBaseContext(), "网络连接未启用", Toast.LENGTH_SHORT).show();
				progressBar.setVisibility(View.GONE);
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
//保留卷信息，并且将卷和话信息一起显示出来
//				if(element.tagName().equals("br"))
//					break;
				if(element.tagName().equals("br"))
					continue;
				ComicChapter chapter = new ComicChapter();
				chapter.setTitle(element.attr("title"));
				chapter.setUrl(element.attr("href"));
				chapters.add(chapter);
			}
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(this,ComicReadActivity.class);
		intent.putExtra("comicName",title + " " + chapters.get(position).getTitle());
		intent.putExtra("position", 0);
		intent.putExtra("comicUrl", chapters.get(position).getUrl());
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.intro:
			introView.toggle();
			break;

		default:
			break;
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
				convertView = inflater.inflate(R.layout.chapter_textview, parent,false);
				textView = (TextView) convertView.findViewById(R.id.chapter_text);
			}
			else {
				textView = (TextView) convertView;
			}
			textView.setText(chapters.get(position).getTitle());			
			return textView;
		}
		
	}


}
