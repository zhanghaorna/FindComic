package com.zhr.comic;

import java.util.ArrayList;

import com.zhr.customview.ReaderHintView;
import com.zhr.customview.ReaderHintView.OnTouchClick;
import com.zhr.findcomic.R;
import com.zhr.util.BaseActivity;
import com.zhr.util.BitmapLoader;
import com.zhr.util.LoadAndDisplayTask;
import com.zhr.util.Util;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月12日
 * @description
 */
public class ComicReadActivity extends BaseActivity implements OnTouchClick,OnDismissListener{
	private RelativeLayout rootView;
	private RecyclerView mRecyclerView;
	private PictureAdapter mAdapter;
	private LinearLayoutManager mLayoutManager;
	
	private String[] picPaths;
	private int position;
	
	private PopupWindow upWindow;
	private PopupWindow downWindow;
	private boolean dismiss;
	
	private ReaderHintView readerHintView;
	private WindowManager.LayoutParams mClickLayoutParams;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_comic_read);
		
		preData();
		initView();
		initData();
	}
	
	private void preData()
	{
		Intent intent = getIntent();
		picPaths = intent.getStringArrayExtra("picPaths");
		position = intent.getIntExtra("position", 0);
		
		mAdapter = new PictureAdapter();
		mLayoutManager = new LinearLayoutManager(this);
		
		mClickLayoutParams = new WindowManager.LayoutParams(  
				WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT,  
                WindowManager.LayoutParams.TYPE_APPLICATION,  
                WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,  
                PixelFormat.TRANSLUCENT);
	}
	
	private void initView()
	{

		rootView = (RelativeLayout)findViewById(R.id.root);
		
		mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
		mRecyclerView.setLayoutManager(mLayoutManager);
		
		mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
				DividerItemDecoration.VERTICAL_LIST));
		mRecyclerView.setAdapter(mAdapter);
		mLayoutManager.scrollToPosition(position);
		
		readerHintView = new ReaderHintView(this,getResources().getConfiguration().orientation);
		mWindowManager.addView(readerHintView, lp);
		readerHintView.setOnTouchClickListener(this);
		
		View upView = getLayoutInflater().inflate(R.layout.popupwindow_up_in_v,rootView,false);
		upWindow = new PopupWindow(upView,LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT,false);
		upWindow.setOutsideTouchable(true);
		upWindow.setTouchable(true);
		upWindow.setBackgroundDrawable(new BitmapDrawable(getResources()));
		upWindow.setOnDismissListener(this);
		setupPopWindowListener(upWindow.getContentView());
	}
	
	private void initData()
	{
		
	}
	
	private void setupPopWindowListener(View view)
	{
		ImageView back = (ImageView)view.findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		TextView share = (TextView)view.findViewById(R.id.share);
		TextView hint = (TextView)view.findViewById(R.id.hint);
		hint.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				readerHintView.showHint();
				upWindow.dismiss();
			}
		});
	}
	
	private class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.PicViewHolder>
	{
	
		class PicViewHolder extends ViewHolder
		{	
			private ImageView imageView;
			public PicViewHolder(View itemView) {
				super(itemView);
				imageView = (ImageView)itemView.findViewById(R.id.comic_picture);
				imageView.setLayoutParams(new FrameLayout.LayoutParams(
						Util.getScreenWidth(ComicReadActivity.this),
						Util.getScreenHeight(ComicReadActivity.this)));
				
			}
			
		}

		@Override
		public int getItemCount() {
			// TODO Auto-generated method stub
			return picPaths == null ? 0 : picPaths.length;
		}

		@Override
		public void onBindViewHolder(PicViewHolder holder, int poistion) {
			// TODO Auto-generated method stub
			BitmapLoader.getInstance().loadImageNoCache(holder.imageView, 
					picPaths[poistion], false);
			
		}

		@Override
		public PicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			// TODO Auto-generated method stub
			PicViewHolder holder = new PicViewHolder(LayoutInflater.from(ComicReadActivity.this).
					inflate(R.layout.comic_read_recycleview_item, parent,false));
			return holder;
		}
	}
	
		
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		super.dispatchTouchEvent(ev);
		if(readerHintView.dispatchTouchEvent(ev))
		{
			return true;
		}
		return false;
	}

	//绘制ImageView间分割线
	private class DividerItemDecoration extends RecyclerView.ItemDecoration
	{
		private final int[] ATTRS = new int[]{
			android.R.attr.listDivider
		};		
	    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
	    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;
	    private Drawable mDivider;
	    private int mOrientation;
	    
	    public DividerItemDecoration(Context context, int orientation) {
	        final TypedArray a = context.obtainStyledAttributes(ATTRS);
	        mDivider = a.getDrawable(0);
	        a.recycle();
	        setOrientation(orientation);
	    }
	    
	    public void setOrientation(int orientation) {
	        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
	            throw new IllegalArgumentException("invalid orientation");
	        }
	        mOrientation = orientation;
	    }
	    
	    //会在drawChildren之前进行调用，也即RecyclerView绘制子view之前
	    public void onDraw(Canvas c, RecyclerView parent) { 
	        if (mOrientation == VERTICAL_LIST) {
	            drawVertical(c, parent);
	        } else {
	            drawHorizontal(c, parent);
	        }
	 
	    }
	    
	    public void drawVertical(Canvas c, RecyclerView parent) {	    	
	        final int left = parent.getPaddingLeft();
	        final int right = parent.getWidth() - parent.getPaddingRight();
	 
	        final int childCount = parent.getChildCount();
	        for (int i = 0; i < childCount; i++) {
	            final View child = parent.getChildAt(i);
	            android.support.v7.widget.RecyclerView v = new android.support.v7.widget.RecyclerView(parent.getContext());
	            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
	                    .getLayoutParams();
	            final int top = child.getBottom() + params.bottomMargin;
	            final int bottom = top + mDivider.getIntrinsicHeight();
	            mDivider.setBounds(left, top, right, bottom);
	            mDivider.draw(c);
	        }
	    }
	 
	    public void drawHorizontal(Canvas c, RecyclerView parent) {
	        final int top = parent.getPaddingTop();
	        final int bottom = parent.getHeight() - parent.getPaddingBottom();
	 
	        final int childCount = parent.getChildCount();
	        for (int i = 0; i < childCount; i++) {
	            final View child = parent.getChildAt(i);
	            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
	                    .getLayoutParams();
	            final int left = child.getRight() + params.rightMargin;
	            final int right = left + mDivider.getIntrinsicHeight();
	            mDivider.setBounds(left, top, right, bottom);
	            mDivider.draw(c);
	        }
	    }
	    //getItemOffsets 可以通过outRect.set()为每个Item设置一定的偏移量，主要用于绘制Decorator
	    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
	        if (mOrientation == VERTICAL_LIST) {
	            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
	        } else {
	            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
	        }
	    }
	 
	}

	//下一页被点击
	@Override
	public void onNextPageClick() {
		// TODO Auto-generated method stub
		
	}
	
	//上一页被点击
	@Override
	public void onPrePageClick() {
		// TODO Auto-generated method stub
		
	}
	
	//菜单被点击,增加dismiss变量，由于popupwindow比自定义的view先收到Touch事件，所以会出现先消失在显示的问题，加入
	//dismiss变量解决
	@Override
	public void onMenuClick() {
		if(!upWindow.isShowing()&&!dismiss)
		{
			upWindow.showAtLocation(rootView, Gravity.TOP, 0, 0);
		}
		
		if(dismiss)
			dismiss = false;

	}

	@Override
	public void onDismiss() {
		dismiss = true;
		
	}
}
