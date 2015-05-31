package com.zhr.mainpage;

import java.text.ParseException;
import java.util.ArrayList;
import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.zhr.database.DBNewsHelper;
import com.zhr.findcomic.R;
import com.zhr.sqlitedao.News;
import com.zhr.util.BitmapLoader;
import com.zhr.util.Constants;
import com.zhr.util.Util;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月30日
 * @description
 */
public class MSiteNewsFragment extends NewsFragment{
	public static final String URL = "http://news.missevan.cn";
	private int index = 1;
	private String real_url = URL;
	private NewsAdapter mAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		timeFormat = "yyyy-MM-dd HH:mm:ss";
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	protected void initView() {		
		super.initView();
		mAdapter = new NewsAdapter();
		mListView.setAdapter(mAdapter);
	}
	
	@Override
	protected void loadFromDatabase() {
		newsItems = (ArrayList<News>) DBNewsHelper.getInstance(getActivity()).queryNews(Constants.MSITE);
		if(newsItems == null)
			newsItems = new ArrayList<News>();
		mAdapter.notifyDataSetChanged();	
	}
	
	protected void loadNewsFromInternet() {
		// TODO Auto-generated method stub
		if(pullToRefresh)
			real_url = URL;
		else{
			real_url = URL + "/news/index?p=" + index;
		}
		client.get(real_url, new AsyncHttpResponseHandler() {
			public void onSuccess(int status, Header[] headers, byte[] response) {
				if(status == 200)
				{
					Document doc = Jsoup.parse(new String(response));
					Elements elements = doc.select("body > div#newsmain > div#left > div.newslist");
					//第一次加载清空目前已经新闻，加载最新新闻
					if(index == 1)
					{
						newsItems.clear();						
					}
					index++;
					int currentItemSize = newsItems.size();
					for(int i = elements.size() - 1;i >= 0;i--)
					{
						Element element = elements.get(i);
						boolean exist = false;
						News item = new News();
						item.setTitle(element.select("div.newstitle > a").text());
						for(News news:newsItems)
						{
							if(news.getTitle().equals(item.getTitle()))
							{
								exist = true;
								break;
							}								
						}
						if(exist)
							continue;					
						item.setContentUrl(URL + element.select("div.newstitle > a").attr("href"));
						item.setSummary(element.select("div.newscontent > p").text());
						String time = element.select("div.newsinfo > div:nth-child(4)").text();
						try 
						{
							item.setTime(Util.stringToDate(time, timeFormat));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						item.setFrom(Constants.MSITE);
						if(pullToRefresh)
							newsItems.add(0, item);
						else
						{
							newsItems.add(currentItemSize,item);
						}
						DBNewsHelper.getInstance(getActivity()).saveNews(item);
					}
					handler.post(new Runnable() {
						public void run() {
							if(pullToRefresh)
								mPullToRefreshView.setRefreshing(false);
							else
								mListView.loadCompleted();
							mAdapter.notifyDataSetChanged();							
						}
					});
				}				
			}
			
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				Toast.makeText(getActivity(), "新闻加载失败", Toast.LENGTH_SHORT).show();
				if(pullToRefresh)
					mPullToRefreshView.setRefreshing(false);
				else
					mListView.loadCompleted();
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
				convertView = inflater.inflate(R.layout.news_listview_item_without_image, parent,false);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.summary = (TextView)convertView.findViewById(R.id.summary);
				holder.time = (TextView)convertView.findViewById(R.id.time);
				convertView.setTag(holder);
			}else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.title.setText(newsItems.get(position).getTitle());
			holder.summary.setText(newsItems.get(position).getSummary());
			holder.time.setText(Util.dateToString(newsItems.get(position).getTime(),timeFormat));
			return convertView;
		}
		
		class ViewHolder
		{
			public TextView title;
			public TextView summary;
			public TextView time;
		}
		
	}

}
