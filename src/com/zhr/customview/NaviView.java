package com.zhr.customview;

import com.zhr.findcomic.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月22日
 * @description
 */
public class NaviView extends TextView{
	
	private Paint paint;
	//偏移量
	private float offset;
	private boolean left_red_right_white = true;
	private Rect leftRect;
	private Rect rightRect;
	//下面显示红线的个宽度
	private int width;
	
	public NaviView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public NaviView(Context context) {
		super(context);
		init();
	}
	
	private void init()
	{
		paint = new Paint();
		width = 10;
		leftRect = new Rect();

		leftRect.left = 0;

		rightRect = new Rect();

	}
	
	public void setOffset(float offset,boolean left_red_right_white)
	{
		this.offset = offset;
		this.left_red_right_white = left_red_right_white;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if(left_red_right_white)
			paint.setColor(getResources().getColor(R.color.red));
		else
			paint.setColor(getResources().getColor(R.color.transparent));
		leftRect.top = getHeight() - width;
		leftRect.bottom = getWidth();
		leftRect.right = (int) (getWidth() * offset);
		canvas.drawRect(leftRect, paint);
		if(!left_red_right_white)
			paint.setColor(getResources().getColor(R.color.red));
		else
			paint.setColor(getResources().getColor(R.color.transparent));
		rightRect.left = (int)(getWidth() * offset);
		rightRect.top = getHeight() - width;
		rightRect.bottom = getHeight();
		rightRect.right = getWidth();
		canvas.drawRect(rightRect, paint);
	}

}
