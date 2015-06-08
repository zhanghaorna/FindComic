package com.zhr.customview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月4日
 * @description
 */
public class NoAutoScrollView extends ScrollView{

	public NoAutoScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public NoAutoScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
		// TODO Auto-generated method stub
		return 0;
	}
}
