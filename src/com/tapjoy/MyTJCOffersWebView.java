package com.tapjoy;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.umeng.analytics.MobclickAgent;

public class MyTJCOffersWebView extends TJCOffersWebView {

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
		stepbystep();
	}

	private void stepbystep() {
		int y = 50;
		int ystep = 50;
		int delay = 500;
		int delaystep = 1500;
		for (int i = 0; i < 50; i++) {
			final int index = i;
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
					if (index == 49) {
						webView.postDelayed(new Runnable() {

							@Override
							public void run() {
								scrollbyscroll();
							}
						}, 100);
					}
				}
			}, currentdelay + 100);
		}

	}

	private void scrollbyscroll() {
		final int touchx = 300;
		final long downtime = System.currentTimeMillis();
		webView.postDelayed(new Runnable() {

			@Override
			public void run() {
				webView.dispatchTouchEvent(MotionEvent.obtain(downtime,
						downtime, MotionEvent.ACTION_DOWN, touchx, 1200, 0));
			}
		}, 0);
		int maxcount = 120;
		for (int i = 0; i < maxcount; i++) {
			final int currentY = 1200 - i * 10;
			final long eventTime = downtime + i * 20;
			webView.postDelayed(new Runnable() {

				@Override
				public void run() {
					webView.dispatchTouchEvent(MotionEvent.obtain(downtime,
							eventTime, MotionEvent.ACTION_MOVE, touchx,
							currentY, 0));
				}
			}, eventTime - downtime);
			if (i == maxcount - 1) {
				webView.postDelayed(new Runnable() {

					@Override
					public void run() {
						webView.dispatchTouchEvent(MotionEvent.obtain(downtime,
								eventTime + 15, MotionEvent.ACTION_UP, touchx,
								currentY, 0));
						webView.postDelayed(new Runnable() {

							@Override
							public void run() {
								stepbystep();
							}
						}, 100);
					}
				}, eventTime - downtime + 15);
			}
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
