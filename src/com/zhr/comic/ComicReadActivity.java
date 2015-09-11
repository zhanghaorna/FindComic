package com.zhr.comic;


import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.zhr.customview.ComicPageView;
import com.zhr.customview.ReaderHintView;
import com.zhr.customview.ReaderHintView.OnTouchClick;
import com.zhr.database.DBComicRecordHelper;
import com.zhr.findcomic.R;
import com.zhr.setting.AppSetting;
import com.zhr.setting.ReadSettingActivity;
import com.zhr.sqlitedao.ComicRecord;
import com.zhr.util.BaseActivity;
import com.zhr.util.BitmapLoader;
import com.zhr.util.Constants;
import com.zhr.util.Util;
import com.zhr.util.WeakActivityHandler;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Message;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
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
		
	private FrameLayout rootView = null;
//	private LinearLayout loadingLayout;
//	//Timer定时器,用来显示加载时切换动态图(
//	private Timer timer;
//	private int[] loadingImage_id = new int[]{R.drawable.playviewloading_1,R.drawable.playviewloading_2,
//			R.drawable.playviewloading_3,R.drawable.playviewloading_4,R.drawable.playviewloading_5,
//			R.drawable.playviewloading_6,R.drawable.playviewloading_7,R.drawable.playviewloading_8,
//			R.drawable.playviewloading_9,R.drawable.playviewloading_10,R.drawable.playviewloading_11,
//			R.drawable.playviewloading_12,R.drawable.playviewloading_13,R.drawable.playviewloading_14,
//			R.drawable.playviewloading_15,R.drawable.playviewloading_16,R.drawable.playviewloading_17,
//			R.drawable.playviewloading_18,R.drawable.playviewloading_19,R.drawable.playviewloading_20,
//			R.drawable.playviewloading_21};
//	private ImageView loadingImageView;
	
	private RecyclerView mRecyclerView;
	//RecyclerView是否在滑动，滑动就不加载图片
	private boolean isScroll;
	//判断是否是用户滑动seekbar,由于滑动停止后会设置seekbar的值，导致屏幕页面的跳转
	private boolean isSeekbarTouch;
	
	private PictureAdapter mAdapter;
	private LinearLayoutManager mLayoutManager;
	
	//记录漫画阅读记录
	private ComicRecord comicRecord;
	
	//漫画名字
	private String comicName;
	//章节名字
	private String chapterName;
	//所有漫画图片的路径
	private String[] picPaths;
