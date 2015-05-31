package com.zhr.customview;

import com.zhr.findcomic.R;
import com.zhr.util.Util;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ProgressBar;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月27日
 * @description
 */
public class LoadMoreListView extends ListView implements OnScrollListener{
	
	private Context context;
	private View footView;
	private ProgressBar footBar;
	private TextView footTextView;
	private int scrollState;
	private boolean isLoading = false;
	private int lastVisibleItem;
	private int totalItem;
	
	private IRefreshListener listener;
	
	public LoadMoreListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	public LoadMoreListView(Context context) {
		super(context);
		initView(context);
	}
	
	private void initView(Context context)
	{
		this.context = context;
		LayoutInflater inflater = LayoutInflater.from(context);
		footView = inflater.inflate(R.layout.listview_footer_layout, this,false);
		footBar = (ProgressBar) footView.findViewById(R.id.footer_progress);
		footTextView = (TextView)footView.findViewById(R.id.footer_text);
		footView.setVisibility(View.GONE);
		addFooterView(footView);
		this.setOnScrollListener(this);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		this.scrollState = scrollState;
//		Log.d("Comic", "visible" + lastVisibleItem + " total" + totalItem);
		if(lastVisibleItem == totalItem)
		{
			if(!isLoading)
			{
				if(Util.isNetWorkConnect(context))
				{
					footBar.setVisibility(View.VISIBLE);
					footTextView.setText("加载中...");

					if(listener != null)
						listener.onload();
				}
				else {
					footBar.setVisibility(View.GONE);
					footTextView.setText("网络问题，加载失败");
				}
				footView.setVisibility(View.VISIBLE);
				isLoading = true;
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.lastVisibleItem = firstVisibleItem + visibleItemCount;
		this.totalItem = totalItemCount;
	}
	
	public void loadCompleted()
	{
		isLoading = false;
		footBar.setVisibility(View.GONE);
		footTextView.setText("没有更多新闻了");
	}
	
	public void setIRefreshListener(IRefreshListener listener)
	{
		this.listener = listener;
	}
	
	public interface IRefreshListener
	{
		public void onload();
	}

}
