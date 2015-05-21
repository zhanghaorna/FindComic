package com.zhr.comic;


import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.zhr.customview.ComicPageView;
import com.zhr.customview.ReaderHintView;
import com.zhr.customview.ReaderHintView.OnTouchClick;
import com.zhr.findcomic.R;
import com.zhr.setting.AppSetting;
import com.zhr.setting.ReadSettingActivity;
import com.zhr.util.BaseActivity;
import com.zhr.util.BitmapLoader;
import com.zhr.util.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
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
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月12日
 * @description
 */
public class ComicReadActivity extends BaseActivity implements OnTouchClick				
{
		
	private RelativeLayout rootView = null;
	private RecyclerView mRecyclerView;
	//RecyclerView是否在滑动，滑动就不加载图片
	private boolean isScroll;
	
	private PictureAdapter mAdapter;
	private LinearLayoutManager mLayoutManager;
	
	private String[] picPaths;
	private int filePosition;
	private int viewPosition;
	
	private PopWindowHolder mPopWindowHolder;
	private boolean dismiss;
	
	private ReaderHintView readerHintView;

	
	//开源服务
	//友盟分享
	private UMSocialService mController;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if(AppSetting.getInstance(getApplicationContext()).getScreen_orientation() ==
				AppSetting.HORIZONTAL_ORIENTATION)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		setContentView(R.layout.activity_comic_read);

