package com.zhr.customview;

import com.umeng.socialize.controller.impl.InitializeController;
import com.zhr.findcomic.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月4日
 * @description
 */
public class TextViewWithExpand extends RelativeLayout{

	private TextView introView;
	private ImageView expandView;
	private boolean isExpand;
	
	public TextViewWithExpand(Context context) {
		super(context);
		init(context);
	}
	
	public TextViewWithExpand(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.textview_with_expand, this,false);
		introView = (TextView)view.findViewById(R.id.intro);
		expandView = (ImageView)view.findViewById(R.id.expand);
		isExpand = false;
		addView(view);
	}
	
	public void setText(String content)
	{
		introView.setText(content);
	}
	
	public void toggle()
	{
		if(isExpand)
		{
			introView.setMaxLines(2);
			expandView.setImageDrawable(getResources().getDrawable(R.drawable.detail_intr_expand));
		}
		else
		{
			introView.setLines(0);
			introView.setMaxLines(Integer.MAX_VALUE);
			expandView.setImageDrawable(getResources().getDrawable(R.drawable.detail_intr_close));
		}
		isExpand = !isExpand;
	}

}
