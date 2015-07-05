package com.zhr.setting;

import com.zhr.comic.ComicReadActivity;
import com.zhr.customview.CustomWaitDialog;
import com.zhr.download.UpdateService;
import com.zhr.findcomic.R;
import com.zhr.util.LocalDirActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class SettingFragment extends Fragment implements OnClickListener,
		UpdateVersion.OnUpdateListener
{
	private TextView softSettingTextView;
	private TextView localComicTextView;
	private TextView checkUpdateTextView;
	private TextView aboutFindComicTextView;
	
	private CustomWaitDialog waitDialog;
	private UpdateVersion updateVersion;
	
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
		
		waitDialog = new CustomWaitDialog(getActivity());
		updateVersion = new UpdateVersion(getActivity());
		updateVersion.setOnUpdateListener(this);
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
			waitDialog.setText("查询服务器。。。");
			waitDialog.show();
			updateVersion.checkUpdate();			
			break;
		case R.id.about_find_comic:
			break;

		default:
			break;
		}
	}
	

	@Override
	public void onWithUpdate(String version,String url) {
		waitDialog.dismiss();
//		Log.d("Comic", "version" + version + " url" + url);
		final String download_url = url;
//		Log.d("Comic", url);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
					.setTitle("目前最新版本为" + version)
					.setMessage("是否更新版本")
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.setPositiveButton("更新", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							Intent intent = new Intent(getActivity(),UpdateService.class);
							intent.putExtra("apk_url", download_url);
							getActivity().startService(intent);
						}
					});
		builder.show();
	}

	@Override
	public void onWithOutUpdate() {
		Toast.makeText(getActivity(), "已是最新版本，无需更新", Toast.LENGTH_SHORT).show();
		waitDialog.dismiss();
	}
}