//	//阅读的图片位置(第几页)
//	private int filePosition;
	//实际漫画显示页(也即显示在下面的)
	private int viewPosition;
	//漫画页是否来自网络
	private boolean fromInternet = false;
	//屏幕方向是否需要改变
	private boolean changeScreenOrientation = false;
	
	private PopWindowHolder mPopWindowHolder;
	private boolean dismiss;
	
	private ReaderHintView readerHintView;	
	//剩余电量
	private int battery;
	
	public static ComicLoadHandler handler;

	//开源服务
	//友盟分享
	private UMSocialService mController = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if(AppSetting.getInstance(getApplicationContext()).getScreen_orientation() != 
				getResources().getConfiguration().orientation)
		{
			changeScreenOrientation = true;
		}
		if(AppSetting.getInstance(getApplicationContext()).getScreen_orientation() ==
				AppSetting.HORIZONTAL_ORIENTATION)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		
		if(!changeScreenOrientation)
		{
			setContentView(R.layout.activity_comic_read);
			Log.d("Comic", "init");
			preData();
			initView();
			initData();
		}		
	}
	
	private void preData()
	{
//		loadingLayout = (LinearLayout)findViewById(R.id.loading_layout);
//		loadingImageView = (ImageView)findViewById(R.id.loading_image);
//		timer = new Timer();
//		//每100ms更新一次图片
//		timer.schedule(new ImageLoadingTask(), 0,100);
	
		
		Intent intent = getIntent();
		//获取传进的漫画相关数据
		picPaths = intent.getStringArrayExtra("picPaths");
		comicName = intent.getStringExtra("comicName");
		chapterName = intent.getStringExtra("chapterName");
		if(chapterName == null)
			chapterName = "";
		if(comicName == null)
			comicName = "";
		
		if(picPaths == null||picPaths.length == 0)
			picPaths = new String[]{""};
		
//		lp.type = 3;
		lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
//		lp.flags |= WindowManager.LayoutParams.LAST_SUB_WINDOW;

		
		mAdapter = new PictureAdapter();
		//翻页模式设置
		if(AppSetting.getInstance(getApplicationContext()).getPage_turn_orientation() == 
				AppSetting.MODE_IN_VERTICAL_UP_DOWN)
		{
			mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		}
		else 
		{
			mLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
		}
		//翻页方向
		if(AppSetting.getInstance(getApplicationContext()).getScreen_orientation() ==
				AppSetting.HORIZONTAL_ORIENTATION)
			mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

		
		
		handler = new ComicLoadHandler(this);
		
		if(intent.getStringExtra("comicUrl") != null)
		{
			fromInternet = true;
			queryImageUrlFromInternet(intent.getStringExtra("comicUrl"));
		}
		
		if(comicName.equals(""))
		{
			viewPosition = intent.getIntExtra("position", 0);
		}
		else
		{
			comicRecord = DBComicRecordHelper.getInstance(getApplicationContext()).getComicRecord(comicName);
			if(comicRecord == null)
			{
				comicRecord = new ComicRecord();
				comicRecord.setName(comicName);
				comicRecord.setPage(0);
				comicRecord.setChapter(chapterName);
			}
			if(chapterName.equals(comicRecord.getChapter()))
			{
				viewPosition = comicRecord.getPage();
			}
			else
			{
				comicRecord.setChapter(chapterName);
				comicRecord.setPage(0);
				viewPosition = 0;
			}					
		}
		
		mPopWindowHolder = new PopWindowHolder();
	}
	
	private void initView()
	{

		rootView = (FrameLayout)findViewById(R.id.root);

		
		mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
		mRecyclerView.setLayoutManager(mLayoutManager);
		
		mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
				DividerItemDecoration.VERTICAL_LIST));
		
		mRecyclerView.addOnScrollListener(new ComicScrollListener());
		if(!fromInternet)
		{
			mRecyclerView.setAdapter(mAdapter);
			mLayoutManager.scrollToPosition(viewPosition);
		}
			
		
		readerHintView = new ReaderHintView(this,getResources().getConfiguration().orientation);
		
		readerHintView.setOnTouchClickListener(this);
		
	}
	
	private void initData()
	{	
		batteryFilter = new IntentFilter();
		batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		batteryFilter.addAction(Intent.ACTION_TIME_TICK);
		batteryFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		
		if(Util.isNetWorkConnect(getApplicationContext()))
		{
			mController = UMServiceFactory.getUMSocialService("com.umeng.share");
			//配置友盟分享相关设置
			mController.getConfig().setSsoHandler(new SinaSsoHandler());
		}

		if(!fromInternet)
		{
//			timer.cancel();
//			loadingLayout.setVisibility(View.GONE);
			mRecyclerView.setVisibility(View.VISIBLE);
		}
		
	}
	//当用户点击设置，重新返回Activity应用设置
	private void changeSetting()
	{
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			if(AppSetting.getInstance(getApplicationContext()).getScreen_orientation() ==
					AppSetting.VERTICAL_ORIENTATION)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);			
		}
		else
		{
			if(AppSetting.getInstance(getApplicationContext()).getScreen_orientation() ==
					AppSetting.HORIZONTAL_ORIENTATION)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			else 
			{
				if(AppSetting.getInstance(getApplicationContext()).getPage_turn_orientation() ==
						AppSetting.MODE_IN_VERTICAL_LEFT_RIGHT)
				{
					mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
				}
				else {
					mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
				}
				
				if(AppSetting.getInstance(getApplicationContext()).getPage_turn_hand() ==
						AppSetting.LEFT_HAND)
				{
					readerHintView.setHandMode(AppSetting.LEFT_HAND);
				}
				else
				{
					readerHintView.setHandMode(AppSetting.RIGHT_HAND);
				}
			}				
		}
		
		//是否显示状态栏
		if(AppSetting.getInstance(getApplicationContext()).isShow_time_battery())
			readerHintView.showStatusView();
		else
			readerHintView.hideStatusView();
	}
	
	
	private void queryImageUrlFromInternet(String url)
	{
		AsyncHttpClient client = new AsyncHttpClient();
//		Log.d("Comic", "query url" + url);
		client.get(url, new AsyncHttpResponseHandler() {

			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				if(arg0 == 200)
				{
					picPaths = Util.getImageUrlsFromInternet(arg2);
				}
//				timer.cancel();
				
				mAdapter.notifyDataSetChanged();
				mPopWindowHolder.refreshStatus();

				readerHintView.setStatusText(battery,mPopWindowHolder.getPageHint().getText().toString());
				mRecyclerView.setAdapter(mAdapter);
//				loadingLayout.setVisibility(View.GONE);
				mRecyclerView.setVisibility(View.VISIBLE);
				mLayoutManager.scrollToPosition(viewPosition);
			}
			
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				Toast.makeText(getBaseContext(), "漫画加载失败", Toast.LENGTH_SHORT).show();
//				timer.cancel();
			}
		});
	}
	
	
