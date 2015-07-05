package com.zhr.mainpage;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.R.integer;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

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
public class DmzjNewsFragment extends NewsFragment implements OnItemClickListener
			,OnScrollListener
{
	
	public static final String URL = "http://acg.178.com/";
	private int index = 1;
	private String real_url = URL;
	private NewsAdapter mAdapter;
	private String timeRegex = "\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}";
	
	private boolean isScroll = false;
	private int firstVisibleItem;
	private int visibleItemCount;
	
	@Override
	protected void initView() {		
		super.initView();
		mAdapter = new NewsAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}
	
	protected void loadFromDatabase() {
		newsItems = (ArrayList<News>) DBNewsHelper.getInstance(getActivity()).queryNews(Constants.DMZJ);
		if(newsItems == null)
			newsItems = new ArrayList<News>();
		mAdapter.notifyDataSetChanged();	
		
	}
	
	@Override
	protected void loadNewsFromInternet() {
		// TODO Auto-generated method stub
		if(pullToRefresh)
			real_url = URL;
		else if(index > 1){
			real_url = URL + "index_" + index + ".html";
		}
		client.get(real_url, new AsyncHttpResponseHandler() {
			public void onSuccess(int status, Header[] headers, byte[] response) {
				if(status == 200)
				{
					Pattern pattern = Pattern.compile(timeRegex);
					Document doc = Jsoup.parse(new String(response));
					Elements elements = doc.select("body > div.bg > div.wrapper.ie6png > div.container > div.left > div.news_box");
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
						item.setTitle(element.select("div.title > a").attr("title"));
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
						item.setTag(element.select("div.title > span").text());	
						String contentUrl = URL + element.select("div.title > a").attr("href");
						int lastindex = contentUrl.lastIndexOf(".html");
						if(lastindex != -1)
						{
							contentUrl = contentUrl.substring(0, lastindex) + "_s" + contentUrl.substring(lastindex);
						}
						item.setContentUrl(contentUrl);
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
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(newsItems.get(position).getContentUrl() == null||
				newsItems.get(position).getContentUrl() == "")
			return;
		Intent intent = new Intent(getActivity(),NewsWebviewActivity.class);	
		intent.putExtra("content_url", newsItems.get(position).getContentUrl());
		intent.putExtra("from", Constants.DMZJ);
		startActivity(intent);
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_IDLE:
			isScroll = false;
			int first = view.getFirstVisiblePosition();
			for(int i = 0;i <visibleItemCount;i++)
			{
				View convertView = view.getChildAt(i);
				BitmapLoader.getInstance().loadImage(((NewsAdapter.ViewHolder)convertView.getTag()).image, 
						newsItems.get(first + i).getImagePath(),true, false, true,false);				
			}
			mAdapter.notifyDataSetChanged();
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			isScroll = false;
			break;
		case OnScrollListener.SCROLL_STATE_FLING:
			isScroll = true;
			break;

		default:
			break;
		}
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.visibleItemCount = visibleItemCount;
		
		
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
			holder.time.setText(Util.dateToString(newsItems.get(position).getTime(),timeFormat));
			holder.image.setImageDrawable(getResources().getDrawable(R.drawable.holder_loading));
			if(!isScroll)
				BitmapLoader.getInstance().loadImage(holder.image, newsItems.get(position).getImagePath(),
					true, false, true,false);
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



}
