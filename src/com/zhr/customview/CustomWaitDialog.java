package com.zhr.customview;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import com.zhr.findcomic.R;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年6月3日
 * @description
 */
public class CustomWaitDialog extends AlertDialog{

	private AlertDialog dialog;
	private TextView waitTextView;
	
	public CustomWaitDialog(Context context) {
		super(context);
		Builder builder = new Builder(context);
		
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.custom_wait_dialog, null);
		waitTextView = (TextView) layout.findViewById(R.id.wait_text);
		dialog = builder.setView(layout).create();
		setCancelable(false);
	}
	
	public boolean isShowing()
	{
		return dialog.isShowing();
	}
	
	public void show()
	{
		if(dialog.isShowing())
			return;
		dialog.show();
	}
	
	public void dismiss()
	{
		if(!dialog.isShowing())
			return;
		dialog.dismiss();
	}
	
	public void setText(String info)
	{
		waitTextView.setText(info);
	}
}
