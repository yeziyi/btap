package com.test;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;

import com.jiubang.rootcheck.R;
import com.tapjoy.TapjoyConnect;
import com.tapjoy.TapjoyNotifier;
import com.tapjoy.Util;
import com.umeng.analytics.MobclickAgent;

public class MainActivity extends Activity {

	public static boolean mIsActive = false;

	private Handler mHandler = new Handler();

	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			mHandler.removeCallbacks(this);
			if (!mIsActive) {
				return;
			}
			TapjoyConnect.getTapjoyConnectInstance().getTapPoints(
					new TapjoyNotifier() {

						@Override
						public void getUpdatePointsFailed(String arg0) {
						}

						@Override
						public void getUpdatePoints(String arg0, final int arg1) {
							if (arg1 > 0) {
								Log.e("", "获取积分" + arg1);
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										Toast.makeText(getApplicationContext(),
												"获取积分" + arg1,
												Toast.LENGTH_SHORT).show();
									}
								});
							}
						}
					});
			mHandler.postDelayed(this, 3000);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.test);

		mIsActive = true;

		findViewById(R.id.btn).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Util.showOffersWithCurrencyID(v.getContext(), null, false,
						null, null, null);
			}
		});
		try {
			Settings.Secure.putString(getContentResolver(),
					android.provider.Settings.Secure.ANDROID_ID,
					InfoUtil.getAndroidID());
		} catch (Throwable e) {
		}
		// Settings.Secure.putString(getContentResolver(),
		// android.provider.Settings.System.WIFI_STATIC_DNS1,
		// "211.155.23.88");
		// Settings.Secure.putString(getContentResolver(),
		// android.provider.Settings.System.WIFI_STATIC_DNS2,
		// "211.162.62.2");
		// Settings.Secure.putString(getContentResolver(), "default_dns_server",
		// "211.155.23.88");

		TapjoyConnect.requestTapjoyConnect(this,
				"45115e1c-423e-4b34-9cc6-be048ba4c12f", "GltP4s9SfMs9k33KCEPe");

		// Log.e("",
		// "default_dns_server = "
		// + android.provider.Settings.Secure.getString(
		// getContentResolver(), "default_dns_server"));
		// Log.e("",
		// "WIFI_STATIC_DNS1 = "
		// + android.provider.Settings.Secure
		// .getString(
		// getContentResolver(),
		// android.provider.Settings.System.WIFI_STATIC_DNS1));
		// Log.e("",
		// "WIFI_STATIC_DNS2 = "
		// + android.provider.Settings.Secure
		// .getString(
		// getContentResolver(),
		// android.provider.Settings.System.WIFI_STATIC_DNS2));
		Log.e("",
				"androidid = "
						+ android.provider.Settings.Secure.getString(
								getContentResolver(),
								android.provider.Settings.Secure.ANDROID_ID));
		Log.e("", "imei = " + InfoUtil.getIMEI(getApplicationContext()));
		Log.e("", "imsi = " + InfoUtil.getIMSI(getApplicationContext()));
		Log.e("", "model = " + android.os.Build.MODEL);
		Log.e("", "manufacturer = " + android.os.Build.MANUFACTURER);
		Log.e("", "version = " + android.os.Build.VERSION.RELEASE);
		Log.e("", "brand = " + android.os.Build.BRAND);
		Log.e("", "country = " + Locale.getDefault().getCountry());
		Log.e("", "language = " + Locale.getDefault().getLanguage());
		Log.e("",
				"carrierCountryCode = "
						+ InfoUtil.getISO(getApplicationContext()));
		Log.e("", "carrierName = " + InfoUtil.getON(getApplicationContext()));
		Log.e("",
				"MobileCountryCode = "
						+ InfoUtil
								.getMobileCountryCode(getApplicationContext()));
		Log.e("",
				"MobileNetworkCode = "
						+ InfoUtil
								.getMobileNetworkCode(getApplicationContext()));
		Log.e("",
				"deviceScreenDensity = "
						+ InfoUtil.getDPI(getApplicationContext()));
		Log.e("",
				"ScreenLayoutSize = "
						+ InfoUtil.getScreenLayoutSize(getApplicationContext()));

		mHandler.post(mRunnable);

		MobclickAgent.updateOnlineConfig(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		TapjoyConnect.getTapjoyConnectInstance().appPause();
		MobclickAgent.onPageEnd("MyTJCOffersWebView");
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		TapjoyConnect.getTapjoyConnectInstance().appResume();
		findViewById(R.id.btn).postDelayed(new Runnable() {

			@Override
			public void run() {
				Util.showOffersWithCurrencyID(MainActivity.this, null, false,
						null, null, null);
			}
		}, 100);
		MobclickAgent.onPageStart("MyTJCOffersWebView");
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onDestroy() {
		mIsActive = false;
		super.onDestroy();
	}
}
