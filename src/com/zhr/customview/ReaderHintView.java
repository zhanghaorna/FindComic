package com.zhr.customview;

import android.R.integer;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

import com.zhr.findcomic.R;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月13日
 * @description
 */
public class ReaderHintView extends View implements OnTouchListener,OnClickListener{

	private Paint paint = new Paint();
	private PorterDuffXfermode mode; 
	
	public static final boolean LEFT_HAND = false;
	public static final boolean RIGHT_HAND = true;
	
	private int screen_orientation;
	
	private int click_x,click_y;
	private boolean show;
	
	//横屏时显示在上面的提示
	private Path topPath;
	private Path leftPath;
	private Path middlePath;
	private Path rightPath;
	private int space;
	//整个view的rect
	private Rect rect;
	//mode默认为左手模式，左边显示下一页
	private boolean handMode;
	
	private OnTouchClick onTouchClickListener;
	
	public ReaderHintView(Context context,int orientation) {
		super(context);
		mode = new PorterDuffXfermode(Mode.CLEAR);
		leftPath = new Path();
		middlePath = new Path();
		rightPath = new Path();
		topPath = new Path();
		space = 5;
		rect = new Rect();
		rect.left = 0;
		rect.top = 0;
		handMode = false;
		show = false;
		screen_orientation = orientation;
		setBackgroundColor(getResources().getColor(R.color.transparent));
		setOnTouchListener(this);
		setOnClickListener(this);
	}
	
	
	public void showHint()
	{
		show = true;
		invalidate();
	}
	
	public void hideHint()
	{
		show = false;
		invalidate();
	}
	
	public void setOnTouchClickListener(OnTouchClick onTouchClick)
	{
		onTouchClickListener = onTouchClick;
	}
	
	public void setHandMode(boolean mode)
	{
		handMode = mode;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if(!show)
		{	
			rect.bottom = getHeight();
			rect.right = getWidth();
			paint.setColor(getResources().getColor(R.color.transparent));
			canvas.drawRect(rect, paint);
			return;
		}
		//如屏幕为横屏
		if(screen_orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			paint.setColor(getResources().getColor(R.color.tran_green));
			topPath.moveTo(0, 0);
			topPath.lineTo(getWidth(), 0);
			topPath.lineTo(getWidth(), getHeight() * 1/3);
			topPath.lineTo(0, getHeight() * 1/3);
			canvas.drawPath(topPath, paint);
			
			leftPath.moveTo(0, getHeight() * 1/3 + space);
			leftPath.lineTo(getWidth() * 1/3, getHeight() * 1/3 + space);
			leftPath.lineTo(getWidth() * 1/3, getHeight());
			leftPath.lineTo(0, getHeight());
			canvas.drawPath(leftPath, paint);
			
			middlePath.moveTo(getWidth() * 1/3 + space, getHeight() * 1/3 +space);
			middlePath.lineTo(getWidth() * 2/3 - space, getHeight() * 1/3 +space);
			middlePath.lineTo(getWidth() * 2/3 - space, getHeight());
			middlePath.lineTo(getWidth() * 1/3 + space, getHeight());
			canvas.drawPath(middlePath, paint);
			
			rightPath.moveTo(getWidth() * 2/3, getHeight() * 1/3 + space);
			rightPath.lineTo(getWidth(), getHeight() * 1/3 + space);
			rightPath.lineTo(getWidth(), getHeight());
			rightPath.lineTo(getWidth() * 2/3, getHeight());
			canvas.drawPath(rightPath, paint);
			
			paint.setColor(getResources().getColor(R.color.white));
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setTextSize(50);
			canvas.drawText("菜单", getWidth() / 2, getHeight() * 2/3, paint);
			canvas.drawText("上一页", getWidth() / 2, getHeight() / 6, paint);
			canvas.drawText("下一页", getWidth() / 6, getHeight() * 2/3, paint);
			canvas.drawText("下一页", getWidth() * 5/6, getHeight() * 2/3, paint);
			
		}
		else if(screen_orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			paint.setColor(getResources().getColor(R.color.tran_green));
			leftPath.moveTo(0, 0);
			leftPath.lineTo(getWidth() * 2/3 , 0);
			leftPath.lineTo(getWidth() * 2/3 , getHeight() * 1/5);
			leftPath.lineTo(getWidth() * 1/3, getHeight() * 1/5);
			leftPath.lineTo(getWidth() * 1/3, getHeight());
			leftPath.lineTo(0, getHeight());
			canvas.drawPath(leftPath, paint);
			
			middlePath.moveTo(getWidth() * 1/3 + space, getHeight() * 1/5 + space);
			middlePath.lineTo(getWidth() * 1/3 + space, getHeight() * 4/5 - space);
			middlePath.lineTo(getWidth() * 2/3, getHeight() * 4/5 - space);
			middlePath.lineTo(getWidth() * 2/3, getHeight() * 1/5 + space);
			canvas.drawPath(middlePath, paint);
			
			rightPath.moveTo(getWidth() * 2/3 + space, 0);
			rightPath.lineTo(getWidth(), 0);
			rightPath.lineTo(getWidth(), getHeight());
			rightPath.lineTo(getWidth() * 1/3 + space, getHeight());
			rightPath.lineTo(getWidth() * 1/3 + space, getHeight() * 4/5);
			rightPath.lineTo(getWidth() * 2/3 + space, getHeight() * 4/5);
			canvas.drawPath(rightPath, paint);			
			
			paint.setColor(getResources().getColor(R.color.white));
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setTextSize(50);
			canvas.drawText("菜单", getWidth() / 2, getHeight() / 2, paint);
			if(handMode == LEFT_HAND)
			{
				canvas.drawText("下一页", getWidth() / 6, getHeight() / 2, paint);
				canvas.drawText("上一页", getWidth() * 5/6, getHeight() / 2, paint);
			}
			else {
				canvas.drawText("上一页", getWidth() / 6, getHeight() / 2, paint);
				canvas.drawText("下一页", getWidth() * 5/6, getHeight() / 2, paint);
			}
		}
	}
	
