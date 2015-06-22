package com.zhr.customview;

import java.util.Timer;
import java.util.TimerTask;

import com.zhr.findcomic.R;

import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月18日
 * @description
 */
public class WaitProgressBar extends View{
	
	private Paint mPaint;
	private Point[] start;
	private Point[] end;
	private int[] colors = new int[]{Color.GRAY,Color.RED,Color.GREEN,Color.BLUE
										,Color.YELLOW};
	//旋转的角度
	private double rotate_angle = 10;
	//进度条的数量
	private int num;
	//是否需要重绘
	private boolean reDraw;
	//计时绘制
	private Timer timer;
	//目前绘制前几块方块
	private int current_draw;
	//目前的颜色索引
	private int color_index;
	private int pre_color_index;
	//目前加载进度
	private int rate = 0;
	//字体属性
	private FontMetrics fontMetrics;
	
	public WaitProgressBar(Context context) {
		super(context);
		initView();
	}
	
	public WaitProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	private void initView()
	{
		ViewGroup.LayoutParams params = getLayoutParams();
		if(params == null)
		{
			params = new ViewGroup.LayoutParams(100, 100);					
		}
		params.width = 100;
		params.height = 100;
		setLayoutParams(params);
		
		mPaint = new Paint();
		mPaint.setColor(colors[0]);
		mPaint.setStyle(Paint.Style.STROKE);
		num = (int) (360 / rotate_angle);
		rotate_angle = rotate_angle * Math.PI / 180;
		reDraw = false;
		
		timer = new Timer();
		timer.schedule(new ColorTask(), 0, 100);
		current_draw = 0;
		color_index = 1;
		pre_color_index = 0;
		
		
	}
	
	private void getCoordinate()
	{
		if(reDraw||start == null||end == null)
		{
			reDraw = false;
			start = new Point[num];
			end = new Point[num];
			for(int i = 0;i < num;i++)
			{
				start[i] = new Point();
				end[i] = new Point();
			}
			
			int radius = Math.min(getWidth(), getHeight());
			float short_radius = (float) (radius * 0.5 * 0.4);
			float long_radius = (float) (radius * 0.5 * 0.8);
			start[0].x = 0;
			start[0].y = (int) short_radius;
			end[0].x = 0;
			end[0].y = (int) long_radius;
			for(int i = 1;i < num;i++)
			{
				start[i].x = (int) (start[0].x * Math.cos(-rotate_angle * i) - start[0].y *
						Math.sin(-rotate_angle * i));
				start[i].y = (int) (start[0].x * Math.sin(-rotate_angle * i) + start[0].y *
						Math.cos(-rotate_angle * i));
				end[i].x = (int) (end[0].x * Math.cos(-rotate_angle * i) - end[0].y *
						Math.sin(-rotate_angle * i));
				end[i].y = (int) (end[0].x * Math.sin(-rotate_angle * i) + end[0].y *
						Math.cos(-rotate_angle * i));
			}
			
//			for(int i = 1;i < num;i++)
//			{
//				start[i].x = (int) (start[i - 1].x * Math.cos(rotate_angle) - start[i - 1].y *
//						Math.sin(rotate_angle));
//				start[i].y = (int) (start[i - 1].x * Math.sin(rotate_angle) + start[i - 1].y *
//						Math.cos(rotate_angle));
//				end[i].x = (int) (end[i - 1].x * Math.cos(rotate_angle) - end[i - 1].y *
//						Math.sin(rotate_angle));
//				end[i].y = (int) (end[i - 1].x * Math.sin(rotate_angle) + end[i - 1].y *
//						Math.cos(rotate_angle));
//			}
			for(int i = 0;i < num;i++)
			{
				start[i].x += getWidth() / 2;
				end[i].x += getWidth() / 2;			
				start[i].y = getHeight() / 2 - start[i].y;
				end[i].y = getHeight() / 2 - end[i].y;
			}
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		reDraw = true;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		getCoordinate();
		mPaint.setStrokeWidth(2);
		mPaint.setColor(colors[color_index]);
		for(int i = 0;i <= current_draw;i++)
		{
			canvas.drawLine(start[i].x, start[i].y, 
					end[i].x, end[i].y, mPaint);
		}
		mPaint.setColor(colors[pre_color_index]);
		for(int i = current_draw;i < num;i++)
		{
			canvas.drawLine(start[i].x, start[i].y, 
					end[i].x, end[i].y, mPaint);
		}
		mPaint.setColor(Color.BLACK);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setStrokeWidth(1);
		mPaint.setTextSize(35);	
		fontMetrics = mPaint.getFontMetrics();
		canvas.drawText(rate + "%", getWidth() / 2, getHeight() / 2
				+ (fontMetrics.descent- fontMetrics.ascent) / 2 - fontMetrics.bottom, mPaint);		
	}
	
	public void setProgress(int progress)
	{
		rate = progress;
		invalidate();
	}
	
	public void cancelTask()
	{
		timer.cancel();
		timer = null;
	}
	
	private class ColorTask extends TimerTask
	{
		public void run() 
		{
			current_draw++;
			if(current_draw >= num)
			{
				current_draw = 0;
				pre_color_index = color_index;
				color_index++;
				if(color_index >= colors.length)
				{
					color_index = 0;
				}
				
			}
			postInvalidate();
		}		
	}

}
