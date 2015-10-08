package com.accenture.datongoaii.vendor.HX;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.vendor.HX.receiver.DeliveryAckMessageReceiver;
import com.accenture.datongoaii.vendor.HX.receiver.NewMessageBroadcastReceiver;
import com.easemob.EMCallBack;
import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.OnMessageNotifyListener;
import com.easemob.chat.OnNotificationClickListener;
import com.easemob.util.EasyUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by leon on 9/25/15.
 * 环信方法controller
 */
public class HXController {
    private static final String TAG = "HXController";
    private static final String PWD = "123456";
    public Context context;
    public static HXController instance;
    public boolean sdkInited;
    protected EMEventListener eventListener;
    public HXNotifier notifier;

    /**
     * 用来记录foreground Activity
     */
    private List<Activity> activityList = new ArrayList<Activity>();

    public HXController() {
        instance = this;
    }

    public synchronized boolean onInit(Context context) {
        if (sdkInited) {
            return true;
        }

        this.context = context;

        // 初始化环信SDK
        EMChat.getInstance().init(context);
        /**
         * debugMode == true 时为打开，sdk 会在log里输入调试信息
         * @param debugMode
         * 在做代码混淆的时候需要设置成false
         */
        EMChat.getInstance().setDebugMode(true);
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);

        // 如果app启用了远程的service，此application:onCreate会被调用2次
        // 为了防止环信SDK被初始化2次，加此判断会保证SDK被初始化1次
        // 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process name就立即返回
        if (processAppName == null || !processAppName.equalsIgnoreCase("com.accenture.datongoaii")) {
            Logger.e(TAG, "enter the service process!");
            //"com.easemob.chatuidemo"为demo的包名，换到自己项目中要改成自己包名

            // 则此application::onCreate 是被service 调用的，直接返回
            return false;
        }

        EMChatManager.getInstance().addConnectionListener(new MyConnectionListener());

        registerGlobleReceivers();
        initOptions();
        initNotifier();
        initEventListener();

