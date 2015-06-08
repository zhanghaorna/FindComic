package com.zhr.searchcomic;

import java.util.ArrayList;

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.zhr.comic.ComicIntroActivity;
import com.zhr.customview.CustomWaitDialog;
import com.zhr.findcomic.R;
import com.zhr.util.BaseActivity;
import com.zhr.util.BitmapLoader;
import com.zhr.util.Util;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月2日
 * @description
 */
public class SearchResultActivity extends BaseActivity implements OnClickListener
					,OnScrollListener,OnItemClickListener
{
	
	private ImageView back;
	private TextView titleView;
	//等待对话框
	private CustomWaitDialog dialog;
	//搜索结果
	private TextView searchView;
	
	private GridView searchGridView;
	private SearchAdapter mSearchAdapter;
	//传过来的url
	private String url;
	//实际请求的url
	private String read_url;
	private AsyncHttpClient client;
	//漫画简介数据
	private ArrayList<ComicIntro> comicIntros;
	//当前第几页
	private int page = 1;
	//是否正在加载
	private boolean isLoading = true;
	//是否是搜索漫画
	private boolean search = false;
	//顶部标题
	private String title;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_result);
		preData();
		initView();
		initData();
		
	}
	
	private void preData()
	{
		title = getIntent().getStringExtra("category");
		if(title == null)
			title = "漫画";

		url = getIntent().getStringExtra("category_url");
		if(url == null)
			url = "";
		search = getIntent().getBooleanExtra("search", false);
	}
	
	private void initView()
	{
		back = (ImageView)findViewById(R.id.back);
		back.setOnClickListener(this);
		titleView = (TextView)findViewById(R.id.title);
		titleView.setText(title);
		searchGridView = (GridView)findViewById(R.id.search_gridview);
		searchView = (TextView)findViewById(R.id.search_result);
		searchGridView.setOnItemClickListener(this);
		if(!search)
			searchGridView.setOnScrollListener(this);
		else {
			searchView.setVisibility(View.VISIBLE);
		}
		
		
		dialog = new CustomWaitDialog(this);
		dialog.show();
	}
	
	private void initData()
	{		
		client = new AsyncHttpClient();
		comicIntros = new ArrayList<ComicIntro>();
		mSearchAdapter = new SearchAdapter();
		searchGridView.setAdapter(mSearchAdapter);
		isLoading = false;
		loadFromInternet();
	}
	
	private void loadFromInternet()
	{
		if(!dialog.isShowing())
			dialog.show();
		isLoading = true;
		if(search)
		{
			read_url = url + title;
		}
		else
		{
			read_url = url + "/" + page;
		}
		client.get(read_url, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				if(arg0 == 200)
				{
					if(filterComicIntro(arg2))
					{
						if(!search)
							page++;
						else {
							searchView.setText("已为你找到" + comicIntros.size() +
									"部相关漫画");
						}
						mSearchAdapter.notifyDataSetChanged();
					}
				}
				isLoading = false;
				if(dialog.isShowing())
					dialog.dismiss();
			}
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				if(!Util.isNetWorkConnect(getApplicationContext()))
					Toast.makeText(SearchResultActivity.this,"网络未连接",
							Toast.LENGTH_SHORT).show();
				isLoading = false;
				if(dialog.isShowing())
					dialog.dismiss();
			}
		});
	}
	
	
	private boolean filterComicIntro(byte[] response)
	{
		Document doc = Jsoup.parse(new String(response));
		Elements elements = doc.select("body > div.main > ul.se-list > li.clearfix");
		if(elements == null||elements.size() == 0)
			return false;
		for(Element element:elements)
		{
			ComicIntro comicIntro = new ComicIntro();
			comicIntro.setImageUrl(element.select("a.pic > img").attr("src"));
			comicIntro.setTitle(element.select("a.pic > div.con > h3").text());
			comicIntro.setAuthor(element.select("a.pic > div.con > p:nth-child(2)").text());
			comicIntro.setIntroUrl(element.select("a.pic").attr("href"));
			String update = element.select("a.tool > span.h").text();
			if(update.equals("[完结]"))
			{
				comicIntro.setFinished(true);
				comicIntro.setUpdate(update);
			}
			else
			{
				comicIntro.setFinished(false);
				comicIntro.setUpdate("更新到" + update + "话");
			}
			comicIntros.add(comicIntro);
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
			break;

		default:
			break;
		}
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mSearchAdapter = null;
		searchGridView = null;
		if(dialog.isShowing())
			dialog.dismiss();
		dialog = null;
		
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if(firstVisibleItem + visibleItemCount == totalItemCount&&!isLoading)
		{
			loadFromInternet();
		}		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(this,ComicIntroActivity.class);
		intent.putExtra("title", comicIntros.get(position).getTitle());
		intent.putExtra("author", comicIntros.get(position).getAuthor());
		intent.putExtra("imageUrl", comicIntros.get(position).getImageUrl());
		intent.putExtra("introUrl", comicIntros.get(position).getIntroUrl());
		startActivity(intent);
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
	}
	
	private class SearchAdapter extends BaseAdapter
	{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return comicIntros.size();
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
			ViewHolder viewHolder;
			Log.d("Comic", "" + position);
			if(convertView == null)
			{
				LayoutInflater inflater = (LayoutInflater)getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.comic_search_gridview_item,
						parent,false);
				viewHolder = new ViewHolder();
				viewHolder.coverView = (ImageView) convertView.findViewById(R.id.cover_image);
				viewHolder.titleView = (TextView) convertView.findViewById(R.id.comic_title);
				viewHolder.authorView = (TextView)convertView.findViewById(R.id.comic_author);
				viewHolder.updateView = (TextView)convertView.findViewById(R.id.comic_update);
				convertView.setTag(viewHolder);
			}
			else
				viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.coverView.setImageDrawable(getResources()
					.getDrawable(R.drawable.holder_loading));
			viewHolder.titleView.setText(comicIntros.get(position).getTitle());
			viewHolder.authorView.setText(comicIntros.get(position).getAuthor());
			if(comicIntros.get(position).isFinished())
			{
				viewHolder.updateView.setTextColor(getResources().getColor(R.color.red));
				viewHolder.updateView.setText(comicIntros.get(position).getUpdate());
			}
			else
			{
				viewHolder.updateView.setTextColor(getResources().getColor(R.color.light_black));
				viewHolder.updateView.setText(comicIntros.get(position).getUpdate());
			}

			BitmapLoader.getInstance().loadImage(viewHolder.coverView,
					comicIntros.get(position).getImageUrl()	, true, false, false);
			
			return convertView;
		}
		
		class ViewHolder
		{
			ImageView coverView;
			TextView titleView;
			TextView authorView;
			TextView updateView;
		}		
	} 
}
