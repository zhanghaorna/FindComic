package com.zhr.mainpage;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
public class DmzjNewsFragment extends NewsFragment{
	
	public static final String URL = "http://acg.178.com/";
	private int index = 1;
	private String real_url = URL;
	private NewsAdapter mAdapter;
	private String timeRegex = "\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}";
	
	@Override
	protected void initView() {		
		super.initView();
		mAdapter = new NewsAdapter();
		mListView.setAdapter(mAdapter);
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

}