        sdkInited = true;
        return true;
    }


    public static HXController getInstance() {
        return instance;
    }

    public boolean isHXLoggedin() {
        return EMChat.getInstance().isLoggedIn();
    }

    public void startLogin(String imId) {
        if (isHXLoggedin()) {
            try {
                startLogout();
            } catch (Exception e) {
                Logger.e(TAG, e.getMessage());
            }
        }
        EMChatManager.getInstance().login(imId, HXController.PWD, new EMCallBack() {
            @Override
            public void onSuccess() {
                loadAllEnvirionment();
            }

            @Override
            public void onError(int i, String s) {
                Logger.e(TAG, "startLogin error occurred!");
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    public void startLogout() throws Exception {
        if (isHXLoggedin()) {
            EMChatManager.getInstance().logout();
        }
    }

    public void registerDeliveryAckReceiver(Context contextLocal) {
        //如果用到已发送的回执需要把这个flag设置成true
        EMChatManager.getInstance().getChatOptions().setRequireDeliveryAck(true);

        DeliveryAckMessageReceiver deliveryAckMessageReceiver = new DeliveryAckMessageReceiver();
        IntentFilter deliveryAckMessageIntentFilter = new IntentFilter(EMChatManager.getInstance().getDeliveryAckMessageBroadcastAction());
        deliveryAckMessageIntentFilter.setPriority(5);
        contextLocal.registerReceiver(deliveryAckMessageReceiver, deliveryAckMessageIntentFilter);
    }

    public void pushActivity(Activity activity) {
        if (!activityList.contains(activity)) {
            activityList.add(0, activity);
        }
    }

    public void popActivity(Activity activity) {
        activityList.remove(activity);
    }

    public HXNotifier getNotifier() {
        return notifier;
    }

    private String getAppName(int pID) {
        String processName;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = context.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    Logger.d("Process", "Id: " + info.pid + " ProcessName: " + info.processName + "  Label: " + c.toString());
                    // processName = c.toString();
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                Logger.d("Process", "Error>> :" + e.toString());
            }
        }
        return null;
    }

    private void registerGlobleReceivers() {
        NewMessageBroadcastReceiver msgReceiver = new NewMessageBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
        intentFilter.setPriority(3);
        context.registerReceiver(msgReceiver, intentFilter);
    }

    private void initOptions() {
        //获取到配置options对象
        EMChatOptions options = EMChatManager.getInstance().getChatOptions();
        //设置自定义的文字提示
        options.setNotifyText(new OnMessageNotifyListener() {

            @Override
            public String onNewMessageNotify(EMMessage message) {
                return "你的好基友" + message.getFrom() + "发来了一条消息哦";
            }

            @Override
            public String onLatestMessageNotify(EMMessage message, int fromUsersNum, int messageNum) {
                return fromUsersNum + "个基友，发来了" + messageNum + "条消息";
            }

            @Override
            public String onSetNotificationTitle(EMMessage emMessage) {
                return null;
            }

            @Override
            public int onSetSmallIcon(EMMessage emMessage) {
                return 0;
            }
        });

        //设置notification点击listener
        options.setOnNotificationClickListener(new OnNotificationClickListener() {
            @Override
            public Intent onNotificationClick(EMMessage message) {
                Intent intent = new Intent(context, ChatActivity.class);
                EMMessage.ChatType chatType = message.getChatType();
                if (chatType == EMMessage.ChatType.Chat) { //单聊信息
                    intent.putExtra("userId", message.getFrom());
                    intent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
                } else { //群聊信息
                    //message.getTo()为群聊id
                    intent.putExtra("groupId", message.getTo());
                    intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                }
                return intent;
            }
        });


    }

    private void loadAllEnvirionment() {
        try {
            EMGroupManager.getInstance().loadAllGroups();
            EMChatManager.getInstance().loadAllConversations();
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    protected void initEventListener() {
        eventListener = new EMEventListener() {
            private BroadcastReceiver broadCastReceiver = null;

            @Override
            public void onEvent(EMNotifierEvent event) {
                EMMessage message = null;
                if (event.getData() instanceof EMMessage) {
                    message = (EMMessage) event.getData();
                    Logger.d(TAG, "receive the event : " + event.getEvent() + ",id : " + message.getMsgId());
                }

                switch (event.getEvent()) {
                    case EventNewMessage:
                        //应用在后台，不需要刷新UI,通知栏提示新消息
                        if (activityList.size() <= 0) {
                            HXController.getInstance().getNotifier().onNewMsg(message);
                        }
                        break;
                    case EventOfflineMessage:
                        if (activityList.size() <= 0) {
                            Logger.d(TAG, "received offline messages");
                            List<EMMessage> messages = (List<EMMessage>) event.getData();
                            HXController.getInstance().getNotifier().onNewMesg(messages);
                        }
                        break;
                    // below is just giving a example to show a cmd toast, the app should not follow this
                    // so be careful of this
                    case EventNewCMDMessage: {

                        Logger.d(TAG, "收到透传消息");
                        //获取消息body
                        CmdMessageBody cmdMsgBody = (CmdMessageBody) message.getBody();
                        final String action = cmdMsgBody.action;//获取自定义action

                        //获取扩展属性 此处省略
                        //message.getStringAttribute("");
                        Logger.d(TAG, String.format("透传消息：action:%s,message:%s", action, message.toString()));
                        final String str = "收到透传：action：";

                        final String CMD_TOAST_BROADCAST = "com.accenture.dtoaii.im.cmd.toast";
                        IntentFilter cmdFilter = new IntentFilter(CMD_TOAST_BROADCAST);

                        if (broadCastReceiver == null) {
                            broadCastReceiver = new BroadcastReceiver() {

                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    // TODO Auto-generated method stub
                                    Toast.makeText(context, intent.getStringExtra("cmd_value"), Toast.LENGTH_SHORT).show();
                                }
                            };

                            //注册广播接收者
                            context.registerReceiver(broadCastReceiver, cmdFilter);
                        }

                        Intent broadcastIntent = new Intent(CMD_TOAST_BROADCAST);
                        broadcastIntent.putExtra("cmd_value", str + action);
                        context.sendBroadcast(broadcastIntent, null);

                        break;
                    }
                    case EventDeliveryAck:
                        message.setDelivered(true);
                        break;
                    case EventReadAck:
                        message.setAcked(true);
                        break;
                    // add other events in case you are interested in
                    default:
                        break;
                }

            }
        };

        EMChatManager.getInstance().registerEventListener(eventListener);
    }

    public void initNotifier() {
        if (notifier == null) {
            notifier = createNotifier();
            notifier.init(context);
            notifier.setNotificationInfoProvider(getNotificationListener());
        }
    }

    public HXNotifier createNotifier() {
        return new HXNotifier() {
            public synchronized void onNewMsg(final EMMessage message) {
                if (EMChatManager.getInstance().isSlientMessage(message)) {
                    return;
                }

                // 判断app是否在后台
                if (!EasyUtils.isAppRunningForeground(context)) {
                    Logger.d(TAG, "app is running in backgroud");
                    sendNotification(message, false);
                } else {
                    sendNotification(message, true);
                }

                viberateAndPlayTone(message);
            }
        };
    }

    protected HXNotifier.HXNotificationInfoProvider getNotificationListener() {
        //可以覆盖默认的设置
        return new HXNotifier.HXNotificationInfoProvider() {

            @Override
            public String getTitle(EMMessage message) {
                //修改标题,这里使用默认
                return null;
            }

            @Override
            public int getSmallIcon(EMMessage message) {
                //设置小图标，这里为默认
                return 0;
            }

            @Override
            public String getDisplayedText(EMMessage message) {
                // 设置状态栏的消息提示，可以根据message的类型做相应提示
                String ticker = CommonUtils.getMessageDigest(message, context);
                if (message.getType() == EMMessage.Type.TXT) {
                    ticker = ticker.replaceAll("\\[.{2,3}\\]", "[表情]");
                }

                return message.getFrom() + ": " + ticker;
            }

            @Override
            public String getLatestText(EMMessage message, int fromUsersNum, int messageNum) {
                return null;
                // return fromUsersNum + "个基友，发来了" + messageNum + "条消息";
            }

            @Override
            public Intent getLaunchIntent(EMMessage message) {
                //设置点击通知栏跳转事件
                Intent intent = new Intent(context, ChatActivity.class);

                EMMessage.ChatType chatType = message.getChatType();
                if (chatType == EMMessage.ChatType.Chat) { // 单聊信息
                    intent.putExtra("userId", message.getFrom());
                    intent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
                } else { // 群聊信息
                    // message.getTo()为群聊id
                    intent.putExtra("groupId", message.getTo());
                    if (chatType == EMMessage.ChatType.GroupChat) {
                        intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                    } else {
                        intent.putExtra("chatType", ChatActivity.CHATTYPE_CHATROOM);
                    }

                }
                return intent;
            }
        };
    }

    private class MyConnectionListener implements EMConnectionListener {
        @Override
        public void onConnected() {
            Logger.i(TAG, "MyConnectionListener.onConnected");
        }

//        @Override
//        public void onDisConnected(String s) {
//            Logger.e(TAG, "MyConnectionListener.onDisConnected, reason:" + s);
//        }

//        @Override
//        public void onReConnected() {
//            Logger.i(TAG, "MyConnectionListener.onReConnected");
//        }
//
//        @Override
//        public void onReConnecting() {
//            Logger.i(TAG, "MyConnectionListener.onReConnecting");
//        }
//
//        @Override
//        public void onConnecting(String s) {
//            Logger.i(TAG, "MyConnectionListener.onConnecting" + s);
//        }

        @Override
        public void onDisconnected(final int error) {
            if (error == EMError.USER_REMOVED) {
                Logger.i(TAG, "MyConnectionListener.onDisconnected");
            } else if (error == EMError.CONNECTION_CONFLICT) {
                Logger.i(TAG, "MyConnectionListener.onDisconnected");
            } else {
                Logger.i(TAG, "MyConnectionListener.onDisconnected");

            }
        }

    }
}