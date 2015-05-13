package com.zhr.setting;

import com.zhr.customview.SettingTextView;
import com.zhr.customview.SettingTextView.OnIndexChange;
import com.zhr.customview.SettingTextView.OnStatusChange;
import com.zhr.findcomic.R;
import com.zhr.util.BaseActivity;


import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月7日
 * @description
 */
public class ReadSettingActivity extends BaseActivity implements OnIndexChange,OnStatusChange
		,OnClickListener
{
	private SettingTextView screen_orientation;
	private SettingTextView vertical_read_mode;
	private SettingTextView pageOver_volume;
	private SettingTextView hide_virtual_key;
	private SettingTextView show_time_battery;
	private SettingTextView keep_screen_on;
	
	private TextView titleTextView;
	
	private ImageView back;
	
	private String[] orientations = new String[]{"横屏","竖屏"};
	private String[] read_modes = new String[]{"横向翻页(左右滑动)","竖向翻页(上下滑动)"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_setting);
		
		initView();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		AppSetting.getInstance(getApplicationContext()).commitAllAlter();
	}

	private void initView() {
		screen_orientation = (SettingTextView)findViewById(R.id.screen_orientation);
		screen_orientation.addChooseItems(orientations);
		screen_orientation.setCurrentIndex(AppSetting.getInstance(getApplicationContext())
				.getScreen_orientation());
		screen_orientation.setOnIndexChange(this);
		
		vertical_read_mode = (SettingTextView)findViewById(R.id.vertical_read_mode);
		vertical_read_mode.addChooseItems(read_modes);
		vertical_read_mode.setCurrentIndex(AppSetting.getInstance(getApplicationContext())
				.getMode_in_vertical());
		vertical_read_mode.setOnIndexChange(this);
		
		pageOver_volume = (SettingTextView)findViewById(R.id.pageover_volume);
		pageOver_volume.setToggleStatus(AppSetting.getInstance(getApplicationContext())
				.isPageOver_by_volume());
		pageOver_volume.setOnStatusChange(this);
		
		hide_virtual_key = (SettingTextView)findViewById(R.id.hide_virtual_key);
		hide_virtual_key.setToggleStatus(AppSetting.getInstance(getApplicationContext())
				.isHide_virtual_key());
		hide_virtual_key.setOnStatusChange(this);
		
		show_time_battery = (SettingTextView)findViewById(R.id.show_time_battery);
		show_time_battery.setToggleStatus(AppSetting.getInstance(getApplicationContext())
				.isShow_time_battery());
		show_time_battery.setOnStatusChange(this);
		
		keep_screen_on = (SettingTextView)findViewById(R.id.keep_scrren_on);
		keep_screen_on.setToggleStatus(AppSetting.getInstance(getApplicationContext())
				.isKeep_screen_on());
		keep_screen_on.setOnStatusChange(this);
		
		titleTextView = (TextView)findViewById(R.id.title);
		titleTextView.setText(getResources().getText(R.string.read_setting));
		
		back = (ImageView)findViewById(R.id.back);
		back.setOnClickListener(this);
	}

	public void indexChange(View view, int index) {
		switch (view.getId()) {
		case R.id.screen_orientation:
			AppSetting.getInstance(getApplicationContext()).setScreenOrientation(index);
			break;
		case R.id.vertical_read_mode:
			AppSetting.getInstance(getApplicationContext()).setModeInVertical(index);
			break;
		default:
			break;
		}
		
	}


	public void onToggle(View view, boolean on) {
		switch (view.getId()) {
		case R.id.pageover_volume:
			AppSetting.getInstance(getApplicationContext()).setPageOverByVolume(on);
			if(on)
				Toast.makeText(getApplicationContext(), 
						"音量+:上一页,音量-:下一页", Toast.LENGTH_SHORT).show();
			break;
		case R.id.hide_virtual_key:
			AppSetting.getInstance(getApplicationContext()).setHideVirtualKey(on);
			break;
		case R.id.show_time_battery:
			AppSetting.getInstance(getApplicationContext()).setShowTimeBattery(on);
			break;
		case R.id.keep_scrren_on:
			AppSetting.getInstance(getApplicationContext()).setkeepScreenOn(on);
			break;
		default:
			break;
		}
		
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
			break;

		default:
			break;
		}
	}
	
	
}
