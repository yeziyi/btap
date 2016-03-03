package com.tapjoy;

import java.util.HashSet;
import java.util.Set;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ManyVideoTJCOffersWebView extends TJCOffersWebView {

	private Set<String> set = new HashSet<String>();
	private long downTime = System.currentTimeMillis();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TapjoyLog.enableLogging(true);
		this.bridge = new MyTJAdUnitJSBridge(this, webView, null);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view,
					final String url) {
				if (url.contains("ws.tapjoyads.com/videos")) {
					set.add(url);
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
			if (i == 50 - 1) {
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
								for (String xu : set) {
									Log.e("", "url = " + xu);
									webView.loadUrl(xu);
									set.remove(xu);
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
				Log.e("", "set.size() = " + set.size());
				for (String xu : set) {
					Log.e("", "url = " + xu);
					webView.loadUrl(xu);
					set.remove(xu);
					return;
				}
			};
		}.start();
	}
}