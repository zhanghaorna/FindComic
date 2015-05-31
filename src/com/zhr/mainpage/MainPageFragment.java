package com.zhr.mainpage;

import java.util.ArrayList;
import java.util.NavigableMap;

import com.zhr.comic.ComicReadActivity;
import com.zhr.customview.NaviLayout;
import com.zhr.customview.NaviView;
import com.zhr.findcomic.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView.FindListener;

public class MainPageFragment extends Fragment implements OnClickListener,
			OnPageChangeListener
{
	
	private ViewPager viewPager;
	private NaviLayout naviLayout;
	private NaviView[] naviViews;
	//viewpager是滑动方向,0表示往左滑，1表示往右滑
	private int turnOrientation = -1;
	//viewpager是否在滑动
	private boolean isFlip;
	//记录viewpager目前选中的Item
	private int currentIndex = 0;
	


	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.fragment_mainpage, container,false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		initView();
		
	}
	
	private void initView()
	{
		
		
		
		viewPager = (ViewPager)getView().findViewById(R.id.viewpager);
		viewPager.setAdapter(new NewsPagerAdapter(getChildFragmentManager()));
		viewPager.setOnPageChangeListener(this);

//		viewPager.setCurrentItem(0);
		
		naviLayout = (NaviLayout)getView().findViewById(R.id.naviLayout);
		
		naviViews = new NaviView[2];
		naviViews[0] = (NaviView)getView().findViewById(R.id.dmzj);
		naviViews[1] = (NaviView)getView().findViewById(R.id.msite);
		naviViews[0].setSelected(true);
		naviViews[0].setOffset(1,true);
		for(int i = 0;i < naviViews.length;i++)
			naviViews[i].setOnClickListener(this);

	}
	
	//显示动漫新闻的ListView的Adapter,isViewFromObject要返回true才有view显示出来
	private class NewsPagerAdapter extends FragmentPagerAdapter
	{
		private Fragment[] newsFragments;
		
		public NewsPagerAdapter(FragmentManager fm) {
			super(fm);
			newsFragments = new Fragment[2];
		}

		@Override
		public int getCount() {
			return newsFragments.length;
		}

		public Fragment getItem(int arg0) {
			switch (arg0) {
			case 0:
				if(newsFragments[arg0] == null)
					newsFragments[arg0] = new DmzjNewsFragment();
				break;
			case 1:
				if(newsFragments[arg0] == null)
					newsFragments[arg0] = new MSiteNewsFragment();
				break;
			default:
				break;
			}
			return newsFragments[arg0];
		}
	}

	//当arg0为1时表示用户拖动viewpager进行滑动，arg0为2表示调用setCurrentItem()viewpager滑动
	public void onPageScrollStateChanged(int arg0) {
//		Log.d("Comic", "onPageScrollStateChanged:" + arg0);
		if(arg0 == 1||arg0 == 2)
			isFlip = true;
		else if(arg0 == 0)
		{
			isFlip = false;
			turnOrientation = -1;
			currentIndex = viewPager.getCurrentItem();
		}

	}

	//此处currentIndex要与turnOrientation同步更新，为什么不直接viewPager.getCurrentItem
	//由于viewpager载入下一页时，滑动动画并没有滚完。
	//arg0永远为1不知道为什么
	public void onPageScrolled(int arg0, float arg1, int arg2) {
//		Log.d("Comic", "onPageScrolled:" + arg0 + " arg1:" + arg1 + " arg2:" + arg2);
		if(isFlip&&arg2 != 0)
		{
			if(turnOrientation == -1)
			{
				if(arg1 > 0.5)
					turnOrientation = 0;
				else 
					turnOrientation = 1;
			}
//			Log.d("Comic", "index:" + currentIndex + " turn" + turnOrientation);
			naviLayout.setChildOffset(currentIndex, arg1, turnOrientation);
		}
	}

	@Override
	public void onPageSelected(int position) {
		cancelAllSelect();
		naviViews[position].setSelected(true);

	}
	
	private void cancelAllSelect()
	{
		for(int i = 0;i < naviViews.length;i++)
			naviViews[i].setSelected(false);;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dmzj:
			if(viewPager.getCurrentItem() == 0)
				return;
			cancelAllSelect();
			naviViews[0].setSelected(true);
			viewPager.setCurrentItem(0);
			break;
		case R.id.msite:
			if(viewPager.getCurrentItem() == 1)
				return;
			cancelAllSelect();
			naviViews[1].setSelected(true);
			viewPager.setCurrentItem(1);
		default:
			break;
		}
		
	}
}
