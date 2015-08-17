package com.accenture.datongoaii.network;

import com.accenture.datongoaii.activity.MainActivity;
import com.accenture.datongoaii.util.Constants;
import com.accenture.datongoaii.util.Logger;
import com.igexin.sdk.PushConsts;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class PushReceiver extends BroadcastReceiver {
	private static final String TAG = "PushReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Logger.d(TAG,
				"onReceive() action=" + bundle.getInt("action"));
		switch (bundle.getInt(PushConsts.CMD_ACTION)) {
		case PushConsts.GET_MSG_DATA:
			// 获取透传数据
			byte[] payload = bundle.getByteArray("payload");
			if (payload != null) {
				String data = new String(payload);
				Logger.d("GetuiSdkDemo", "receiver payload : " + data);
				Intent main = new Intent(context, MainActivity.class);
				main.putExtra(Constants.PUSH_DATA_TAG, data);
				main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(main);
			}
			break;
		}
	}
}
