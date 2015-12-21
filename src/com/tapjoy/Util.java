package com.tapjoy;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;

public class Util {

	public static void showOffersWithCurrencyID(Context context,
			String currencyID, boolean enableCurrencySelector,
			TJEventData eventData, String callbackID,
			TapjoyOffersNotifier notifier) {

		String multipleCurrencySelector = enableCurrencySelector ? "1" : "0";

		HashMap offersURLParams = new HashMap(TapjoyConnectCore.getURLParams());

		TapjoyUtil.safePut(offersURLParams, "currency_id", currencyID, true);
		TapjoyUtil.safePut(offersURLParams, "currency_selector",
				multipleCurrencySelector, true);

		offersURLParams.putAll(TapjoyConnectCore.getVideoParams());

		Intent intent = new Intent(context, ManyVideoTJCOffersWebView.class);

		if (eventData != null) {
			TapjoyLog.i("TapjoyOffers", "showOffers for eventName: "
					+ eventData.name);
		}

		if ((callbackID != null) && (callbackID.length() > 0)) {
			intent.putExtra("callback_id", callbackID);
		}
		intent.putExtra("view_type", 2);
		intent.putExtra("tjevent", eventData);
		intent.putExtra("legacy_view", true);
		intent.putExtra("URL_PARAMS", offersURLParams);

		intent.setFlags(268435456);
		context.startActivity(intent);
	}

	/**
	 * 向指定URL发送GET方法的请求
	 * 
	 * @param url
	 *            发送请求的URL
	 * @param param
	 *            请求参数，请求参数应该是name1=value1&name2=value2的形式。
	 * @return URL所代表远程资源的响应
	 */
	public static String sendGet(String url) {
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			conn.setConnectTimeout(30 * 1000);
			conn.setReadTimeout(30 * 1000);
			// 建立实际的连接
			conn.connect();
			// 定义BufferedReader输入流来读取URL的响应
			result = InputStreamTOString(conn.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	public static String InputStreamTOString(InputStream in) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] data = new byte[2048];
		int count = -1;
		while ((count = in.read(data, 0, 2048)) != -1) {
			outStream.write(data, 0, count);
		}
		data = null;
		return new String(outStream.toByteArray(), "UTF-8");
	}

}
