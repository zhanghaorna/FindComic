package com.zhr.mainpage;


import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.umeng.socialize.net.f;
import com.zhr.customview.WaitProgressBar;
import com.zhr.findcomic.R;
import com.zhr.util.BaseActivity;
import com.zhr.util.Constants;

/**
 * @author zhr
 * @version 1.0.0
 * @date 2015年5月31日
 * @description
 */
public class NewsWebviewActivity extends BaseActivity{
	
	private WebView mWebView;
	private WaitProgressBar progressBar;
	
	private String URL = "";
	private AsyncHttpClient client;
	
	private String from;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news_webview);
		preData();
		initView();
		initData();
	}
	
	private void initView()
	{
		mWebView = (WebView)findViewById(R.id.webview);	
		mWebView.setWebViewClient(new WebViewClient(){
			//新请求的网址也由本webview处理
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}	
		});
		progressBar = (WaitProgressBar) findViewById(R.id.progressBar);
//		mWebView.getSettings().setJavaScriptEnabled(true);
//		mWebView.loadUrl(URL);
	}
	
	private void preData()
	{
		URL = getIntent().getStringExtra("content_url");
		if(URL == null)
		{
			Toast.makeText(this, "该新闻无法显示", Toast.LENGTH_SHORT).show();
			URL = "";
		}
		from = getIntent().getStringExtra("from");
		if(from == null)
			from = Constants.DMZJ;

	}
	
	private void initData()
	{
		//目前MSite的网站不转码
		if(from.equals(Constants.MSITE))
		{
			mWebView.loadUrl(URL);
			return;
		}
		client = new AsyncHttpClient();
		client.addHeader("User-Agent", "MQQBrowser/26 Mozilla/5.0 (Linux; U; Android 2.3.7; zh-cn; MB200 Build/GRJ22; CyanogenMod-7) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
		client.get(URL, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] response) {
				if(arg0 == 200)
				{
					String webPage = "";
					if(from.equals(Constants.DMZJ))
					{
						webPage = filterDmzj(response);
					}
					else if(from.equals(Constants.MSITE))
					{
						webPage = filterMSite(response);
					}
					mWebView.loadData(webPage, "text/html;charset=UTF-8", null);
				}
				
			}
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				Toast.makeText(NewsWebviewActivity.this, "该新闻无法显示", Toast.LENGTH_SHORT).show();				
			}
		});
	}
	
	private String filterDmzj(byte[] response)
	{
		Document doc = Jsoup.parse(new String(response));
		Elements elements = doc.select("div.wrap");
		for(Element element:elements.get(0).children())
		{
			Log.d("Comic", element.className());
			if(!element.className().equals("mainPage"))
			{
				element.remove();
			}
		}
		return doc.html();
	}
	
	private String filterMSite(byte[] response)
	{
		Document doc = Jsoup.parse(new String(response));
		return doc.html();
	}
	
	@Override
	public void onBackPressed() {
		if(mWebView.canGoBack())
			mWebView.goBack();
		else
			super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mWebView != null)
		{
			mWebView.clearHistory();
			mWebView.clearCache(true);
			mWebView.destroy();
		}
		
	}

}
