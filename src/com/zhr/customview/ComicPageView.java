package com.zhr.customview;

import com.zhr.findcomic.R;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月18日
 * @description
 */
public class ComicPageView extends ImageView{
	
	private ShowDrawable show = null;
	
	public void setPageNum(int num)
	{
		if(show == null)
		{
			show = new ShowDrawable();
		}
		show.setNum(num);
		setImageDrawable(show);
	}

	public ComicPageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	public ComicPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ComicPageView(Context context) {
		super(context);
	}
	
	
	public void setImageBitmap(Bitmap bm) {
		
		super.setImageBitmap(bm);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
	}
	
	
	
	private class ShowDrawable extends Drawable
	{
		private Paint mPaint;
		private Rect mRect;
		private int position;
		
		public ShowDrawable()
		{
			mPaint = new Paint();
			mRect = new Rect();
			mRect.top = 0;
			mRect.left = 0;			
		}
		
		public void setNum(int position)
		{
			this.position = position;
		}
		
		public void draw(Canvas canvas) {
			mPaint.setColor(getResources().getColor(R.color.black));
			mRect.right = getWidth();
			mRect.bottom = getHeight();
			canvas.drawRect(mRect, mPaint);
			mPaint.setColor(getResources().getColor(R.color.white));
			mPaint.setTextAlign(Paint.Align.CENTER);
			mPaint.setTextSize(100);
			canvas.drawText(String.valueOf(position), getWidth() / 2, getHeight() / 5, mPaint);
		}

		@Override
		public void setAlpha(int alpha) {
			mPaint.setAlpha(alpha);			
		}

		public void setColorFilter(ColorFilter cf) {
			mPaint.setColorFilter(cf);
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}
		
	}

}
