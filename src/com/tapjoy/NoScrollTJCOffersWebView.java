package com.tapjoy;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.umeng.analytics.MobclickAgent;

public class NoScrollTJCOffersWebView extends TJCOffersWebView {

	private long downTime = System.currentTimeMillis();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view,
					final String url) {
				Log.e("", "url = " + url);
				new Thread() {
					public void run() {
						String text = Util.sendGet(url);
						Log.e("", "text = " + text);
					};
				}.start();
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				handleWebViewOnPageFinished(view, url);
			}
		});
	}

	@Override
	public void handleWebViewOnPageFinished(WebView view, String url) {
		super.handleWebViewOnPageFinished(view, url);
		Log.e("", "handleWebViewOnPageFinished");
		int y = 50;
		int ystep = 50;
		int delay = 1500;
		int delaystep = 1500;
		for (int i = 0; i < 50; i++) {
			final int currenty = y + i * ystep;
			final int currentdelay = delay + i * delaystep;
			webView.postDelayed(new Runnable() {

				@Override
				public void run() {
					downTime = System.currentTimeMillis();
					webView.dispatchTouchEvent(MotionEvent
							.obtain(downTime, downTime,
									MotionEvent.ACTION_DOWN, 400, currenty, 0));
				}
			}, currentdelay);
			webView.postDelayed(new Runnable() {

				@Override
				public void run() {
					webView.dispatchTouchEvent(MotionEvent.obtain(downTime,
							System.currentTimeMillis(), MotionEvent.ACTION_UP,
							400, currenty, 0));
				}
			}, currentdelay + 120);
		}
	}

	@Override
	public void handleWebViewOnReceivedError(WebView view, int errorCode,
			String description, String failingUrl) {
		super.handleWebViewOnReceivedError(view, errorCode, description,
				failingUrl);
		Log.e("", "handleWebViewOnReceivedError");
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("MyTJCOffersWebView");
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("MyTJCOffersWebView");
		MobclickAgent.onPause(this);
	}

}
