package com.tapjoy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ManyVideoTJCOffersWebView extends TJCOffersWebView {

	private List<String> list = new ArrayList<String>();
	private long downTime = System.currentTimeMillis();

	private boolean startclick = false;
	private Handler mHandler = new Handler(Looper.getMainLooper());
	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			mHandler.removeCallbacks(this);
			mHandler.removeCallbacks(mRunnable);
			if (startclick) {
				return;
			}
			long downTime = System.currentTimeMillis();
			webView.dispatchTouchEvent(MotionEvent.obtain(downTime, downTime,
					MotionEvent.ACTION_DOWN, 400, 400, 0));
			try {
				Thread.sleep(120);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			webView.dispatchTouchEvent(MotionEvent.obtain(downTime,
					System.currentTimeMillis(), MotionEvent.ACTION_UP, 400,
					400, 0));
			mHandler.postDelayed(this, 1000);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TapjoyLog.enableLogging(true);
		this.bridge = new MyTJAdUnitJSBridge(this, webView, null);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view,
					final String url) {
				Log.e("test", "shouldOverrideUrlLoading url = " + url);
				if (!startclick) {
					startclick = true;
					handleWebViewOnPageFinished(view, url);
					return true;
				}
				if (url.contains("ws.tapjoyads.com/videos")) {
					if (!list.contains(url)) {
						list.add(url);
					}
				}
				// Log.e("", "url = " + url);
				new Thread() {
					public void run() {
						String text = Util.sendGet(url);
						Log.e("", "text = " + text);
					};
				}.start();
				return true;
			}

		});
		mHandler.postDelayed(mRunnable, 1000);
	}

	@Override
	public void handleWebViewOnPageFinished(WebView view, String url) {
		super.handleWebViewOnPageFinished(view, url);
		Log.e("", "handleWebViewOnPageFinished");
		int y = 50;
		int ystep = 50;
		int delay = 1500;
		int delaystep = 1500;
		for (int i = 0; i < 30; i++) {
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
			if (i == 30 - 1) {
				webView.postDelayed(new Runnable() {

					@Override
					public void run() {
						webView.setWebViewClient(new WebViewClient() {
							@Override
							public boolean shouldOverrideUrlLoading(
									WebView view, final String url) {
								view.loadUrl(url);
								return true;
							}

							@Override
							public void onPageFinished(WebView view, String url) {
								super.onPageFinished(view, url);
							}
						});
						new Thread() {
							public void run() {
								for (String xu : list) {
									Log.e("", "url = " + xu);
									webView.loadUrl(xu);
									list.remove(xu);
									return;
								}
							};
						}.start();
					}
				}, currentdelay + 120 + 1000);
			}
		}
	}

	@Override
	public void handleWebViewOnReceivedError(WebView view, int errorCode,
			String description, String failingUrl) {
		Log.e("", "handleWebViewOnReceivedError");
		webView.loadUrl(failingUrl);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e("", "onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
		new Thread() {
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Log.e("", "set.size() = " + list.size());
				for (String xu : list) {
					Log.e("", "url = " + xu);
					webView.loadUrl(xu);
					list.remove(xu);
					return;
				}
			};
		}.start();
	}
}