	@Override
	public boolean performClick() {
		// TODO Auto-generated method stub
		return super.performClick();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if(show)
		{
			hideHint();
		}		
		return super.dispatchTouchEvent(event);
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			click_x = (int) event.getX();
			click_y = (int) event.getY();
			break;
		case MotionEvent.ACTION_UP:
//			Log.d("Comic", "click_x:" + click_x + " click_y:" + click_y);
//			Log.d("Comic", "X:" + (int)event.getX() + "Y:" + (int)event.getY());
//			Log.d("Comic", "Height:" + getHeight() + " Width:" + getWidth());
			int tap_x = (int)event.getX();
			int tap_y = (int)event.getY();
			if(Math.abs(tap_x - click_x) < 5&&Math.abs(tap_y - click_y) < 10)
			{
				if(onTouchClickListener == null)
					return false;
				if((click_x < getWidth() * 2/3&&click_y < getHeight() * 1/5)||
						(click_x < getWidth() * 1/3&&click_y < getHeight()&&click_y > getHeight() * 1/5))
				{
					if(handMode == LEFT_HAND)
						onTouchClickListener.onNextPageClick();
					else {
						onTouchClickListener.onPrePageClick();
					}
				}
				else if(click_x <  getWidth() * 2/3&&click_x > getWidth() * 1/3
							&&click_y > getHeight() * 1/5&&click_y < getHeight() * 4/5)
				{
					onTouchClickListener.onMenuClick();
				}
				else
				{
					if(handMode == RIGHT_HAND)
						onTouchClickListener.onNextPageClick();
					else {
						onTouchClickListener.onPrePageClick();
					}
				}
				return true;
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}
	
	public interface OnTouchClick
	{
		void onNextPageClick();
		void onPrePageClick();
		void onMenuClick();
	}

	@Override
	public void onClick(View v) {
		
		
	}

}
