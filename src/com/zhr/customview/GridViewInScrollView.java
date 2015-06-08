package com.zhr.customview;

import com.zhr.searchcomic.ComicChapter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月4日
 * @description
 */
public class GridViewInScrollView extends GridView{

	public GridViewInScrollView(Context context) {
		super(context);
	}
	
	public GridViewInScrollView(Context context, AttributeSet attrs) {
		super(context,attrs);
	}
	
	//使GridView不会出现滚动条
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(   
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);  
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
		
}
