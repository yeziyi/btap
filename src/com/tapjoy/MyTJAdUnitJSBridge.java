package com.tapjoy;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

public class MyTJAdUnitJSBridge extends TJAdUnitJSBridge {
	private Context context;

	public MyTJAdUnitJSBridge(Context c, WebView w, TJEventData e) {
		super(c, w, e);
		context = c;
	}

	@Override
	public void displayVideo(JSONObject json, String callbackID) {
		Toast.makeText(context, "displayVideo", Toast.LENGTH_SHORT).show();
		Log.e("test", "displayVideo json = " + json + "  callbackID = "
				+ callbackID);
		String url = "";
		String cancelMessage = "";
		try {
			cancelMessage = json.getString("cancelMessage");
		} catch (Exception e) {
			TapjoyLog.w("TJAdUnitJSBridge", "no cancelMessage");
		}
		try {
			url = json.getString("url");
			Intent videoIntent = new Intent(this.context,
					MyTapjoyVideoView.class);
			videoIntent.putExtra("VIDEO_URL", url);
			videoIntent.putExtra("VIDEO_CANCEL_MESSAGE", cancelMessage);
			videoIntent.putExtra("VIDEO_SHOULD_DISMISS", true);
			videoIntent.putExtra("callback_id", callbackID);
			((Activity) this.context).startActivityForResult(videoIntent, 0);
		} catch (Exception e) {
			Toast.makeText(context,
					"displayVideo Exception e = " + e.toString(),
					Toast.LENGTH_SHORT).show();
			Log.e("test", "displayVideo Exception e = " + e.toString());
			invokeJSCallback(callbackID, new Object[] { Boolean.FALSE });
			e.printStackTrace();
		}
	}

}
