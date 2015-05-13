package com.zhr.findcomic;

import com.zhr.mianpage.MainPageFragment;
import com.zhr.recommend.RecommendFragment;
import com.zhr.searchcomic.SearchComicFragment;
import com.zhr.setting.SettingFragment;
import com.zhr.util.BaseFragmentActivity;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;
/**
 * 
 * @author zhr
 * @version 1.0.0
 * @description 
 */
public class MainActivity extends BaseFragmentActivity implements OnClickListener{
	private TextView homePageTextView;
	private TextView recommendTextView;
	private TextView searchTextView;
	private TextView moreTextView;
	

	private Fragment[] fragments;
	private int current_fragment = 0;
	private FragmentManager fragmentManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initData();
		

	}
	
	private Fragment getCurrentFragment(int index)
	{
		if(fragments[index] != null)
			return fragments[index];
		switch (index) {
		case 0:
			fragments[index] = new MainPageFragment();
			break;
		case 1:
			fragments[index] = new RecommendFragment();
			break;
		case 2:
			fragments[index] = new SearchComicFragment();
			break;
		case 3:
			fragments[index] = new SettingFragment();
			break;
		default:
			break;
		}
		return fragments[index];
	}
	
	private void initData()
	{
		fragmentManager = getSupportFragmentManager();
		fragments = new Fragment[4];
		fragments[current_fragment] = getCurrentFragment(current_fragment);
	
		if(fragments[current_fragment] != null)
		{
			fragmentManager.beginTransaction().add(R.id.main_container,fragments[current_fragment]).commit();
		}
		
	}
	
	private void replaceFragment(int index)
	{
		fragments[index] = getCurrentFragment(index);
		if(fragments[index] != null)
		{
			fragmentManager.beginTransaction().replace(R.id.main_container,fragments[index]).commit();
		}
	}
	
	private void initView()
	{
		homePageTextView = (TextView)findViewById(R.id.main_homepage);
		recommendTextView = (TextView)findViewById(R.id.main_recommmend);
		searchTextView = (TextView)findViewById(R.id.main_search);
		moreTextView = (TextView)findViewById(R.id.main_more);
		homePageTextView.setOnClickListener(this);
		recommendTextView.setOnClickListener(this);
		searchTextView.setOnClickListener(this);
		moreTextView.setOnClickListener(this);
		
		homePageTextView.setSelected(true);
	}
	
	private void cancelSelectState()
	{
		homePageTextView.setSelected(false);
		recommendTextView.setSelected(false);
		searchTextView.setSelected(false);
		moreTextView.setSelected(false);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.main_homepage:
			cancelSelectState();
			homePageTextView.setSelected(true);
			if(current_fragment == 0)
				return;
			current_fragment = 0;
			replaceFragment(current_fragment);
			break;
		case R.id.main_recommmend:
			cancelSelectState();
			recommendTextView.setSelected(true);
			if(current_fragment == 1)
				return ;
			current_fragment = 1;
			replaceFragment(current_fragment);
			break;
		case R.id.main_search:
			cancelSelectState();
			searchTextView.setSelected(true);
			if(current_fragment == 2)
				return;
			current_fragment = 2;
			replaceFragment(current_fragment);
			break;
		case R.id.main_more:
			cancelSelectState();
			moreTextView.setSelected(true);
			if(current_fragment == 3)
				return ;
			current_fragment = 3;
			replaceFragment(current_fragment);
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
}
