package com.zhr.mainpage;


import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.opensource.yalantis.phoenix.PullToRefreshView;
import com.opensource.yalantis.phoenix.PullToRefreshView.OnRefreshListener;
import com.zhr.customview.LoadMoreListView;
import com.zhr.customview.LoadMoreListView.IRefreshListener;
import com.zhr.database.DBNewsHelper;
import com.zhr.findcomic.R;
import com.zhr.sqlitedao.News;
import com.zhr.util.BitmapLoader;










import com.zhr.util.Constants;
import com.zhr.util.Util;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月24日
 * @description
 */
public class DmzjNewsFragment extends Fragment implements IRefreshListener{
	
	public static final String URL = "http://acg.178.com/";
	
	private PullToRefreshView mPullToRefreshView;
	private LoadMoreListView mListView;
	private NewsAdapter mAdapter;
	private boolean isBusy;
	private int visibleItemCount;
	
	private ArrayList<News> newsItems = new ArrayList<News>();
	
	private String timeRegex = "\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}";
	
	private AsyncHttpClient client;
	private static Handler handler;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("Comic", "onCreateView");
		return inflater.inflate(R.layout.fragment_comic_news, container,false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.d("Comic", "onActivityCreated");
		initView();
		initData();
	}
	
	private void initView()
	{
		mPullToRefreshView = (PullToRefreshView)getView().findViewById(R.id.pull_to_refresh);
		mListView = (LoadMoreListView)getView().findViewById(R.id.news_listview);
		mAdapter = new NewsAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setIRefreshListener(this);
		mPullToRefreshView.setOnRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
//				loadNewsFromInternet();
				mPullToRefreshView.setRefreshing(false);
			}
		});		
		mPullToRefreshView.setRefreshing(true);
	}
	
	//从网络获取数据
	private void initData()
	{
		handler = new Handler();
		client = new AsyncHttpClient();
		loadFromDatabase();
		if(Util.isNetWorkConnect(getActivity()))
			loadNewsFromInternet();
	}
	
	private void loadFromDatabase()
	{
		newsItems = (ArrayList<News>) DBNewsHelper.getInstance(getActivity()).queryNews();
		if(newsItems == null)
			newsItems = new ArrayList<News>();
		mAdapter.notifyDataSetChanged();
	}
	
	private void loadNewsFromInternet()
	{
		client.get(URL, new AsyncHttpResponseHandler() {
			public void onSuccess(int status, Header[] headers, byte[] response) {
				if(status == 200)
				{
					Pattern pattern = Pattern.compile(timeRegex);
					DateFormat dateFormat = new DateFormat();
					Document doc = Jsoup.parse(new String(response));
					Elements elements = doc.select("body > div.bg > div.wrapper.ie6png > div.container > div.left > div.news_box");
					for(Element element:elements)
					{
						News item = new News();
						item.setTag(element.select("div.title > span").text());
						item.setTitle(element.select("div.title > a").attr("title"));
						item.setContentUrl(URL + element.select("div.title > a").attr("href"));
						String time = element.select("div.title_data").text();
						Matcher matcher = pattern.matcher(time);
						try
						{
							if(matcher.find())
							{								
								item.setTime(Util.stringToDate(matcher.group()));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						item.setImagePath(element.select("div.newspic > a > img").attr("src"));
						item.setFrom(Constants.DMZJ);
						newsItems.add(item);
//						DBNewsHelper.getInstance(getActivity()).saveNews(item);
					}
					handler.post(new Runnable() {
						public void run() {
							mPullToRefreshView.setRefreshing(false);
							mAdapter.notifyDataSetChanged();							
						}
					});
				}				
			}
			
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				Toast.makeText(getActivity(), "新闻加载失败", Toast.LENGTH_SHORT).show();
				mPullToRefreshView.setRefreshing(false);
			}
		});
	}
	
	private class NewsAdapter extends BaseAdapter
	{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return newsItems.size();
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
			ViewHolder holder = null;
			if(convertView == null)
			{
				LayoutInflater inflater = getActivity().getLayoutInflater();
				convertView = inflater.inflate(R.layout.news_listview_item_with_image, parent,false);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.tag = (TextView)convertView.findViewById(R.id.tag);
				holder.time = (TextView)convertView.findViewById(R.id.time);
				holder.image = (ImageView)convertView.findViewById(R.id.image);
				convertView.setTag(holder);
			}else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.title.setText(newsItems.get(position).getTitle());
			holder.tag.setText(newsItems.get(position).getTag());
			holder.time.setText(Util.dateToString(newsItems.get(position).getTime()));
			holder.image.setImageDrawable(getResources().getDrawable(R.drawable.loading));
			BitmapLoader.getInstance().loadImage(holder.image, newsItems.get(position).getImagePath(),
					true, false, true);
			return convertView;
		}
		
		class ViewHolder
		{
			public TextView title;
			public TextView tag;
			public TextView time;
			public ImageView image;
		}
		
	}

	@Override
	public void onload() {
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				mListView.loadCompleted();
			}
		}, 1500);
	}

//	@Override
//	public void onScrollStateChanged(AbsListView view, int scrollState) {
//		switch (scrollState) {
//		case OnScrollListener.SCROLL_STATE_IDLE:
//			isBusy = false;
////			int first = view.getFirstVisiblePosition();
////			for(int i = 0;i < visibleItemCount;i++)
////			{
////				View convertView = view.getChildAt(i);
////				BitmapLoader.getInstance().loadImage(((NewsAdapter.ViewHolder)convertView.getTag()).image,
////						newsItems.get(i).getImageUrl(),true);
////				
////			}
//			break;
//		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
//			isBusy = true;
//			break;
//		case OnScrollListener.SCROLL_STATE_FLING:
//			isBusy = true;
//			break;
//		default:
//			break;
//		}
//		
//	}
//
//	@Override
//	public void onScroll(AbsListView view, int firstVisibleItem,
//			int visibleItemCount, int totalItemCount) {
//		this.visibleItemCount = visibleItemCount;
//		
//	}
}
