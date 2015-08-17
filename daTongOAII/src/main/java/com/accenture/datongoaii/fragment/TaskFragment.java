package com.accenture.datongoaii.fragment;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.network.HttpUtil;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

@SuppressWarnings("unused")
public class TaskFragment extends Fragment {
	private static final int HANDLER_TAG_REFRESH_VIEW = 0;
	
	private View layoutTask;
	private TextView tvResponse;
	private String url = "";
	private String auth = "";
	String res;
	private HttpEntity data = null;
	private FragmentHandler handler = new FragmentHandler(this);
	
	public static class FragmentHandler extends Handler {
		WeakReference<TaskFragment> theFragment;

		public FragmentHandler(TaskFragment TaskFragment) {
			this.theFragment = new WeakReference<TaskFragment>(TaskFragment);
		}

		@Override
		public void handleMessage(Message message) {
			TaskFragment mFragment = theFragment.get();
			switch (message.what) {
			case HANDLER_TAG_REFRESH_VIEW:
//				mFragment.tvResponse.setText(mFragment.res);
				break;
			}
		}

	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		layoutTask = inflater.inflate(R.layout.frag_task, container, false);
//		Button btnTest = (Button) layoutTask.findViewById(R.id.btnTest);
//		tvResponse = (TextView) layoutTask.findViewById(R.id.tvResponse);
//		url = "https://sandboxapp.cloopen.com:8883/2013-12-26/Accounts/8a48b5514ee36774014ef223f1590e89/SMS/TemplateSMS?sig="
//				+ getSP();
//		try {
//			JSONObject json = new JSONObject();
//			json.put("to", "18982135898");
//			json.put("appId", "aaf98f894ee35d30014ef229cd470ea3");
//			json.put("templateId", "1");
//			JSONArray array = new JSONArray("[\"字段测试\",\"字段测试\"]");
//			json.put("datas", array);
//			Logger.i("post data=", json.toString());
//			Logger.i("post url=", url);
//			data = new StringEntity(json.toString());
//		} catch (UnsupportedEncodingException e1) {
//			e1.printStackTrace();
//		} catch (JSONException e2) {
//			e2.printStackTrace();
//		}
//
//		auth = getAuth();
//		btnTest.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				new Thread() {
//					public void run() {
//						try {
//							res = HttpUtil.post(url, data, true,
//									TaskFragment.this.getActivity(), auth);
//							handler.sendEmptyMessage(HANDLER_TAG_REFRESH_VIEW);
////							try {
////								res = HttpUtil.get("https://github.com/", TaskFragment.this.getActivity());
////								handler.sendEmptyMessage(HANDLER_TAG_REFRESH_VIEW);
////							} catch (Throwable e) {
////								e.printStackTrace();
////							}
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				}.start();
//			}
//		});
		return layoutTask;
	}

//	private String getAuth() {
//		String str = "8a48b5514ee36774014ef223f1590e89:" + getTimeStamp();
//		return Utils.base64(str);
//	}
//
//	@SuppressLint("DefaultLocale")
//	private String getSP() {
//		String str = "8a48b5514ee36774014ef223f1590e8935ce7ffe98434f1db026bee7fa035c20";
//		return Utils.md5(str + getTimeStamp()).toUpperCase();
//	}
//
//	@SuppressLint("SimpleDateFormat")
//	private String getTimeStamp() {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//		return sdf.format(new Date(System.currentTimeMillis()));
//	}
}
