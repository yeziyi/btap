package com.tapjoy;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyTapjoyVideoView extends Activity implements
		MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
		MediaPlayer.OnPreparedListener {
	private TextView overlayText;
	private String videoURL;
	private String webviewURL;
	private RelativeLayout relativeLayout;
	private Bitmap watermark;
	Timer timer;
	private static boolean videoError = false;
	private static TapjoyVideoObject videoData;
	private boolean dialogShowing;
	private boolean sendClick;
	private boolean clickRequestSuccess;
	private boolean shouldDismiss;
	private int seekTime;
	private ImageView tapjoyImage;
	final Handler mHandler;
	static int textSize = 16;
	final Runnable mUpdateResults;

	protected void onCreate(Bundle savedInstanceState) {
		Log.e("test", "MyTapjoyVideoView onCreate");
		TapjoyLog.i("VideoView", "onCreate");
		super.onCreate(savedInstanceState);

		TapjoyConnectCore.viewWillOpen(3);
		if (savedInstanceState != null) {
			TapjoyLog.i("VideoView", "*** Loading saved data from bundle ***");
			this.seekTime = savedInstanceState.getInt("seek_time");
			this.dialogShowing = savedInstanceState
					.getBoolean("dialog_showing");
		}
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			videoData = (TapjoyVideoObject) extras
					.getSerializable("VIDEO_DATA");

			this.videoURL = extras.getString("VIDEO_URL");
			if (extras.containsKey("VIDEO_SHOULD_DISMISS")) {
				this.shouldDismiss = extras.getBoolean("VIDEO_SHOULD_DISMISS");
			}
		}
		TapjoyLog.i("VideoView", "dialogShowing: " + this.dialogShowing
				+ ", seekTime: " + this.seekTime);
		if (videoData != null) {
			this.sendClick = true;
			if (TapjoyVideo.getInstance() == null) {
				TapjoyLog.i("VideoView", "null video");
				finishWithResult(false);
				return;
			}
			this.videoURL = videoData.dataLocation;
			this.webviewURL = videoData.webviewURL;
			if ((this.videoURL == null) || (this.videoURL.length() == 0)) {
				TapjoyLog.i("VideoView",
						"no cached video, try streaming video at location: "
								+ videoData.videoURL);
				this.videoURL = videoData.videoURL;
			}
			TapjoyLog.i("VideoView", "videoPath: " + this.videoURL);
		} else if (this.videoURL != null) {
			this.sendClick = false;

			TapjoyLog.i("VideoView", "playing video only: " + this.videoURL);
		}
		requestWindowFeature(1);
		this.relativeLayout = new RelativeLayout(this);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				-1, -1);
		this.relativeLayout.setLayoutParams(params);
		setContentView(this.relativeLayout);
		if (Build.VERSION.SDK_INT > 3) {
			TapjoyDisplayMetricsUtil displayMetricsUtil = new TapjoyDisplayMetricsUtil(
					this);

			int deviceScreenLayoutSize = displayMetricsUtil
					.getScreenLayoutSize();

			TapjoyLog.i("VideoView", "deviceScreenLayoutSize: "
					+ deviceScreenLayoutSize);
			if (deviceScreenLayoutSize == 4) {
				textSize = 32;
			}
		}

		initVideoView();

		TapjoyLog.i("VideoView", "onCreate DONE");

		TapjoyConnectCore.viewDidOpen(3);

		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				onCompletion(null);
			}
		}, 5 * 1000);
	}

	protected void onDestroy() {
		super.onDestroy();
		if (isFinishing()) {
			TapjoyConnectCore.viewWillClose(3);
			TapjoyConnectCore.viewDidClose(3);
		}
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		TapjoyLog.i("VideoView", "*** onSaveInstanceState ***");
		TapjoyLog.i("VideoView", "dialogShowing: " + this.dialogShowing
				+ ", seekTime: " + this.seekTime);
		outState.putBoolean("dialog_showing", this.dialogShowing);
		outState.putInt("seek_time", this.seekTime);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		TapjoyLog.i("VideoView", "onActivityResult requestCode:" + requestCode
				+ ", resultCode: " + resultCode);

		Bundle extras = null;
		if (data != null) {
			extras = data.getExtras();
		}
		String result = extras != null ? extras.getString("result") : null;
		if ((result == null) || (result.length() == 0)
				|| (result.equals("offer_wall"))) {
			finishWithResult(true);
		} else if (result.equals("tjvideo")) {
			initVideoView();
		}
	}

	private void initVideoView() {
		this.relativeLayout.removeAllViews();
		this.relativeLayout.setBackgroundColor(-16777216);
		if ((this.overlayText == null)) {
			this.tapjoyImage = new ImageView(this);

			this.watermark = TapjoyVideo.getWatermarkImage();
			if (this.watermark != null) {
				this.tapjoyImage.setImageBitmap(this.watermark);
			}
			RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
					-2, -2);
			imageParams.addRule(12);
			imageParams.addRule(11);
			this.tapjoyImage.setLayoutParams(imageParams);

			this.overlayText = new TextView(this);
			this.overlayText.setTextSize(textSize);
			this.overlayText.setTypeface(Typeface.create("default", 1), 1);

			RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
					-2, -2);
			textParams.addRule(12);
			this.overlayText.setLayoutParams(textParams);
		}

		this.relativeLayout.addView(this.tapjoyImage);
		this.relativeLayout.addView(this.overlayText);
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				startVideo();
			}
		}, 1000);
	}

	private void showVideoCompletionScreen() {
		if (this.shouldDismiss) {
			finishWithResult(true);
		} else {
			Intent intent = new Intent(this, TJAdUnitView.class);
			intent.putExtra("view_type", 4);
			intent.putExtra("url", this.webviewURL);
			intent.putExtra("legacy_view", true);

			startActivityForResult(intent, 0);
		}
	}

	private void startVideo() {
		if (this.dialogShowing) {
			TapjoyLog.i("VideoView", "dialog is showing -- don't start");
		} else {
			TapjoyLog.i("VideoView", "start");
			TapjoyVideo.videoNotifierStart();
		}
		if (this.timer != null) {
			this.timer.cancel();
		}
		this.timer = new Timer();
		this.timer.schedule(new RemainingTime(), 500L, 100L);

		this.clickRequestSuccess = false;
		if (this.sendClick) {
			new Thread(new Runnable() {
				public void run() {
					TapjoyLog.i("VideoView", "SENDING CLICK...");

					TapjoyHttpURLResponse response = new TapjoyURLConnection()
							.getResponseFromURL(videoData.clickURL);
					if ((response.response != null)
							&& (response.response.contains("OK"))) {
						TapjoyLog.i("VideoView", "CLICK REQUEST SUCCESS!");
						clickRequestSuccess = true;
					}
				}
			}).start();
			this.sendClick = false;
		}
	}

	private void finishWithResult(boolean result) {
		Intent returnIntent = new Intent();
		returnIntent.putExtra("result", result);
		returnIntent.putExtra("result_string1", Float.toString(29.84f));
		returnIntent.putExtra("result_string2", Float.toString(30.0f));
		returnIntent.putExtra("callback_id",
				getIntent().getStringExtra("callback_id"));
		setResult(-1, returnIntent);
		finish();
	}

	private int getRemainingVideoTime() {
		return 0;
	}

	private class RemainingTime extends TimerTask {
		private RemainingTime() {
		}

		public void run() {
			MyTapjoyVideoView.this.mHandler
					.post(MyTapjoyVideoView.this.mUpdateResults);
		}
	}

	public MyTapjoyVideoView() {
		this.overlayText = null;
		this.videoURL = null;
		this.webviewURL = null;

		this.timer = null;

		this.dialogShowing = false;

		this.sendClick = false;
		this.clickRequestSuccess = false;
		this.shouldDismiss = false;
		this.seekTime = 0;

		this.mHandler = new Handler();

		this.mUpdateResults = new Runnable() {
			public void run() {
				MyTapjoyVideoView.this.overlayText.setText(""
						+ MyTapjoyVideoView.this.getRemainingVideoTime()
						+ " seconds");
			}
		};
	}

	public boolean onError(MediaPlayer mp, int what, int extra) {
		return true;
	}

	public void onCompletion(MediaPlayer mp) {
		TapjoyLog.i("VideoView", "onCompletion");
		if (this.timer != null) {
			this.timer.cancel();
		}
		showVideoCompletionScreen();
		if (!videoError) {
			TapjoyVideo.videoNotifierComplete();

			new Thread(new Runnable() {
				public void run() {
					if (MyTapjoyVideoView.this.clickRequestSuccess) {
						TapjoyConnectCore.getInstance().actionComplete(
								videoData.offerID);
					}
				}
			}).start();
		}
		videoError = false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
	}

}
