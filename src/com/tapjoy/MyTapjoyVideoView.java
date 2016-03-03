package com.tapjoy;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class MyTapjoyVideoView extends Activity implements
		MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
		MediaPlayer.OnPreparedListener {
	private VideoView videoView;
	private TextView overlayText;
	private String videoURL;
	private String webviewURL;
	private String cancelMessage;
	private String connectivityMessage;
	private RelativeLayout relativeLayout;
	private Bitmap watermark;
	private TapjoyVideoBroadcastReceiver videoBroadcastReceiver;
	Dialog dialog;
	Timer timer;
	private static boolean videoError = false;
	private static boolean streamingVideo = false;
	private static TapjoyVideoObject videoData;
	private boolean dialogShowing;
	private static final String BUNDLE_DIALOG_SHOWING = "dialog_showing";
	private static final String BUNDLE_SEEK_TIME = "seek_time";
	private boolean sendClick;
	private boolean clickRequestSuccess;
	private boolean allowBackKey;
	private boolean shouldDismiss;
	private int timeRemaining;
	private int seekTime;
	private static final int DIALOG_WARNING_ID = 0;
	private static final int DIALOG_CONNECTIVITY_LOST_ID = 1;
	private static final String videoWillResumeText = "";
	private static final String videoSecondsText = " seconds";
	private ImageView tapjoyImage;
	private static final String TAG = "VideoView";
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
			if (extras.containsKey("VIDEO_CANCEL_MESSAGE")) {
				this.cancelMessage = extras.getString("VIDEO_CANCEL_MESSAGE");
			}
			if (extras.containsKey("VIDEO_SHOULD_DISMISS")) {
				this.shouldDismiss = extras.getBoolean("VIDEO_SHOULD_DISMISS");
			}
		}
		TapjoyLog.i("VideoView", "dialogShowing: " + this.dialogShowing
				+ ", seekTime: " + this.seekTime);
		if (videoData != null) {
			this.sendClick = true;
			streamingVideo = false;
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
				streamingVideo = true;
			}
			TapjoyLog.i("VideoView", "videoPath: " + this.videoURL);
		} else if (this.videoURL != null) {
			streamingVideo = true;
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
		this.videoBroadcastReceiver = new TapjoyVideoBroadcastReceiver();
		registerReceiver(this.videoBroadcastReceiver, new IntentFilter(
				"android.net.conn.CONNECTIVITY_CHANGE"));

		initVideoView();

		TapjoyLog.i("VideoView", "onCreate DONE");

		TapjoyConnectCore.viewDidOpen(3);

		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				onCompletion(null);
			}
		}, 40 * 1000);
	}

	protected void onPause() {
		super.onPause();
		if (this.videoView.isPlaying()) {
			TapjoyLog.i("VideoView", "onPause");

			this.videoView.pause();
			this.seekTime = this.videoView.getCurrentPosition();
			TapjoyLog.i("VideoView", "seekTime: " + this.seekTime);
		}
	}

	protected void onResume() {
		TapjoyLog.i("VideoView", "onResume");
		super.onResume();

		setRequestedOrientation(0);
		if (this.seekTime > 0) {
			TapjoyLog.i("VideoView", "seekTime: " + this.seekTime);

			this.videoView.seekTo(this.seekTime);
			if ((!this.dialogShowing) || (this.dialog == null)
					|| (!this.dialog.isShowing())) {
				this.videoView.start();
			}
		}
	}

	protected void onDestroy() {
		super.onDestroy();
		if (isFinishing()) {
			unregisterReceiver(this.videoBroadcastReceiver);
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

	public void onWindowFocusChanged(boolean hasFocus) {
		TapjoyLog.i("VideoView", "onWindowFocusChanged");
		super.onWindowFocusChanged(hasFocus);
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
		if ((this.videoView == null) && (this.overlayText == null)) {
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

			this.videoView = new VideoView(this);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
					-1, -1);
			layoutParams.addRule(13);
			this.videoView.setLayoutParams(layoutParams);

			this.overlayText = new TextView(this);
			this.overlayText.setTextSize(textSize);
			this.overlayText.setTypeface(Typeface.create("default", 1), 1);

			RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
					-2, -2);
			textParams.addRule(12);
			this.overlayText.setLayoutParams(textParams);
		}

		this.relativeLayout.addView(this.videoView);
		this.relativeLayout.addView(this.tapjoyImage);
		this.relativeLayout.addView(this.overlayText);
		videoView.requestFocus();
		if (streamingVideo) {
			Log.e("test", "streaming video: " + this.videoURL);
			this.videoView.setVideoURI(Uri.parse(this.videoURL));
		} else {
			Log.e("test", "cached video: " + this.videoURL);
			this.videoView.setVideoPath(this.videoURL);
		}
		this.videoView.setOnCompletionListener(this);
		this.videoView.setOnErrorListener(this);
		this.videoView.setOnPreparedListener(this);
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				MyTapjoyVideoView.this.timeRemaining = (MyTapjoyVideoView.this.videoView
						.getDuration() / 1000);

				Log.e("test", "videoView.getDuration(): "
						+ MyTapjoyVideoView.this.videoView.getDuration());
				Log.e("test", "timeRemaining: "
						+ MyTapjoyVideoView.this.timeRemaining);

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
			this.videoView.seekTo(this.seekTime);
			TapjoyLog.i("VideoView", "dialog is showing -- don't start");
		} else {
			TapjoyLog.i("VideoView", "start");
			this.videoView.seekTo(0);
			this.videoView.start();

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
		int timeRemaining = (this.videoView.getDuration() - this.videoView
				.getCurrentPosition()) / 1000;
		if (timeRemaining < 0) {
			timeRemaining = 0;
		}
		return timeRemaining;
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
		this.videoView = null;
		this.overlayText = null;
		this.videoURL = null;
		this.webviewURL = null;
		this.cancelMessage = "Currency will not be awarded, are you sure you want to cancel the video?";
		this.connectivityMessage = "A network connection is necessary to view videos. You will be able to complete the offer and receive your reward on the next connect.";

		this.timer = null;

		this.dialogShowing = false;

		this.sendClick = false;
		this.clickRequestSuccess = false;
		this.allowBackKey = false;
		this.shouldDismiss = false;
		this.timeRemaining = 0;
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

	public void onPrepared(MediaPlayer mp) {
		TapjoyLog.i("VideoView", "onPrepared");
		Log.e("test", "VideoView onPrepared");
	}

	public boolean onError(MediaPlayer mp, int what, int extra) {
//		videoError = true;
//		Log.e("test", "VideoView " + "onError, what: " + what + " extra: "
//				+ extra);
//		TapjoyLog.i("VideoView", "onError, what: " + what + "extra: " + extra);
//
//		TapjoyVideo.videoNotifierError(3);
//
//		this.allowBackKey = true;
//		if (this.timer != null) {
//			this.timer.cancel();
//		}
//		if ((what == 1) && (extra == -1004)) {
//			return true;
//		}
		
            switch (what){
                case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                    Log.e("test", "unknown media playback error");
                    break;
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    Log.e("test", "server connection died");
                default:
                    Log.e("test", "generic audio playback error");
                    break;
            }

            switch (extra){
                case MediaPlayer.MEDIA_ERROR_IO:
                    Log.e("test", "IO media error");
                    break;
                case MediaPlayer.MEDIA_ERROR_MALFORMED:
                    Log.e("test", "media error, malformed");
                    break;
                case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                    Log.e("test", "unsupported media content");
                    break;
                case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                    Log.e("test", "media timeout error");
                    break;
                default:
                    Log.e("test", "unknown playback error");
                    break;
            }


        return true;
//		return false;
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
		this.allowBackKey = true;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == 4) {
			if ((!this.allowBackKey) && (this.cancelMessage != null)
					&& (this.cancelMessage.length() > 0)) {
				this.seekTime = this.videoView.getCurrentPosition();
				this.videoView.pause();

				this.dialogShowing = true;
				showDialog(0);

				TapjoyLog.i("VideoView", "PAUSE VIDEO time: " + this.seekTime);
				TapjoyLog.i(
						"VideoView",
						"currentPosition: "
								+ this.videoView.getCurrentPosition());
				TapjoyLog
						.i("VideoView",
								"duration: "
										+ this.videoView.getDuration()
										+ ", elapsed: "
										+ (this.videoView.getDuration() - this.videoView
												.getCurrentPosition()));
				return true;
			}
			if (this.videoView.isPlaying()) {
				this.videoView.stopPlayback();
				showVideoCompletionScreen();
				if (this.timer != null) {
					this.timer.cancel();
				}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	protected Dialog onCreateDialog(int id) {
		TapjoyLog.i("VideoView", "dialog onCreateDialog");
		if (!this.dialogShowing) {
			return this.dialog;
		}
		switch (id) {
		case 0:
			this.dialog = new AlertDialog.Builder(this)
					.setTitle("Cancel Video?")
					.setMessage(this.cancelMessage)
					.setNegativeButton("End",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									MyTapjoyVideoView.this
											.finishWithResult(false);
								}
							})
					.setPositiveButton("Resume",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.dismiss();

									MyTapjoyVideoView.this.videoView
											.seekTo(MyTapjoyVideoView.this.seekTime);
									MyTapjoyVideoView.this.videoView.start();

									MyTapjoyVideoView.this.dialogShowing = false;

									TapjoyLog
											.i("VideoView",
													"RESUME VIDEO time: "
															+ MyTapjoyVideoView.this.seekTime);
									TapjoyLog
											.i("VideoView",
													"currentPosition: "
															+ MyTapjoyVideoView.this.videoView
																	.getCurrentPosition());
									TapjoyLog.i(
											"VideoView",
											"duration: "
													+ MyTapjoyVideoView.this.videoView
															.getDuration()
													+ ", elapsed: "
													+ (MyTapjoyVideoView.this.videoView
															.getDuration() - MyTapjoyVideoView.this.videoView
															.getCurrentPosition()));
								}
							}).create();
			this.dialog
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						public void onCancel(DialogInterface dialog) {
							TapjoyLog.i("VideoView", "dialog onCancel");

							dialog.dismiss();
							MyTapjoyVideoView.this.videoView
									.seekTo(MyTapjoyVideoView.this.seekTime);
							MyTapjoyVideoView.this.videoView.start();

							MyTapjoyVideoView.this.dialogShowing = false;
						}
					});
			this.dialog.show();
			this.dialogShowing = true;
			break;
		case 1:
			this.dialog = new AlertDialog.Builder(this)
					.setTitle("Network Connection Lost")
					.setMessage(this.connectivityMessage)
					.setPositiveButton("Okay",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.dismiss();
									MyTapjoyVideoView.this.dialogShowing = false;
									MyTapjoyVideoView.this
											.finishWithResult(false);
								}
							}).create();
			this.dialog
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						public void onCancel(DialogInterface dialog) {
							TapjoyLog.i("VideoView", "dialog onCancel");
							dialog.dismiss();
							MyTapjoyVideoView.this.dialogShowing = false;
							MyTapjoyVideoView.this.finishWithResult(false);
						}
					});
			this.dialog.show();
			this.dialogShowing = true;
			break;
		default:
			this.dialog = null;
		}
		return this.dialog;
	}

	private class TapjoyVideoBroadcastReceiver extends BroadcastReceiver {
		private TapjoyVideoBroadcastReceiver() {
		}

		public void onReceive(Context context, Intent intent) {
			boolean noConnectivity = intent.getBooleanExtra("noConnectivity",
					false);
			if (noConnectivity) {
				MyTapjoyVideoView.this.videoView.pause();
				MyTapjoyVideoView.this.dialogShowing = true;
				MyTapjoyVideoView.this.showDialog(1);
				TapjoyLog.i("VideoView",
						"No network connectivity during video playback");
			}
		}
	}
}
