package com.zhr.download;

import java.util.List;

import com.zhr.database.DBComicDownloadDetailHelper;
import com.zhr.findcomic.R;
import com.zhr.sqlitedao.ComicDownloadDetail;
import com.zhr.util.Constants;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ComicManageActivity extends Activity implements OnClickListener{
	
	private final int UNFINISH_MODE = 0;
	private final int FINISH_MODE = 1;
	private final int EDIT_MODE = 2;
	private int mode = 0;
	
	private TextView leftView;
	private TextView middleView;
	private TextView rightView;
	private View divideView;
	
	private String comicName;
	private List<ComicDownloadDetail> cDetails;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comic_manage);
		initView();
		initData();
	}
	
	private void initView()
	{
		leftView = (TextView)findViewById(R.id.left);
		leftView.setOnClickListener(this);
		middleView = (TextView)findViewById(R.id.middle);
		middleView.setOnClickListener(this);
		rightView = (TextView)findViewById(R.id.right);
		rightView.setOnClickListener(this);
		divideView = (View)findViewById(R.id.divider_left);
	}
	
	private void initData()
	{
		Intent intent = getIntent();
		if(intent.getStringExtra("comicName") != null)
		{
			comicName = intent.getStringExtra("comicName");
			cDetails = DBComicDownloadDetailHelper.getInstance(getApplicationContext())
					.getComicDownloadDetails(comicName);
			mode = FINISH_MODE;
			for(int i = 0;i < cDetails.size();i++)
			{
				if(cDetails.get(i).getStatus() != Constants.FINISHED)
				{
					mode = UNFINISH_MODE;
					break;
				}
			}
			setModeView();
		}
	}
	
	
	
	private void setModeView()
	{
		if(mode == FINISH_MODE)
		{
			leftView.setText("目录");
			rightView.setText("编辑");
			middleView.setVisibility(View.GONE);
			divideView.setVisibility(View.GONE);
		}
		else if(mode == UNFINISH_MODE)
		{
			middleView.setVisibility(View.VISIBLE);
			divideView.setVisibility(View.VISIBLE);
			leftView.setText("目录");
			middleView.setText("全部开始");
			rightView.setText("编辑");
		}
		else if(mode == EDIT_MODE)
		{
			middleView.setVisibility(View.VISIBLE);
			divideView.setVisibility(View.VISIBLE);
			leftView.setText("删除");
			leftView.setEnabled(false);
			middleView.setText("全选");
			rightView.setText("完成");
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
	private class ComicDownloadDetailAdapter extends BaseAdapter
	{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return cDetails.size() + 1;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position	;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int type = getItemViewType(position);
			if(convertView == null)
			{
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				switch (type) {
				case 1:
					
					break;

				default:
					break;
				}
			}
			
			return null;
		}
		
		@Override
		public int getViewTypeCount() {
			// TODO Auto-generated method stub
			return 2;
		}
		
		@Override
		public int getItemViewType(int position) {
			// TODO Auto-generated method stub
			if(position == 0)
				return 1;
			else 
				return 2;
		}
		
	}
}
