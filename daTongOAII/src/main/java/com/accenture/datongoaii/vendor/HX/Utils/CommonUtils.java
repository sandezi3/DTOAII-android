/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.accenture.datongoaii.vendor.HX.Utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.DTOAIIApplication;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.vendor.HX.Constant;
import com.accenture.datongoaii.vendor.HX.receiver.NewCMDMessageBroadcastReceiver;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.util.EMLog;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class CommonUtils {
    private static final String TAG = "CommonUtils";

    /**
     * 检测网络是否可用
     *
     * @param context Context
     * @return boolean
     */
    public static boolean isNetWorkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }

        return false;
    }

    /**
     * 检测Sdcard是否存在
     *
     * @return boolean
     */
    public static boolean isExitsSdcard() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }


    /**
     * 根据消息内容和消息类型获取消息内容提示
     *
     * @param message EMMessage
     * @param context Context
     * @return String
     */
    public static String getMessageDigest(EMMessage message, Context context) {
        String digest = "";
        switch (message.getType()) {
//        case LOCATION: // 位置消息
//            if (message.direct == EMMessage.Direct.RECEIVE) {
//                //从sdk中提到了ui中，使用更简单不犯错的获取string方法
////              digest = EasyUtils.getAppResourceString(context, "location_recv");
//                digest = "[%1$s的位置]";
//                digest = String.format(digest, message.getFrom());
//                return digest;
//            } else {
////              digest = EasyUtils.getAppResourceString(context, "location_prefix");
//                digest = getString(context, R.string.location_prefix);
//            }
//            break;
            case IMAGE: // 图片消息
                digest = Constant.MESSAGE_ATTR_IS_PICTURE;
                break;
            case VOICE:// 语音消息
                digest = Constant.MESSAGE_ATTR_IS_VOICE;
                break;
//        case VIDEO: // 视频消息
//            digest = getString(context, R.string.video);
//            break;
            case TXT: // 文本消息
                if (!message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                    TextMessageBody txtBody = (TextMessageBody) message.getBody();
                    digest = txtBody.getMessage();
                }
//            else{
//                TextMessageBody txtBody = (TextMessageBody) message.getBody();
//                digest = getString(context, R.string.voice_call) + txtBody.getMessage();
//            }
                break;
//        case FILE: //普通文件消息
//            digest = getString(context, R.string.file);
//            break;
            default:
                EMLog.e(TAG, "error, unknow type");
                return "";
        }

        return digest;
    }

    static String getString(Context context, int resId) {
        return context.getResources().getString(resId);
    }


    public static String getTopActivity(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

        if (runningTaskInfos != null)
            return runningTaskInfos.get(0).topActivity.getClassName();
        else
            return "";
    }

    public static boolean handleEventSkip(EMNotifierEvent event) {
        if (event.getData() instanceof EMMessage) {
            EMMessage message = (EMMessage) event.getData();
            if (message.getFrom().equals("admin")) {
                if (message.getType().equals(EMMessage.Type.TXT)) {
                    String msg = ((TextMessageBody) message.getBody()).getMessage();
                    if (msg.startsWith(Constants.BROADCAST_CMD_PREFIX_TAG)) {
                        return true;
                    }
                }
            }
        } else if (event.getData() instanceof ArrayList) {
            for (int i = 0; i < ((ArrayList) event.getData()).size(); i++) {
                if (!(((ArrayList) event.getData()).get(i) instanceof EMMessage)) {
                    return false;
                }
                EMMessage message = (EMMessage) ((ArrayList) event.getData()).get(i);
                if (!message.getFrom().equals("admin")) {
                    return false;
                }
                if (!message.getType().equals(EMMessage.Type.TXT)) {
                    return false;
                }
                String msg = ((TextMessageBody) message.getBody()).getMessage();
                if (!msg.startsWith(Constants.BROADCAST_CMD_PREFIX_TAG)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean handleEvent(EMNotifierEvent event) {
        if (event.getData() instanceof EMMessage) {
            EMMessage message = (EMMessage) event.getData();
            Logger.d(TAG, "receive the event : " + event.getEvent() + ",id : " + message.getMsgId());
            if (handleMessage(message)) {
                return true;
            }
        } else if (event.getData() instanceof ArrayList) {
            for (int i = 0; i < ((ArrayList) event.getData()).size(); i++) {
                if (!(((ArrayList) event.getData()).get(i) instanceof EMMessage)) {
                    return false;
                }
                EMMessage message = (EMMessage) ((ArrayList) event.getData()).get(i);
                if (!handleMessage(message)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean handleMessage(EMMessage message) {
        if (message.getFrom().equals("admin")) {
            if (message.getType().equals(EMMessage.Type.TXT)) {
                String msg = ((TextMessageBody) message.getBody()).getMessage();
                if (msg.startsWith(Constants.BROADCAST_CMD_PREFIX_TAG)) {
                    String action = msg.substring(Constants.BROADCAST_CMD_PREFIX_TAG.length());
                    final String CMD_TOAST_BROADCAST = "com.accenture.dtoaii.im.cmd.toast";
                    IntentFilter cmdFilter = new IntentFilter(CMD_TOAST_BROADCAST);

                    NewCMDMessageBroadcastReceiver broadCastReceiver = new NewCMDMessageBroadcastReceiver();
                    //注册广播接收者
                    DTOAIIApplication.applicationContext.registerReceiver(broadCastReceiver, cmdFilter);
                    Intent broadcastIntent = new Intent(CMD_TOAST_BROADCAST);
                    broadcastIntent.putExtra(Constants.BROADCAST_CMD, action);
                    DTOAIIApplication.applicationContext.sendBroadcast(broadcastIntent, null);
                    EMConversation conversation = EMChatManager.getInstance().getConversation(message.getFrom());
                    conversation.removeMessage(message.getMsgId());
                    return true;
                }
            }
        }
        return false;
    }
}
