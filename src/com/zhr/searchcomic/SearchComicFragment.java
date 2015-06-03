package com.zhr.searchcomic;

import com.umeng.socialize.net.h;
import com.zhr.customview.EditTextWithDel;
import com.zhr.findcomic.R;
import com.zhr.util.BaseActivity;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchComicFragment extends Fragment implements OnClickListener,OnItemClickListener{
	
	private EditTextWithDel editTextWithDel;
	private TextView searchView;
	private GridView mGridView;
	
	private String URL = "http://m.99mh.com/lists/";
	
	private String[] category;
	private int[] category_id = new int[]{R.drawable.mengxi,R.drawable.gaoxiao
			,R.drawable.gedou,R.drawable.kehuan,R.drawable.juqing,R.drawable.zhentan
			,R.drawable.jingji,R.drawable.mofa,R.drawable.shengui,R.drawable.xiaoyuan
			,R.drawable.jingli,R.drawable.chuyi,R.drawable.weiniang,R.drawable.tupian
			,R.drawable.maoxian,R.drawable.xiaoshuo,R.drawable.gangman,R.drawable.danmei
			,R.drawable.jingdian,R.drawable.oumei,R.drawable.riwen};
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.fragment_search_comic, container,false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		initData();
		editTextWithDel = (EditTextWithDel)getView().findViewById(R.id.edit_text);
		searchView = (TextView)getView().findViewById(R.id.search_text);
		searchView.setOnClickListener(this);
		
		mGridView = (GridView)getView().findViewById(R.id.gridview);
		mGridView.setAdapter(new CategoryAdapter());
		mGridView.setOnItemClickListener(this);
		
	}
	
	private void initData()
	{
		category = getResources().getStringArray(R.array.category);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search_text:
			
			break;

		default:
			break;
		}
		
	}
	
	private class CategoryAdapter extends BaseAdapter
	{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return category_id.length;
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
			ViewHolder holder;
			if(convertView == null)
			{
				holder = new ViewHolder();
				LayoutInflater inflater = (LayoutInflater)getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.category_gridview_item, parent,false);
				
				holder.textView = (TextView) convertView.findViewById(R.id.text);
				
				holder.imageView = (ImageView)convertView.findViewById(R.id.image);
				convertView.setTag(holder);
			}
			else
				holder = (ViewHolder) convertView.getTag();
			holder.imageView.setImageDrawable(getResources().getDrawable(category_id[position]));
			holder.textView.setText(category[position]);
			return convertView;
		}
		
		
		class ViewHolder
		{
			ImageView imageView;
			TextView textView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(getActivity(),SearchResultActivity.class);
		intent.putExtra("category", category[position]);
		position++;
		if(position >= 16)
			position += 3;
		intent.putExtra("category_url", URL + position);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
	}
}
