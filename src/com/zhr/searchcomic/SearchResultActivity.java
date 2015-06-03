package com.zhr.searchcomic;

import java.util.ArrayList;

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
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
	
	private GridView searchGridView;
	private SearchAdapter mSearchAdapter;
	
	private String url;
	private AsyncHttpClient client;
	//漫画简介数据
	private ArrayList<ComicIntro> comicIntros;
	//当前第几页
	private int page = 1;
	//是否正在加载
	private boolean isLoading = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_result);
		initView();
		initData();
		
	}
	
	private void initView()
	{
		back = (ImageView)findViewById(R.id.back);
		back.setOnClickListener(this);
		titleView = (TextView)findViewById(R.id.title);
		
		searchGridView = (GridView)findViewById(R.id.search_gridview);
		searchGridView.setOnScrollListener(this);
		searchGridView.setOnItemClickListener(this);
		
		dialog = new CustomWaitDialog(this);
		dialog.show();
	}
	
	private void initData()
	{
		String title = getIntent().getStringExtra("category");
		if(title == null)
			title = "漫画";
		titleView.setText(title);
		url = getIntent().getStringExtra("category_url");
		if(url == null)
			url = "";
		
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
		client.get(url + "/" + page, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				if(arg0 == 200)
				{
					if(filterComicIntro(arg2))
					{
						page++;
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
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
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
			viewHolder.coverView.setImageDrawable(getResources()
					.getDrawable(R.drawable.holder_loading));
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
