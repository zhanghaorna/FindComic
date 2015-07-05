package com.zhr.comic;


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
import com.zhr.findcomic.R;
import com.zhr.setting.AppSetting;
import com.zhr.setting.ReadSettingActivity;
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
import android.os.Bundle;
import android.os.Message;
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
	private LinearLayout loadingLayout;
	//Timer定时器,用来显示加载时切换动态图
	private Timer timer;
	private int[] loadingImage_id = new int[]{R.drawable.playviewloading_1,R.drawable.playviewloading_2,
			R.drawable.playviewloading_3,R.drawable.playviewloading_4,R.drawable.playviewloading_5,
			R.drawable.playviewloading_6,R.drawable.playviewloading_7,R.drawable.playviewloading_8,
			R.drawable.playviewloading_9,R.drawable.playviewloading_10,R.drawable.playviewloading_11,
			R.drawable.playviewloading_12,R.drawable.playviewloading_13,R.drawable.playviewloading_14,
			R.drawable.playviewloading_15,R.drawable.playviewloading_16,R.drawable.playviewloading_17,
			R.drawable.playviewloading_18,R.drawable.playviewloading_19,R.drawable.playviewloading_20,
			R.drawable.playviewloading_21};
	private ImageView loadingImageView;
	
	private RecyclerView mRecyclerView;
	//RecyclerView是否在滑动，滑动就不加载图片
	private boolean isScroll;
	//判断是否是用户滑动seekbar,由于滑动停止后会设置seekbar的值，导致屏幕页面的跳转
	private boolean isSeekbarTouch;
	
	private PictureAdapter mAdapter;
	private LinearLayoutManager mLayoutManager;
	//获取漫画网的前缀
	private int url_prefix_position;
	
	//漫画名字
	private String comicName;
	//所有漫画图片的路径
	private String[] picPaths;
	//阅读的图片位置(第几页)
	private int filePosition;
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
	private UMSocialService mController;
	
	
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
		setContentView(R.layout.activity_comic_read);
		if(!changeScreenOrientation)
		{
			Log.d("Comic", "init");
			preData();
			initView();
			initData();
		}		
	}
	
	private void preData()
	{
		loadingLayout = (LinearLayout)findViewById(R.id.loading_layout);
		loadingImageView = (ImageView)findViewById(R.id.loading_image);
		timer = new Timer();
		//每100ms更新一次图片
		timer.schedule(new ImageLoadingTask(), 0,100);
	
		
		Intent intent = getIntent();

		picPaths = intent.getStringArrayExtra("picPaths");
		comicName = intent.getStringExtra("comicName");
		if(comicName == null)
			comicName = "";
		if(picPaths == null||picPaths.length == 0)
			picPaths = new String[]{""};
		filePosition = intent.getIntExtra("position", 0);

		
		viewPosition = filePosition;
		
		lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		

		
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
		handler = new ComicLoadHandler(this);
		
		if(intent.getStringExtra("comicUrl") != null)
		{
			fromInternet = true;
			queryImageUrlFromInternet(intent.getStringExtra("comicUrl"));
		}
	}
	
	private void initView()
	{

		rootView = (FrameLayout)findViewById(R.id.root);

		
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
		if(!fromInternet)
		{
			timer.cancel();
			loadingLayout.setVisibility(View.GONE);
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
	}
	
	
	private void queryImageUrlFromInternet(String url)
	{
		AsyncHttpClient client = new AsyncHttpClient();
//		Log.d("Comic", "query url" + url);
		client.get(url, new AsyncHttpResponseHandler() {

			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				if(arg0 == 200)
				{
					Document document = Jsoup.parse(new String(arg2));					
					String text = document.data();
					if(!text.equals("")&&text.indexOf(";") != -1)
					{						
						String position = text.split(";")[1];
						text = text.split(";")[0];
						position = position.replace("var", "").replace("sPath=", "").replace("\"", "")
							.replace(" ", "");
						url_prefix_position = Integer.valueOf(position).intValue();
					}
					text = text.replace("var", "").replace("sFiles=", "").replace("\"", "")
							.replace(" ", "");
					getImageUrl(text);
				}
				Log.d("Comic", "disappear");
				timer.cancel();

				mAdapter.notifyDataSetChanged();
				mPopWindowHolder.refreshStatus();
				readerHintView.setStatusText(battery,mPopWindowHolder.getPageHint().getText().toString());
				loadingLayout.setVisibility(View.GONE);
				mRecyclerView.setVisibility(View.VISIBLE);
			}
			
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				Toast.makeText(getBaseContext(), "漫画加载失败", Toast.LENGTH_SHORT).show();
				timer.cancel();
			}
		});
	}
	//从一串代码中解析出图片地址，由于该网站采取了加密
	private void getImageUrl(String code)
	{
		String x = code.substring(code.length() - 1);
		int xi = "abcdefghijklmnopqrstuvwxyz".indexOf(x) + 1;
		String sk = code.substring(code.length() - xi - 12,code.length() - xi - 1);
		code = code.substring(0,code.length() - xi - 12);
		String k = sk.substring(0,sk.length() - 1);
		String f = sk.substring(sk.length() - 1);
		for(int i = 0;i < k.length();i++)
		{
			code = code.replaceAll(k.substring(i,i+1), i + "");
		}
		String[] ss = code.split(f);
		StringBuilder builder = new StringBuilder();
		for(int i = 0;i < ss.length;i++)
		{
			builder.append((char)Integer.valueOf(ss[i]).intValue());
		}
		String[] path = builder.toString().split("\\|");
		for(int i = 0;i < path.length;i++)
		{
			path[i] = Constants.URL_PERFIX[url_prefix_position - 1] + path[i];
		}
		picPaths = path;
	}
	
	private class ImageLoadingTask extends TimerTask
	{
		private int index = 1;
		public void run() {			
			if(loadingImageView != null)
			{
				loadingImageView.post(new Runnable() {
					public void run() {
						loadingImageView.setImageDrawable(getResources()
								.getDrawable(loadingImage_id[index]));						
					}
				});
				index++;
				if(index > 20)
					index = 1;
			}
		}		
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
			Log.d("Comic", "load" + poistion);						
			viewPosition = poistion;
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
			title.setText(comicName);
			
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
			pageSeekBar.setProgress(filePosition);
			page_hint.setText((filePosition + 1) + "/" + picPaths.length);
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
	//提前预加载5张图片(当前页，以及上下各两页)
	private void preLoadComicPage(int position)
	{
		for(int i = position - 1;i <= position + 1;i++)
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
	
	//电量改变监听器，获取手机电量
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
				if(readerHintView != null)
					readerHintView.setStatusText(battery,mPopWindowHolder.getPageHint().getText().toString());
			}
			else if(Intent.ACTION_TIME_TICK.equals(intent.getAction()))
			{
				if(readerHintView != null)
					readerHintView.setStatusText(battery,mPopWindowHolder.getPageHint().getText().toString());
			}
		}
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
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		intentFilter.addAction(Intent.ACTION_TIME_TICK);
		if(batteryChangeReceiver != null)
			registerReceiver(batteryChangeReceiver, intentFilter);
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
		if(batteryChangeReceiver != null)
			unregisterReceiver(batteryChangeReceiver);
		AppSetting.getInstance(getApplicationContext()).commitAllAlter();
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
		intent.putExtra("last_read_path", picPaths[viewPosition]);
		setResult(RESULT_OK,intent);	
		super.onBackPressed();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Log.d("Comic", "turn orientation");
	}

}
