package com.zhr.customview;



import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opensource.zcw.togglebutton.ToggleButton;
import com.opensource.zcw.togglebutton.ToggleButton.OnToggleChanged;
import com.zhr.findcomic.R;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月8日
 * @description 
 */
public class SettingTextView extends RelativeLayout implements OnClickListener{
	//显示在左边的view
	private TextView mainTextView;
	//显示在右边的view
	private TextView subTextView;
	//显示在右边的toggle
	private ToggleButton toggleButton;
	
	private int show_mode;
	private String mainText;
	private String subText;
	
	private String[] chooseText = null;
	private int current_index;
	
	private OnIndexChange onIndexChangeListener;
	private OnStatusChange onStatusChangeListener;
	private Context context;
	
	private void init(Context context)
	{
		this.context = context;
		if(show_mode == 0)
		{
			LayoutInflater.from(context).inflate(R.layout.custom_setting_textview, this);
			mainTextView = (TextView)findViewById(R.id.main_text);
			subTextView = (TextView)findViewById(R.id.sub_text);
			mainTextView.setText(mainText);
			subTextView.setText(subText);
			toggleButton = null;
		}
		else {
			LayoutInflater.from(context).inflate(R.layout.custom_setting_toggle, this);
			mainTextView = (TextView)findViewById(R.id.main_text);
			toggleButton = (ToggleButton)findViewById(R.id.setting_toggle);
			mainTextView.setText(mainText);
			toggleButton.setOnToggleChanged(new OnToggleChanged() {
				
				public void onToggle(boolean on) {
					if(onStatusChangeListener != null)
						onStatusChangeListener.onToggle(SettingTextView.this,toggleButton.getToggleStatus());
				}
			});
			subTextView = null;
		}
		
		this.setOnClickListener(this);
		
	}
	
	public SettingTextView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		getAttrs(context, attrs);
		init(context);
	}
	
	public SettingTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getAttrs(context, attrs);
		init(context);
	}
	
	public SettingTextView(Context context) {
		super(context);
		init(context);
	}
	
	private void getAttrs(Context context,AttributeSet attrs)
	{
		TypedArray tArray = context.obtainStyledAttributes(attrs, R.styleable.setting_view);
		show_mode = tArray.getInteger(R.styleable.setting_view_view_mode, 0);
		mainText = tArray.getString(R.styleable.setting_view_mainText);
		if(mainText == null)
			mainText = "";
		if(show_mode == 0)
		{
			subText = tArray.getString(R.styleable.setting_view_subText);
			if(subText == null)
				subText = "";
		}
			
		tArray.recycle();
	}
	
	
	public void addChooseItems(String[] items)
	{
		chooseText = items;
	}
	
	public void setCurrentIndex(int index)
	{
		if(show_mode == 0&&index < chooseText.length)
		{
			this.current_index = index;
			subTextView.setText(chooseText[index]);
		}
	}
	
	public void setToggleStatus(boolean status)
	{
		if(show_mode == 1)
		{
			if(status)
				toggleButton.toggleOn();
			else {
				toggleButton.toggleOff();
			}
		}
	}
	
	public void setMainText(String text)
	{
		mainTextView.setText(text);
	}
	
	public void setOnIndexChange(OnIndexChange l)
	{
		if(show_mode == 0)
			onIndexChangeListener = l;
	}
	
	public void setOnStatusChange(OnStatusChange l)
	{
		if(show_mode == 1)
			onStatusChangeListener = l;
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(show_mode == 0)
		{
			if(chooseText != null&&chooseText.length > 0)
			{
				Log.d("TAG", "run");
				new AlertDialog.Builder(context).setTitle(mainText)
					.setSingleChoiceItems(chooseText,current_index,
							new DialogInterface.OnClickListener() {
								
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									current_index = which;
									subTextView.setText(chooseText[which]);
									if(onIndexChangeListener != null)
										onIndexChangeListener.indexChange(SettingTextView.this,which);
									dialog.dismiss();
								}
							}).show();
			}
		}
		else
		{
			if(toggleButton != null)
				toggleButton.toggle();
		}
	}
	
	public interface OnIndexChange
	{
		void indexChange(View view,int index);
	}
	
	public interface OnStatusChange
	{
		void onToggle(View view,boolean on);
	}

}