		preData();
		initView();
		initData();
		
	}
	
	private void preData()
	{
		Intent intent = getIntent();
		picPaths = intent.getStringArrayExtra("picPaths");
		if(picPaths == null||picPaths.length == 0)
			picPaths = new String[]{""};
		filePosition = intent.getIntExtra("position", 0);
		
		viewPosition = filePosition;
		
		mAdapter = new PictureAdapter();
		if(AppSetting.getInstance(getApplicationContext()).getPage_turn_orientation() == 
				AppSetting.MODE_IN_VERTICAL_UP_DOWN)
		{
			mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		}
		else {
			mLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
		}
		
		if(AppSetting.getInstance(getApplicationContext()).getScreen_orientation() ==
				AppSetting.HORIZONTAL_ORIENTATION)
			mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		
		
		mPopWindowHolder = new PopWindowHolder();
	}
	
	private void initView()
	{

		rootView = (RelativeLayout)findViewById(R.id.root);
		
		mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
		mRecyclerView.setLayoutManager(mLayoutManager);
		
		mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
				DividerItemDecoration.VERTICAL_LIST));
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.addOnScrollListener(new ComicScrollListener());
		mLayoutManager.scrollToPosition(filePosition);
		
		readerHintView = new ReaderHintView(this,getResources().getConfiguration().orientation);
		
		readerHintView.setOnTouchClickListener(this);
	
	}
	
	private void initData()
	{
		mController = UMServiceFactory.getUMSocialService("com.umeng.share");
		//配置友盟分享相关设置
		mController.getConfig().setSsoHandler(new SinaSsoHandler());
		
		
	}
	
	
	private class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.PicViewHolder>
	{
	
		class PicViewHolder extends ViewHolder
		{	
			private ComicPageView imageView;
			public PicViewHolder(View itemView) {
				super(itemView);
				imageView = (ComicPageView)itemView.findViewById(R.id.comic_picture);
				imageView.setLayoutParams(new FrameLayout.LayoutParams(
						Util.getScreenWidth(ComicReadActivity.this),
						Util.getScreenHeight(ComicReadActivity.this)));				
			}			
		}

		public int getItemCount() {
			return picPaths == null ? 0 : picPaths.length;
		}

		public void onBindViewHolder(PicViewHolder holder, int poistion) {
			holder.imageView.setPageNum(poistion + 1);
			if(!isScroll)
				BitmapLoader.getInstance().loadImage(holder.imageView,
						picPaths[poistion], true, false, false);
			viewPosition = poistion;
			Log.d("Comic", "" + poistion);

		}

		@Override
		public PicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			PicViewHolder holder = new PicViewHolder(LayoutInflater.from(ComicReadActivity.this).
					inflate(R.layout.comic_read_recycleview_item, parent,false));
			return holder;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("Comic", "onResume");
		mAdapter.notifyItemChanged(viewPosition);
		
		if(readerHintView != null)
			mWindowManager.addView(readerHintView, lp);
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d("Comic", "onPause");
		if(readerHintView != null)
		{
			//不能使用removeview(异步的方法)，activity已被销毁了，但view还没有被remove掉
			//导致activity leaked问题，使用removeViewImmediate(同步)
			mWindowManager.removeViewImmediate(readerHintView);
		}
		if(mPopWindowHolder.isShowing())
		{
			mPopWindowHolder.dismiss(); 
		}

		AppSetting.getInstance(getApplicationContext()).commitAllAlter();
	}
	
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mController.dismissShareBoard();
		Log.d("Comic", "onDestory");		
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
	
	//将所有popupwindow写在一起，方便管理
	private class PopWindowHolder implements OnDismissListener
	{
		private PopupWindow topWindow;
		private PopupWindow bottomWindow;
		
		private TextView page_hint;
		private SeekBar pageSeekBar;
		
		public PopWindowHolder()
		{
			View topView = getLayoutInflater().inflate(R.layout.popupwindow_top,rootView,false);
			topWindow = new PopupWindow(topView,LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT,false);
			topWindow.setTouchable(true);
			topWindow.setOnDismissListener(this);
			topWindow.setAnimationStyle(R.style.popupwindow_top_anim);
			setupTopWindowListener(topWindow.getContentView());
			View bottomView = null;
			if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
				bottomView = getLayoutInflater().inflate(R.layout.popupwindow_bottom_in_v,rootView,false);
			else {
				bottomView = getLayoutInflater().inflate(R.layout.popupwindow_bottom_in_h,rootView,false);
			}
			bottomWindow = new PopupWindow(bottomView,LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT,false);
			bottomWindow.setTouchable(true);
			bottomWindow.setAnimationStyle(R.style.popupwindow_bottom_anim);
			setupBottomWindowListener(bottomWindow.getContentView());
		}
		//设置topWindow监听
		private void setupTopWindowListener(View view)
		{
			ImageView back = (ImageView)view.findViewById(R.id.back);
			back.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dismiss();
					finish();
				}
			});
			
			TextView share = (TextView)view.findViewById(R.id.share);
			share.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					mController.getConfig().setPlatforms(SHARE_MEDIA.SINA);
					mController.setShareMedia(new UMImage(ComicReadActivity.this, picPaths[viewPosition]));
					mController.openShare(ComicReadActivity.this, false);
				}
			});
			
			TextView hint = (TextView)view.findViewById(R.id.hint);
			hint.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					readerHintView.showHint();
					dismiss();
				}
			});
		}
		//设置bottomWindow监听
		private void setupBottomWindowListener(View view)
		{
			page_hint = (TextView)view.findViewById(R.id.page_hint);
			page_hint.setText((filePosition + 1) + "/" + picPaths.length);
			
			pageSeekBar = (SeekBar)view.findViewById(R.id.page_seekbar);
			pageSeekBar.setMax(picPaths.length - 1);
			pageSeekBar.setProgress(filePosition);
			pageSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				
				public void onStopTrackingTouch(SeekBar seekBar) {
					
				}
				
				public void onStartTrackingTouch(SeekBar seekBar) {
					
				}
				
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					page_hint.setText((progress  + 1) + "/" + picPaths.length);
					mLayoutManager.scrollToPosition(progress);
				}
			});
			//屏幕阅读方向
			TextView turn_screen_orientation = (TextView)view.findViewById(R.id.landscape);
			set_screen_orientation(turn_screen_orientation);
			turn_screen_orientation.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dismiss();
					AppSetting.getInstance(getApplicationContext()).changeScreenOrientation();
					if(AppSetting.getInstance(getApplicationContext()).getScreen_orientation() ==
								AppSetting.VERTICAL_ORIENTATION)
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					else {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					}
				}
			});
			
			//设置
			TextView setting = (TextView)view.findViewById(R.id.setting);
			setting.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(ComicReadActivity.this,ReadSettingActivity.class);
					startActivity(intent);
					overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
				}
			});
			if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
				return;
			//翻页方向
			TextView page_turn_orientation = (TextView)view.findViewById(R.id.page_turn_orientation);
			set_page_orientation(page_turn_orientation);
			page_turn_orientation.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Toast.makeText(getBaseContext(), "当前切换为" + ((TextView)v).getText(), 
							Toast.LENGTH_SHORT).show();
					AppSetting.getInstance(getApplicationContext()).changePage_turn_orientation();
					set_page_orientation((TextView)v);
					mLayoutManager.setOrientation(AppSetting.getInstance(getApplicationContext())
							.getPage_turn_orientation());
					mAdapter.notifyDataSetChanged();
					
				}
			});
			//左右手翻页
			TextView page_turn_hand = (TextView)view.findViewById(R.id.page_turn_hand);
			set_page_hand(page_turn_hand);
			page_turn_hand.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Toast.makeText(getBaseContext(), "当前切换为" + ((TextView)v).getText(),
							Toast.LENGTH_SHORT).show();
					AppSetting.getInstance(getApplicationContext()).changePage_turn_hand();
					set_page_hand((TextView) v);
					readerHintView.setHandMode(AppSetting.getInstance(getApplicationContext())
							.getPage_turn_hand());
					dismiss();
					readerHintView.showHint();
				}
			});

			
		}
		//设置横屏or竖屏阅读
		private void set_screen_orientation(TextView textView)
		{
			if(AppSetting.getInstance(getApplicationContext()).getScreen_orientation() == 
					AppSetting.VERTICAL_ORIENTATION)
			{
				textView.setText(AppSetting.screen_orientations[AppSetting.HORIZONTAL_ORIENTATION]);
				textView.setCompoundDrawablesWithIntrinsicBounds(null,
						getResources().getDrawable(AppSetting.screen_orientations_src[AppSetting.HORIZONTAL_ORIENTATION]), null, null);
			}
			else
			{
				textView.setText(AppSetting.screen_orientations[AppSetting.VERTICAL_ORIENTATION]);
				textView.setCompoundDrawablesWithIntrinsicBounds(null,
						getResources().getDrawable(AppSetting.screen_orientations_src[AppSetting.VERTICAL_ORIENTATION]), null, null);
			}
		}
		//popupwindow设置翻页方向
		private void set_page_orientation(TextView textView)
		{
			if(AppSetting.getInstance(getApplicationContext()).getPage_turn_orientation() == 0)
			{
				textView.setText(AppSetting.page_turn_orientations[1]);
				textView.setCompoundDrawablesWithIntrinsicBounds(null,
					       getResources().getDrawable(AppSetting.page_turn_orientation_src[1]), null, null);
			}
			else {
				textView.setText(AppSetting.page_turn_orientations[0]);
				textView.setCompoundDrawablesWithIntrinsicBounds(null,
					       getResources().getDrawable(AppSetting.page_turn_orientation_src[0]), null, null);
			}
		}
		//popupwindow设置翻页左右手
		private void set_page_hand(TextView textView)
		{
			if(AppSetting.getInstance(getApplicationContext()).getPage_turn_hand() == AppSetting.LEFT_HAND)
			{
				textView.setText(AppSetting.page_turn_hands[AppSetting.RIGHT_HAND]);
				textView.setCompoundDrawablesWithIntrinsicBounds(null, 
						getResources().getDrawable(AppSetting.page_turn_hand_src[AppSetting.RIGHT_HAND]), null, null);
			}
			else {
				textView.setText(AppSetting.page_turn_hands[AppSetting.LEFT_HAND]);
				textView.setCompoundDrawablesWithIntrinsicBounds(null, 
						getResources().getDrawable(AppSetting.page_turn_hand_src[AppSetting.LEFT_HAND]), null, null);
			}
		}
		//返回其中一个PopupWindow就可以
		public boolean isShowing()
		{
			return topWindow.isShowing();
		}
		
		public SeekBar getSeekBar()
		{
			return this.pageSeekBar;
		}
		
		public void dismiss()
		{
			topWindow.dismiss();
			bottomWindow.dismiss();
		}
		
		//显示所有popupwindow
		public void show()
		{
			topWindow.showAtLocation(rootView, Gravity.TOP, 0, 0);
			bottomWindow.showAtLocation(rootView, Gravity.BOTTOM,0, 0);
		}

		public void onDismiss() {
			dismiss = true;
		}
	}
	
	//监听recycleView的ScrollListener
	private class ComicScrollListener extends RecyclerView.OnScrollListener
	{
		//记录滚动状态，当view滚动时，不加载图片
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			switch (newState) {
			//当滚动结束时则加载当前页图片以及当前页上一页图片
			//recyclerView中的子view要通过它的LayoutManager寻找到，然后在调用自身的getChildViewHolder
			//找到自己的viewHolder
			case OnScrollListener.SCROLL_STATE_IDLE:
				Log.d("Comic", "scroll stop");
				isScroll = false;
				int first = mLayoutManager.findFirstVisibleItemPosition();
				int last = mLayoutManager.findLastVisibleItemPosition();
				PictureAdapter.PicViewHolder vHolder = null;
				for(int i = first;i <= last;i++)
				{
					vHolder = (PictureAdapter.PicViewHolder) recyclerView.getChildViewHolder(
							mLayoutManager.findViewByPosition(i));
					BitmapLoader.getInstance().loadImage(vHolder.imageView,
							picPaths[i], true, false, false);
				}
				mPopWindowHolder.getSeekBar().setProgress(mLayoutManager.findFirstVisibleItemPosition());
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				isScroll = true;
				break;
			case OnScrollListener.SCROLL_STATE_FLING:	
				isScroll = true;
				break;
			default:
				break;
			}
		}
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
	public void onNextPageClick() {		
		int page = mLayoutManager.findFirstVisibleItemPosition();
		if(page < picPaths.length - 1)
			mLayoutManager.smoothScrollToPosition(mRecyclerView, null, ++page);
	}
	
	//上一页被点击
	public void onPrePageClick() {
		int page = mLayoutManager.findLastVisibleItemPosition();
		if(page > 0)
			mLayoutManager.smoothScrollToPosition(mRecyclerView, null, --page);
	}
	
	//菜单被点击,增加dismiss变量，由于popupwindow比自定义的view先收到Touch事件，所以会出现先消失在显示的问题，加入
	//dismiss变量解决
	public void onMenuClick() {
		if(!mPopWindowHolder.isShowing()&&!dismiss)
		{
			mPopWindowHolder.show();

		}
		
	}

	@Override
	public void onClick() {
		dismiss = false;
		if(mPopWindowHolder.isShowing())
		{
			mPopWindowHolder.dismiss(); 
			dismiss = true;
		}

	}
	



}
