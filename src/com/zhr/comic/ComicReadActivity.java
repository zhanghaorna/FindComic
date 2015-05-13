package com.zhr.comic;

import java.util.ArrayList;

import com.zhr.findcomic.R;
import com.zhr.util.BaseActivity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月12日
 * @description
 */
public class ComicReadActivity extends BaseActivity{
	private RecyclerView mRecyclerView;
	private PictureAdapter mAdapter;
	
	private int[] mDatas = new int[]{R.drawable.one,R.drawable.two,R.drawable.three};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comic_read);
		
		initView();
		initData();
	}
	
	private void initView()
	{
		mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mAdapter = new PictureAdapter();
		mRecyclerView.setAdapter(mAdapter);
	}
	
	private void initData()
	{
		
	}
	
	private class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.PicViewHolder>
	{
	
		class PicViewHolder extends ViewHolder
		{	
			private ImageView imageView;
			public PicViewHolder(View itemView) {
				super(itemView);
				imageView = (ImageView)itemView.findViewById(R.id.comic_picture);
			}
			
		}

		@Override
		public int getItemCount() {
			// TODO Auto-generated method stub
			return mDatas.length;
		}

		@Override
		public void onBindViewHolder(PicViewHolder holder, int poistion) {
			// TODO Auto-generated method stub
			holder.imageView.setImageResource(mDatas[poistion]);
		}

		@Override
		public PicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			// TODO Auto-generated method stub
			PicViewHolder holder = new PicViewHolder(LayoutInflater.from(ComicReadActivity.this).
					inflate(R.layout.comic_read_recycleview_item, parent,false));
			return holder;
		}
	}

}
