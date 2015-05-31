package com.zhr.mainpage;

import java.util.ArrayList;

import com.loopj.android.http.AsyncHttpClient;
import com.opensource.yalantis.phoenix.PullToRefreshView;
import com.opensource.yalantis.phoenix.PullToRefreshView.OnRefreshListener;
import com.zhr.customview.LoadMoreListView;
import com.zhr.customview.LoadMoreListView.IRefreshListener;
import com.zhr.findcomic.R;
import com.zhr.sqlitedao.News;
import com.zhr.util.BitmapLoader;
import com.zhr.util.Util;
import com.zhr.util.WeakFragmentHandler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月30日
 * @description
 */
public class NewsFragment extends Fragment implements IRefreshListener{

	protected PullToRefreshView mPullToRefreshView;
	protected LoadMoreListView mListView;
	
	
	protected boolean pullToRefresh = true;
	protected static Handler handler;
	protected AsyncHttpClient client;
	
	protected String timeFormat = "yyyy-MM-dd HH:mm";
	
	protected ArrayList<News> newsItems = new ArrayList<News>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.fragment_comic_news, container,false);
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		initView();
		initData();
	}
	
	protected void initView()
	{
		mPullToRefreshView = (PullToRefreshView)getView().findViewById(R.id.pull_to_refresh);
		mListView = (LoadMoreListView)getView().findViewById(R.id.news_listview);
		mListView.setIRefreshListener(this);
		mPullToRefreshView.setOnRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				pullToRefresh = true;
				loadNewsFromInternet();
			}
		});		
	}
	
	private void initData()
	{
		handler = new NewsHandler(this);
		client = new AsyncHttpClient();
		client.addHeader("User-Agent", "Googlebot/2.1");
		loadFromDatabase();
		if(Util.isNetWorkConnect(getActivity()))
		{
			mPullToRefreshView.setRefreshing(true);
			loadNewsFromInternet();
		}
	}
	
	protected void loadNewsFromInternet()
	{
		
	}
	
	protected void loadFromDatabase()
	{
		
	}
	
	public void onload() {
		pullToRefresh = false;
		loadNewsFromInternet();		
	}
	
	private static class NewsHandler extends WeakFragmentHandler<Fragment>
	{
		public NewsHandler(Fragment fragment) {
			super(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
	}
	


}
