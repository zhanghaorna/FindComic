package com.zhr.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月22日
 * @description
 */
public class NaviLayout extends LinearLayout{


	public NaviLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public NaviLayout(Context context) {
		super(context);
	}
	
	public void setChildOffset(int index,float offset,int turnOrientation)
	{
		if(index >= getChildCount())
			return;
		Log.d("Comic", "index:" + index + " turnOrientation:" + turnOrientation);
		NaviView currentView = (NaviView) getChildAt(index);
		NaviView nextView = null;
		if(turnOrientation == 1)
		{
			nextView = (NaviView)getChildAt(index + 1);
			if(currentView!= null)
				currentView.setOffset(offset, false);
			if(nextView != null)
				nextView.setOffset(offset, true);
		}
		else if(turnOrientation == 0){
			nextView = (NaviView)getChildAt(index - 1);
			if(currentView!= null)
				currentView.setOffset(offset, true);
			if(nextView != null)
				nextView.setOffset(offset, false);
		}
		
	}

}
