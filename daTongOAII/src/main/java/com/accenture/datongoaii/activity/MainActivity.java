package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.fragment.ContactFragment;
import com.accenture.datongoaii.fragment.NotiFragment;
import com.accenture.datongoaii.fragment.TaskFragment;
import com.accenture.datongoaii.fragment.TodoFragment;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;
import com.accenture.datongoaii.vendor.HX.HXController;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.igexin.sdk.PushManager;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends FragmentActivity implements OnClickListener, EMEventListener {
    private long mExitTime = 0;
    private final int TAB_TODO = 0;
    private final int TAB_NOTI = 1;
    private final int TAB_TASK = 2;
    private final int TAB_CONTACT = 3;
    private int curTab = 0;

    public TodoFragment todoFrag;
    private NotiFragment notiFrag;
    private TaskFragment taskFrag;
    public ContactFragment contactFrag;

    private int retryTimes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PushManager.getInstance().initialize(getApplicationContext());
        syncUserForPush();

        findViewById(R.id.lTodo).setOnClickListener(this);
        findViewById(R.id.lNotification).setOnClickListener(this);
        findViewById(R.id.lTask).setOnClickListener(this);
        findViewById(R.id.lContact).setOnClickListener(this);
        setTabSelected(TAB_TODO);

        Utils.toast(this, Config.SUCCESS_LOGIN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HXController controller = HXController.getInstance();
        controller.pushActivity(this);

        EMChatManager.getInstance().registerEventListener(
                this,
                new EMNotifierEvent.Event[]{EMNotifierEvent.Event.EventNewMessage, EMNotifierEvent.Event.EventOfflineMessage,
                        EMNotifierEvent.Event.EventDeliveryAck, EMNotifierEvent.Event.EventReadAck});
    }

    @Override
    protected void onStop() {
        HXController controller = HXController.getInstance();
        controller.popActivity(this);
        EMChatManager.getInstance().unregisterEventListener(this);
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            contactFrag.getOrg(Account.getInstance().getUserId());
            notiFrag.syncConversationList();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolvePushMessage();
    }

    private void resolvePushMessage() {
        Bundle bundle = getIntent().getExtras();
        String data = bundle.getString(Constants.PUSH_DATA_TAG);
        if (data != null && data.length() > 0) {
            try {
                JSONObject json = new JSONObject(data);
                int command = Integer.parseInt(json
                        .getString(Constants.PUSH_JSON_COMMAND));
                switch (command) {
                    case Constants.PUSH_COMMAND_REFRESH_TODO:
                        todoFrag.refreshTodoList();
                        break;
                    case Constants.PUSH_COMMAND_REFRESH_NOTI:
                        notiFrag.syncConversationList();
                    case Constants.PUSH_COMMAND_REFRESH_CONTACT:
//					Integer deptId = json.getInt(Constants.PUSH_JSON_REFRESH_CONTACT_DEPT_ID);
//					if (deptId >= -1) {
//						contactFrag.get(Account.getInstance().getUserId());
//					}
                }
            } catch (JSONException e) {
                Logger.e("resolvePushMessage", e.getMessage());
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lTodo:
                if (curTab != TAB_TODO) {
                    setTabSelected(TAB_TODO);
                    curTab = TAB_TODO;
                }
                break;
            case R.id.lNotification:
                if (curTab != TAB_NOTI) {
                    setTabSelected(TAB_NOTI);
                    curTab = TAB_NOTI;
                }
                break;
            case R.id.lTask:
                if (curTab != TAB_TASK) {
                    setTabSelected(TAB_TASK);
                    curTab = TAB_TASK;
                }
                break;
            case R.id.lContact:
                if (curTab != TAB_CONTACT) {
                    setTabSelected(TAB_CONTACT);
                    curTab = TAB_CONTACT;
                }
                break;
        }
    }

    private void setTabSelected(int tab) {
        Utils.closeSoftKeyboard(this, getCurrentFocus());
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        hideFragments(t);
        unsetTabBehavier();
        switch (tab) {
            case TAB_TODO:
                if (todoFrag == null) {
                    todoFrag = new TodoFragment();
                    t.add(R.id.lBody, todoFrag);
                } else {
                    t.show(todoFrag);
                }
                ((ImageView) findViewById(R.id.imgTodo))
                        .setImageResource(R.drawable.tab_todo_h);
                ((TextView) findViewById(R.id.txtTodo)).setTextColor(this
                        .getResources().getColor(R.color.green));
                break;
            case TAB_NOTI:
                if (notiFrag == null) {
                    notiFrag = new NotiFragment();
                    t.add(R.id.lBody, notiFrag);
                } else {
                    t.show(notiFrag);
                    notiFrag.syncConversationList();
                }
                ((ImageView) findViewById(R.id.imgNotification))
                        .setImageResource(R.drawable.tab_noti_h);
                ((TextView) findViewById(R.id.txtNotification)).setTextColor(this
                        .getResources().getColor(R.color.green));
                break;
            case TAB_TASK:
                if (taskFrag == null) {
                    taskFrag = new TaskFragment();
                    t.add(R.id.lBody, taskFrag);
                } else {
                    t.show(taskFrag);
                }
                ((ImageView) findViewById(R.id.imgTask))
                        .setImageResource(R.drawable.tab_task_h);
                ((TextView) findViewById(R.id.txtTask)).setTextColor(this
                        .getResources().getColor(R.color.green));
                break;
            case TAB_CONTACT:
                if (contactFrag == null) {
                    contactFrag = new ContactFragment();
                    t.add(R.id.lBody, contactFrag);
                } else {
                    t.show(contactFrag);
                }
                ((ImageView) findViewById(R.id.imgContact))
                        .setImageResource(R.drawable.tab_contact_h);
                ((TextView) findViewById(R.id.txtContact)).setTextColor(this
                        .getResources().getColor(R.color.green));
                break;
        }
        t.commitAllowingStateLoss();
    }

    private void hideFragments(FragmentTransaction t) {
        if (todoFrag != null) {
            t.hide(todoFrag);
        }
        if (notiFrag != null) {
            t.hide(notiFrag);
        }
        if (taskFrag != null) {
            t.hide(taskFrag);
        }
        if (contactFrag != null) {
            t.hide(contactFrag);
        }
    }

    private void unsetTabBehavier() {
        ((ImageView) findViewById(R.id.imgTodo))
                .setImageResource(R.drawable.tab_todo);
        ((ImageView) findViewById(R.id.imgNotification))
                .setImageResource(R.drawable.tab_noti);
        ((ImageView) findViewById(R.id.imgTask))
                .setImageResource(R.drawable.tab_task);
        ((ImageView) findViewById(R.id.imgContact))
                .setImageResource(R.drawable.tab_contact);
        ((TextView) findViewById(R.id.txtTodo)).setTextColor(this
                .getResources().getColor(R.color.gray_5));
        ((TextView) findViewById(R.id.txtNotification)).setTextColor(this
                .getResources().getColor(R.color.gray_5));
        ((TextView) findViewById(R.id.txtTask)).setTextColor(this
                .getResources().getColor(R.color.gray_5));
        ((TextView) findViewById(R.id.txtContact)).setTextColor(this
                .getResources().getColor(R.color.gray_5));
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按就要退出了哟~", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void syncUserForPush() {
        String url = Config.SERVER_HOST + "/DTOAII/syncUser.json";
        JSONObject json = new JSONObject();
        try {
            json.put("userId", Account.getInstance().getUserId());
            json.put(
                    "clientId",
                    PushManager.getInstance().getClientid(
                            getApplicationContext()));
        } catch (JSONException e) {
            Logger.e("syncUserForPush", e.getMessage());
        }
        new HttpConnection().post(url, json,
                new HttpConnection.CallbackListener() {
                    @Override
                    public void callBack(String result) {
                        if (!result.equals("fail")) {
                            try {
                                CommonResponse res = Intepreter
                                        .getCommonStatusFromJson(result);
                                if (res.statusCode == 0) {
                                    retryTimes = 0;
                                } else {
                                    show(res.statusMsg);
                                }
                            } catch (JSONException e) {
                                show(Config.ERROR_INTERFACE);
                            }
                        } else {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    retrySyncUser();
                                }
                            }, 30000 * retryTimes);
                        }
                    }
                });
    }

    private void retrySyncUser() {
        if (retryTimes < 2) {
            syncUserForPush();
        }
        retryTimes++;
    }


    @Override
    public void onEvent(EMNotifierEvent emNotifierEvent) {
        switch (emNotifierEvent.getEvent()) {
            case EventNewMessage: {
                //获取到message
                EMMessage message = (EMMessage) emNotifierEvent.getData();

//                String username = null;
//                //群组消息
//                if (message.getChatType() == EMMessage.ChatType.GroupChat || message.getChatType() == EMMessage.ChatType.ChatRoom) {
//                    username = message.getTo();
//                } else {
//                    //单聊消息
//                    username = message.getFrom();
//                }

                //刷新消息列表
                HXController.getInstance().getNotifier().viberateAndPlayTone(message);
                notiFrag.syncConversationList();

                break;
            }
//            case EventDeliveryAck:
//            {
//                //获取到message
//                EMMessage message = (EMMessage) event.getData();
//                refreshUI();
//                break;
//            }
//            case EventReadAck:
//            {
//                //获取到message
//                EMMessage message = (EMMessage) event.getData();
//                refreshUI();
//                break;
//            }
//            case EventOfflineMessage:
//            {
//                //a list of offline messages
//                //List<EMMessage> offlineMessages = (List<EMMessage>) event.getData();
//                refreshUI();
//                break;
//            }
            default:
                break;
        }
    }

    private void show(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
