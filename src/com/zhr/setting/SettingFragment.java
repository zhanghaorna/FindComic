package com.zhr.setting;

import com.zhr.comic.ComicReadActivity;
import com.zhr.findcomic.R;
import com.zhr.util.LocalDirActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SettingFragment extends Fragment implements OnClickListener{
	private TextView softSettingTextView;
	private TextView localComicTextView;
	private TextView checkUpdateTextView;
	private TextView aboutFindComicTextView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.fragment_setting, container,false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		initView();
	}
	
	private void initView()
	{
		softSettingTextView = (TextView)getView().findViewById(R.id.soft_setting);
		localComicTextView = (TextView)getView().findViewById(R.id.local_comic);
		checkUpdateTextView = (TextView)getView().findViewById(R.id.check_update);
		aboutFindComicTextView = (TextView)getView().findViewById(R.id.about_find_comic);
		softSettingTextView.setOnClickListener(this);
		localComicTextView.setOnClickListener(this);
		checkUpdateTextView.setOnClickListener(this);
		aboutFindComicTextView.setOnClickListener(this);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = null;
		switch (v.getId()) {
		case R.id.soft_setting:
			intent = new Intent(SettingFragment.this.getActivity(),SoftSettingActivity.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
			break;
		case R.id.local_comic:
			intent = new Intent(SettingFragment.this.getActivity(),LocalDirActivity.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
			break;
		case R.id.check_update:
			intent = new Intent(SettingFragment.this.getActivity(),ComicReadActivity.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
			break;
		case R.id.about_find_comic:
			break;

		default:
			break;
		}
	}
}
