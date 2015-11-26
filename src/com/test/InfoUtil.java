package com.test;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * 手机信息工具类
 * 
 * @author xiedezhi
 * 
 */
public class InfoUtil {
	private static Random sRandom = new Random();
	/**
	 * 默认盒子上网时间
	 */
	public static final int DEFAULT_INTERNET_TIME = 1800;

	private int Width = 0; // 屏幕寬
	private int Height = 0; // 屏幕高

	public InfoUtil(Context context) {
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(mDisplayMetrics);
		Width = mDisplayMetrics.widthPixels;
		Height = mDisplayMetrics.heightPixels;
	}

	public int getWidth() {
		return Width;
	}

	public void setWidth(int width) {
		Width = width;
	}

	public int getHeight() {
		return Height;
	}

	public void setHeight(int height) {
		Height = height;
	}

	/**
	 * 获取IMEI
	 */
	public static String getIMEI(Context context) {
		String imei = ((TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		return imei;
	}

	/**
	 * 获取IMSI
	 */
	public static String getIMSI(Context context) {
		TelephonyManager telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = telManager.getSubscriberId();
		return imsi;
	}

	/**
	 * 获取手机型号
	 * 
	 * @return
	 */
	public static String getModel() {
		return android.os.Build.MODEL;
	}

	/**
	 * 获取android版本号
	 */
	public static String getVersion() {
		return android.os.Build.VERSION.RELEASE;
	}

	/**
	 * 中国移动/中国联通
	 */
	public static String getSimOperatorName(Context context) {
		String imsi = getIMSI(context);
		if (imsi != null) {
			if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
				// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
				return "中国移动";
			} else if (imsi.startsWith("46001")) {
				return "中国联通";
			} else if (imsi.startsWith("46003")) {
				return "中国电信";
			}
		}
		return "";
	}

	/**
	 * 获取手机品牌
	 */
	public static String getManuFacturer() {
		return android.os.Build.BRAND;
	}

	/**
	 * 获取分辨率
	 */
	public static String getrResolution(Context context) {
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(mDisplayMetrics);
		int W = mDisplayMetrics.widthPixels;
		int H = mDisplayMetrics.heightPixels;
		return W + "*" + H;
	}

	/**
	 * 获取ISO
	 */
	public static String getISO(Context context) {
		TelephonyManager telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return telManager.getSimCountryIso();
	}

	public static String getON(Context context) {
		TelephonyManager telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return telManager.getNetworkOperatorName();
	}

	public static String getMobileCountryCode(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if ((telephonyManager.getNetworkOperator() != null)
				&& ((telephonyManager.getNetworkOperator().length() == 5) || (telephonyManager
						.getNetworkOperator().length() == 6))) {
			return telephonyManager.getNetworkOperator().substring(0, 3);
		}
		return null;
	}

	public static String getDPI(Context context) {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context
				.getSystemService("window");
		windowManager.getDefaultDisplay().getMetrics(metrics);
		return "" + metrics.densityDpi;
	}

	public static String getScreenLayoutSize(Context context) {
		Configuration configuration = context.getResources().getConfiguration();
		return (configuration.screenLayout & 0xF) + "";
	}

	public static String getMobileNetworkCode(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if ((telephonyManager.getNetworkOperator() != null)
				&& ((telephonyManager.getNetworkOperator().length() == 5) || (telephonyManager
						.getNetworkOperator().length() == 6))) {
			return telephonyManager.getNetworkOperator().substring(3);
		}
		return null;
	}

	/**
	 * 获取手机mac地址
	 */
	public static String getLocalMacAddress(Context context) {
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

	/**
	 * 获取本应用版本号
	 */
	public static String getVersionName(Context context) {
		try {
			// 获取packagemanager的实例
			PackageManager packageManager = context.getPackageManager();
			// getPackageName()是你当前类的包名，0代表是获取版本信息
			PackageInfo packInfo = packageManager.getPackageInfo(
					context.getPackageName(), 0);
			String version = packInfo.versionName;
			return version;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 
	 * @return 是否有活动的网络连接
	 */
	public static final boolean hasNetWorkConnection(Context context) {
		// 获取连接活动管理器
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// 获取链接网络信息
		final NetworkInfo networkInfo = connectivityManager
				.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isAvailable());
	}

	/**
	 * @return 返回boolean ,是否为wifi网络
	 * 
	 */
	public static final boolean hasWifiConnection(Context context) {
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		// 是否有网络并且已经连接
		return (networkInfo != null && networkInfo.isConnected());
	}

	/**
	 * @return 返回boolean,判断网络是否可用,是否为移动网络
	 */
	public static final boolean hasGPRSConnection(Context context) {
		// 获取活动连接管理器
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		return (networkInfo != null && networkInfo.isAvailable());
	}

	/**
	 * @return 
	 *         判断网络是否可用，并返回网络类型，ConnectivityManager.TYPE_WIFI，ConnectivityManager
	 *         .TYPE_MOBILE，不可用返回-1
	 */
	public static final int getNetWorkConnectionType(Context context) {
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo wifiNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final NetworkInfo mobileNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifiNetworkInfo != null && wifiNetworkInfo.isAvailable()) {
			return ConnectivityManager.TYPE_WIFI;
		} else if (mobileNetworkInfo != null && mobileNetworkInfo.isAvailable()) {
			return ConnectivityManager.TYPE_MOBILE;
		} else {
			return -1;
		}
	}

	/**
	 * 获取当前连接的WIFI名字
	 */
	public static final String getCurWifiName(Context context) {
		if (!hasWifiConnection(context)) {
			return "";
		}
		WifiManager mWifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = mWifi.getConnectionInfo();
		return wifiInfo.getSSID() == null ? "" : wifiInfo.getSSID();
	}

	/**
	 * 获取RAM大小
	 */
	public static final long getTotalRAM() {
		RandomAccessFile reader = null;
		String load = null;
		double totRam = 0;
		try {
			reader = new RandomAccessFile("/proc/meminfo", "r");
			load = reader.readLine();

			// Get the Number value from the string
			Pattern p = Pattern.compile("(\\d+)");
			Matcher m = p.matcher(load);
			String value = "";
			while (m.find()) {
				value = m.group(1);
			}
			reader.close();
			totRam = Double.parseDouble(value);
			long ret = (long) (totRam + 0.5);
			return ret;
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	public static String getDeviceInfo(Context context) {
		try {
			org.json.JSONObject json = new org.json.JSONObject();
			android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			String device_id = tm.getDeviceId();

			android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);

			String mac = wifi.getConnectionInfo().getMacAddress();
			json.put("mac", mac);

			if (TextUtils.isEmpty(device_id)) {
				device_id = mac;
			}

			if (TextUtils.isEmpty(device_id)) {
				device_id = android.provider.Settings.Secure.getString(
						context.getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);
			}

			json.put("device_id", device_id);

			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static File createNewFile(String path, boolean append) {
		File newFile = new File(path);
		if (!append) {
			if (newFile.exists()) {
				newFile.delete();
			}
		}
		if (!newFile.exists()) {
			try {
				File parent = newFile.getParentFile();
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}
				newFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return newFile;
	}

	/**
	 * 保存数据到指定文件
	 */
	public static boolean saveByteToFile(final byte[] byteData,
			final String filePathName) {
		boolean result = false;
		try {
			File newFile = createNewFile(filePathName, false);
			FileOutputStream fileOutputStream = new FileOutputStream(newFile);
			fileOutputStream.write(byteData);
			fileOutputStream.flush();
			fileOutputStream.close();
			result = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 读取文件数据
	 */
	public static byte[] getByteFromFile(final String filePathName) {
		byte[] bs = null;
		try {
			File newFile = new File(filePathName);
			FileInputStream fileInputStream = new FileInputStream(newFile);
			DataInputStream dataInputStream = new DataInputStream(
					fileInputStream);
			BufferedInputStream inPutStream = new BufferedInputStream(
					dataInputStream);
			bs = new byte[(int) newFile.length()];
			inPutStream.read(bs);
			fileInputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bs;
	}

	public static void chmod(File file) {
		try {
			String command = "chmod 777 " + file.getAbsolutePath();
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(command);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static String getAndroidID() {
		return getRandom(16);
	}

	public static String getRandom(int count) {
		String ret = "";
		for (int i = 0; i < count; i++) {
			int yu = Math.abs(sRandom.nextInt()) % 11;
			if (yu == 1) {
				ret = ret + ((char) ((Math.abs(sRandom.nextInt()) % 10) + 'a'));
			} else {
				ret = ret + (Math.abs(sRandom.nextInt()) % 10);
			}
		}
		return ret;
	}

}
