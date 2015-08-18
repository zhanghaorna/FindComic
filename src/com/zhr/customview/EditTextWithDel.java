package com.zhr.customview;

import com.zhr.findcomic.R;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月1日
 * @description
 */
public class EditTextWithDel extends EditText{
	

	private Drawable mRightDrawable;
	
	
	public EditTextWithDel(Context context) {
		super(context);
		initView(context);
	}
	
	public EditTextWithDel(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	private void initView(Context context)
	{
		Drawable[] drawables = getCompoundDrawables();
		//取得right位置的Drawable  
        //即我们在布局文件中设置的android:drawableRight  
		mRightDrawable = drawables[2];
		
		addTextChangedListener(new TextWatchImpl());
		setOnFocusChangeListener(new FocusChangeListenImpl());
		setClearDrawableVisible(false);
	}
	
	 /** 
     * 当手指抬起的位置在clean的图标的区域 
     * 我们将此视为进行清除操作 
     * getWidth():得到控件的宽度 
     * event.getX():抬起时的坐标(改坐标是相对于控件本身而言的) 
     * getTotalPaddingRight():clean的图标左边缘至控件右边缘的距离 
     * getPaddingRight():clean的图标右边缘至控件右边缘的距离 
     * 于是: 
     * getWidth() - getTotalPaddingRight()表示: 
     * 控件左边到clean的图标左边缘的区域 
     * getWidth() - getPaddingRight()表示: 
     * 控件左边到clean的图标右边缘的区域 
     * 所以这两者之间的区域刚好是clean的图标的区域 
     */  
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			boolean isClean = (event.getX() > (getWidth() - getTotalPaddingRight()))&&
							(event.getX() < (getWidth() - getPaddingRight()));
			if(isClean)
				setText("");
			break;

		default:
			break;
		}
		return super.onTouchEvent(event);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		
	}
	
	private class TextWatchImpl implements TextWatcher
	{

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void afterTextChanged(Editable s) {
			if(s.length() > 0)
				setClearDrawableVisible(true);
			else {
				setClearDrawableVisible(false);
			}
		}
		
	}
	
	private class FocusChangeListenImpl implements OnFocusChangeListener
	{

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus)
			{
				boolean isVisible = getText().toString().length() > 0;
				setClearDrawableVisible(isVisible);
			}
			else
				setClearDrawableVisible(false);
			
		}
		
	}
	
	  //隐藏或者显示右边clean的图标  
    protected void setClearDrawableVisible(boolean isVisible) {  
        Drawable rightDrawable;  
        if (isVisible) {  
            rightDrawable = mRightDrawable;  
        } else {  
            rightDrawable = null;  
        }  
        //使用代码设置该控件left, top, right, and bottom处的图标  
        setCompoundDrawables(getCompoundDrawables()[0],getCompoundDrawables()[1],   
                             rightDrawable,getCompoundDrawables()[3]);  
    }   




}
