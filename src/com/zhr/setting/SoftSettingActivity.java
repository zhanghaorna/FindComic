package com.zhr.setting;

import com.zhr.customview.SettingTextView;
import com.zhr.customview.SettingTextView.OnIndexChange;
import com.zhr.customview.SettingTextView.OnStatusChange;
import com.zhr.findcomic.R;
import com.zhr.util.BaseActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月9日
 * @description
 */
public class SoftSettingActivity extends BaseActivity implements OnClickListener,OnIndexChange
		,OnStatusChange
{
	private ImageView back;
	private TextView title;
	
	private SettingTextView read_setting;
	private SettingTextView notify_setting;
	private SettingTextView night_mode;
	private SettingTextView alert_download_dir;
	private SettingTextView clear_cache;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_soft_setting);
		
		initView();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		AppSetting.getInstance(getApplicationContext()).commitAllAlter();
	}


	private void initView() {
		back = (ImageView)findViewById(R.id.back);
		back.setOnClickListener(this);
		
		title = (TextView)findViewById(R.id.title);
		title.setText(getResources().getText(R.string.setting));
		
		read_setting = (SettingTextView)findViewById(R.id.read_setting);
		read_setting.setOnClickListener(this);
		
		notify_setting = (SettingTextView)findViewById(R.id.notify_setting);
		notify_setting.setOnClickListener(this);
		
		night_mode = (SettingTextView)findViewById(R.id.night_mode);
		night_mode.setToggleStatus(AppSetting.getInstance(getApplicationContext())
				.isNight_mode());
		night_mode.setOnStatusChange(this);
		
		alert_download_dir = (SettingTextView)findViewById(R.id.alert_download_dir);
		
		clear_cache = (SettingTextView)findViewById(R.id.clear_cache);
		
	}
	
	


	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
			break;
		case R.id.read_setting:
			Intent intent = new Intent(this,ReadSettingActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
		case R.id.notify_setting:
			break;
		default:
			break;
		}
		
	}


	public void onToggle(View view, boolean on) {
		switch (view.getId()) {
		case R.id.night_mode:
			AppSetting.getInstance(getApplicationContext()).setNightMode(on);
			
			if(on)
			{
				Toast.makeText(this, "夜间模式已开启", Toast.LENGTH_SHORT).show();
			}
			changeNightMode();
			break;

		default:
			break;
		}
		
	}


	public void indexChange(View view, int index) {
		// TODO Auto-generated method stub
		
	}
}
