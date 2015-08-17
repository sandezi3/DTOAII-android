package com.accenture.datongoaii.network;

import java.security.KeyStore;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * 网络连接工具
 */
public class HttpUtil {
	private static final String TAG = "HttpUtil";
	private static HttpParams parameters = new BasicHttpParams();
	private static SchemeRegistry schemeRegistry = new SchemeRegistry();
	// private static ClientConnectionManager cm = new
	// ThreadSafeClientConnManager(parameters, schemeRegistry);
	// private static HttpClient httpClient = new DefaultHttpClient(cm,
	// parameters);
	public static final int NET_CONNECT_EXCEPTION = 10;

	/**
	 * static块，用于初始化网络连接变量httpClient
	 */
	static {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			X509HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
			// http scheme
			schemeRegistry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			SSLSocketFactory socketFactory = new SSLSocketFactoryEx(trustStore);
			socketFactory.setHostnameVerifier(hostnameVerifier);
			// https scheme
			// schemeRegistry.register(new Scheme("https", socketFactory, 443));
			schemeRegistry.register(new Scheme("https", socketFactory, 8883));
			parameters = new BasicHttpParams();
			parameters.setParameter("charset", HTTP.UTF_8);
			HttpConnectionParams.setConnectionTimeout(parameters, 60000);
			HttpConnectionParams.setSoTimeout(parameters, 60000);
			// cm = new ThreadSafeClientConnManager(parameters, schemeRegistry);
		} catch (Exception e) {
			String err = (e.getMessage() == null) ? "socket identityValidate failed"
					: e.getMessage();
			Log.e(TAG, err);
		}

	}

	/**
	 * post方式联网
	 * 
	 * @param url
	 * @param data
	 * @return 服务器返回的字符串数据
	 * @throws Exception
	 */
	public static String post(String url, HttpEntity data, boolean isJsonType,
			Context context, String authorization) throws Exception {
		if (!HttpUtil.isNetConnected(context)) {
			throw new Exception();
		}
		String response = "";
		HttpPost httpPost = new HttpPost(url);
		DefaultHttpClient httpClient = null;
		try {
			if (isJsonType) {
				httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
			} else {
				httpPost.setHeader("content-type",
						"application/x-www-form-urlencoded");
			}
			httpPost.setHeader("Authorization", authorization);
			httpPost.setHeader("Content-Lenght", "139");
			httpPost.setHeader("Accept", "application/json");
			httpPost.setEntity(data);

			ClientConnectionManager cm = new ThreadSafeClientConnManager(
					parameters, schemeRegistry);
			httpClient = new DefaultHttpClient(cm, parameters);
			HttpResponse httpResponse = httpClient.execute(httpPost);

			int code = httpResponse.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_OK) {
				response = EntityUtils.toString(httpResponse.getEntity(),
						"utf-8");
			} else {
				throw new Exception();
			}
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}

		return response;
	}

	/*
	 * 非埃森哲接口，pass系统接口
	 */

	public static String paasSysPost(String url, HttpEntity data,
			Context context) throws Exception, Exception {
		String response = "";
		if (!HttpUtil.isNetConnected(context)) {
			throw new Exception();
		}
		HttpPost httpPost = new HttpPost(url);
		DefaultHttpClient httpClient = null;
		try {

			httpPost.setHeader("content-type",
					"application/x-www-form-urlencoded;charset=utf-8");
			httpPost.setHeader("Accept-Language", HTTP.UTF_8);
			httpPost.setEntity(data);

			HttpParams httpParams = new BasicHttpParams();
			httpParams.removeParameter("Expect");

			httpClient = new DefaultHttpClient(httpParams);
			httpClient.getParams().setParameter(
					"http.protocol.content-charset", "UTF-8");
			// 请求超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
			// 读取超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, 30000);
			HttpResponse httpResponse = httpClient.execute(httpPost);

			int code = httpResponse.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_OK) {
				response = EntityUtils.toString(httpResponse.getEntity(),
						"utf-8");
			} else {
				throw new Exception();
			}
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
		return response;
	}

	/**
	 * get方式联网
	 * 
	 * @param url
	 * @param data
	 * @return 服务器返回的字符串数据
	 * @throws Exception
	 */
	public static String get(String url, Context context) throws Throwable {

		if (!HttpUtil.isNetConnected(context)) {
			throw new Exception("网络未连接，请检查");
		}
		String response = "";
		HttpGet httpGet = new HttpGet(url);
		HttpClient httpClient = null;
		try {
			httpGet.setHeader("content-type",
					"application/x-www-form-urlencoded");
			httpGet.setHeader("Accept-Language", HTTP.UTF_8);

			ClientConnectionManager cm = new ThreadSafeClientConnManager(
					parameters, schemeRegistry);
			httpClient = new DefaultHttpClient(cm, parameters);
			HttpResponse httpResponse = httpClient.execute(httpGet);

			int code = httpResponse.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_OK) {
				HttpEntity entity = httpResponse.getEntity();
				response = EntityUtils.toString(entity, "utf-8");
			} else {
				throw new Exception("获取信息失败，请确认接口已正确部署");
			}
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
			if (httpGet != null)
				httpGet.abort();
		}
		return response;
	}

	/**
	 * 手机连接是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetConnected(Context context) {
		ConnectivityManager cManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cManager.getActiveNetworkInfo();
		if (info != null && info.isAvailable()) {

			// 网络可用
			/** 是手机自带的联网方式 */
			if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
				return true;
			}
			/** WIFI或其它联网方式 */
			else {
				return true;
			}
		} else {
			// 不能联网
			return false;
		}
	}

}