//	private class ImageLoadingTask extends TimerTask
//	{
//		private int index = 1;
//		public void run() {			
//			if(loadingImageView != null)
//			{
//				loadingImageView.post(new Runnable() {
//					public void run() {
//						loadingImageView.setImageDrawable(getResources()
//								.getDrawable(loadingImage_id[index]));						
//					}
//				});
//				index++;
//				if(index > 20)
//					index = 1;
//			}
//		}		
//	}
	
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
						Util.getImageHeight(ComicReadActivity.this)));				
			}			
		}

		public int getItemCount() {
			return picPaths == null ? 0 : picPaths.length;
		}

		public void onBindViewHolder(PicViewHolder holder, int poistion) {
			holder.imageView.setPageNum(poistion + 1);
//			if(!isScroll)
			
			
//			BitmapLoader.getInstance().loadImage(holder.imageView,
//						picPaths[poistion], true, false, false,true);
			BitmapLoader.getInstance().loadComicImage(holder.imageView,
						picPaths[poistion]);
			preLoadComicPage(poistion);
			Log.d("Comic", "load " + poistion);						
			viewPosition = poistion;
			if(comicRecord != null&&comicRecord.getPage() != viewPosition)
			{
				comicRecord.setPage(viewPosition);
			}
//			Log.d("Comic", "path:" + picPaths[poistion]);

		}

		@Override
		public PicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			PicViewHolder holder = new PicViewHolder(LayoutInflater.from(ComicReadActivity.this).
					inflate(R.layout.comic_read_recycleview_item, parent,false));
			return holder;
		}
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
			
			//将popupWindow 的type设为TYPE_SYSTEM_ALERT，可以置于最顶层
			try
			{
				Field filed = bottomWindow.getClass().getDeclaredField("mWindowLayoutType");
				filed.setAccessible(true);
				filed.set(bottomWindow, android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			setupBottomWindowListener(bottomWindow.getContentView());
		}
		//设置topWindow监听
		private void setupTopWindowListener(View view)
		{
			ImageView back = (ImageView)view.findViewById(R.id.back);
			back.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dismiss();
					Intent intent = new Intent();
					intent.putExtra("last_read_path", picPaths[viewPosition]);
					setResult(RESULT_OK,intent);					
					finish();
				}
			});
			
			TextView title =  (TextView)view.findViewById(R.id.title);
			title.setText(comicName + " " + chapterName);
			
			TextView share = (TextView)view.findViewById(R.id.share);
			share.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if(mController == null)
					{
						Toast.makeText(ComicReadActivity.this, "网络未连接，无法分享",
								Toast.LENGTH_SHORT).show();
						return;
					}
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
			page_hint.setText((viewPosition + 1) + "/" + picPaths.length);
			
			pageSeekBar = (SeekBar)view.findViewById(R.id.page_seekbar);
			pageSeekBar.setMax(picPaths.length - 1);
			pageSeekBar.setProgress(viewPosition);
			pageSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				
				public void onStopTrackingTouch(SeekBar seekBar) {
					
				}
				
				public void onStartTrackingTouch(SeekBar seekBar) {
					isSeekbarTouch = true;
				}
				
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					page_hint.setText((progress  + 1) + "/" + picPaths.length);
					if(isSeekbarTouch)
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
				textView.setText(AppSetting.screen_orientations[0]);
				textView.setCompoundDrawablesWithIntrinsicBounds(null,
						getResources().getDrawable(AppSetting.screen_orientations_src[0]), null, null);
			}
			else
			{
				textView.setText(AppSetting.screen_orientations[1]);
				textView.setCompoundDrawablesWithIntrinsicBounds(null,
						getResources().getDrawable(AppSetting.screen_orientations_src[1]), null, null);
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
		
		public TextView getPageHint()
		{
			return this.page_hint;
		}
		
		public void refreshStatus()
		{			
			pageSeekBar.setMax(picPaths.length - 1);
			pageSeekBar.setProgress(viewPosition);
			page_hint.setText((viewPosition + 1) + "/" + picPaths.length);
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
				isScroll = false;
				//由于滚动时显示的页面不多，取消滚动时显示，由于每次暂停时加载，会严重影响性能，导致加载过慢
//				int first = mLayoutManager.findFirstVisibleItemPosition();
//				int last = mLayoutManager.findLastVisibleItemPosition();
//				PictureAdapter.PicViewHolder vHolder = null;
//				for(int i = first;i <= last;i++)
//				{
//					vHolder = (PictureAdapter.PicViewHolder) recyclerView.getChildViewHolder(
//							mLayoutManager.findViewByPosition(i));
////					BitmapLoader.getInstance().loadImage(vHolder.imageView,
////							picPaths[i], true, false, false);
//					BitmapLoader.getInstance().loadComicImage(vHolder.imageView, picPaths[i]);
//				}
//				Log.d("Comic", "scroll run");
//				preLoadComicPage(first);

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
			isSeekbarTouch = false;
			mPopWindowHolder.getSeekBar().setProgress(mLayoutManager.findFirstVisibleItemPosition());
			readerHintView.setStatusText(battery,mPopWindowHolder.getPageHint().getText().toString());
		}
	}
	//提前预加载3张图片(当前页，以及上下各两页),暂时修改为只加载下一页
	private void preLoadComicPage(int position)
	{
		for(int i = position + 1;i <= position + 1;i++)
		{
			if(i >= 0&&i <= picPaths.length - 1&&i != position)
			{
				BitmapLoader.getInstance().loadComicImage(null, picPaths[i]);
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
	
	public static class ComicLoadHandler extends WeakActivityHandler<Activity>
	{

		public ComicLoadHandler(Activity activty) {
			super(activty);
		}
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);	
		}		
	}
	
	private IntentFilter batteryFilter;
	//电量改变监听器，获取手机电量,(同时获取网络状态)
	private BroadcastReceiver batteryChangeReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent == null)
				return;
			if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction()))
			{
				int level = intent.getIntExtra("level", 0);
				int scale = intent.getIntExtra("scale", 100);
				battery = level * 100 / scale;
				if(readerHintView != null&&mPopWindowHolder != null)
					readerHintView.setStatusText(battery,mPopWindowHolder.getPageHint().getText().toString());
			}
			else if(Intent.ACTION_TIME_TICK.equals(intent.getAction()))
			{
				if(readerHintView != null&&mPopWindowHolder != null)
					readerHintView.setStatusText(battery,mPopWindowHolder.getPageHint().getText().toString());
			}
			else if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction()))
			{
				if(Util.isNetWorkConnect(getApplicationContext()))
				{
					//当有网络时就可以初始化
					if(mController == null)
					{
						mController = UMServiceFactory.getUMSocialService("com.umeng.share");
						//配置友盟分享相关设置
						mController.getConfig().setSsoHandler(new SinaSsoHandler());
					}
				}
			}
		}
	};
	
	//监听音量键点击
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) 
	{
		
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			if(AppSetting.getInstance(getApplicationContext()).isPageOver_by_volume())
			{
				onPrePageClick();
				return true;
			}
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if(AppSetting.getInstance(getApplicationContext()).isPageOver_by_volume())
			{
				onNextPageClick();
				return true;
			}
			break;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	};
	
	//滑动体验不好，可以改为滑动一定距离
	//下一页被点击
	public void onNextPageClick() {		
		Log.d("Comic", "next page");
		int page = mLayoutManager.findFirstVisibleItemPosition();
		if(page < picPaths.length - 1)
			mLayoutManager.smoothScrollToPosition(mRecyclerView, null, ++page);
	}
	
	//上一页被点击
	public void onPrePageClick() {
		Log.d("Comic", "pre page");
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
		readerHintView.setStatusText(battery,mPopWindowHolder.getPageHint().getText().toString());
		dismiss = false;
		if(mPopWindowHolder.isShowing())
		{
			mPopWindowHolder.dismiss(); 
			dismiss = true;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("Comic", "onResume");
		if(!changeScreenOrientation)
			changeSetting();
		if(mAdapter != null)
			mAdapter.notifyItemChanged(viewPosition);
		if(batteryChangeReceiver != null&&batteryFilter != null)
			registerReceiver(batteryChangeReceiver, batteryFilter);
		if(!AppSetting.getInstance(getApplicationContext()).isKeep_screen_on())
			lp.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		else 
			lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
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
		if(mPopWindowHolder!= null&&mPopWindowHolder.isShowing())
		{
			mPopWindowHolder.dismiss(); 
		}
		if(batteryChangeReceiver != null&&batteryFilter != null)
			unregisterReceiver(batteryChangeReceiver);
		AppSetting.getInstance(getApplicationContext()).commitAllAlter();
		if(comicRecord != null)
			DBComicRecordHelper.getInstance(getApplicationContext()).saveRecord(comicRecord);
	}
	
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mController != null)
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
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		if(fromInternet)
		{
			super.onBackPressed();
		}
		else
		{
			intent.putExtra("last_read_path", picPaths[viewPosition]);
			setResult(RESULT_OK,intent);	
			finish();
		}
		
		
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Log.d("Comic", "turn orientation");
	}

}
