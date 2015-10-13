package com.accenture.datongoaii.vendor.HX;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.DTOARequest;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Group;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;
import com.accenture.datongoaii.vendor.HX.adapter.MessageAdapter;
import com.easemob.EMCallBack;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by leon on 9/28/15.
 * 单聊
 */
public class ChatActivity extends Activity implements View.OnClickListener, EMEventListener {

    private static final String TAG = "ChatActivity";
    public static final int CHATTYPE_SINGLE = 1;
    public static final int CHATTYPE_GROUP = 2;
    public static final int CHATTYPE_CHATROOM = 3;
    private static final int pagesize = 20;

    private Context context;
    private String toId;
    private int chatType;
    private EMConversation conversation;
    private EditText mEditTextContent;
    public PullToRefreshListView ptrlvChat;
    public MessageAdapter adapter;
    private boolean isloading = false;
    private boolean haveMoreData = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        context = this;
        chatType = getIntent().getIntExtra("chatType", CHATTYPE_SINGLE);
        if (chatType == CHATTYPE_SINGLE) {
            toId = getIntent().getStringExtra("userId");
        } else if (chatType == CHATTYPE_GROUP) {
            toId = getIntent().getStringExtra("groupId");
        }

        mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);

        Button btnSend = (Button) findViewById(R.id.btn_send);
        ptrlvChat = (PullToRefreshListView) findViewById(R.id.ptrlvChat);

        btnSend.setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);

        ptrlvChat.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        ptrlvChat.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                loadMoreChatHistory();
            }
        });

        initAdapter();
    }

    private void initAdapter() {
        if (chatType == CHATTYPE_SINGLE) {
            DTOARequest.startGetUserByImId(toId, new HttpConnection.CallbackListener() {
                @Override
                public void callBack(String result) {
                    if (!result.equals("fail")) {
                        try {
                            Contact c = Contact.fromJSON(new JSONObject(result).getJSONObject("data"));
                            assert c != null;
                            ((TextView) findViewById(R.id.textTitle)).setText(c.name);
                            adapter = new MessageAdapter(context, c);
                            ptrlvChat.setAdapter(adapter);
                            refreshUIWithNewMessage();
                            initConversations();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else if (chatType == CHATTYPE_GROUP) {
            DTOARequest.startGetGroup(toId, new HttpConnection.CallbackListener() {
                @Override
                public void callBack(String result) {
                    if (!result.equals("fail")) {
                        try {
                            Group group = new Group();
                            group.imId = toId;
                            group = Group.updateFromJSON(group, new JSONObject(result).getJSONObject("data"));
                            if (group.name != null) {
                                ((TextView) findViewById(R.id.textTitle)).setText(group.name);
                            }
                            adapter = new MessageAdapter(context, group);
                            ptrlvChat.setAdapter(adapter);
                            refreshUIWithNewMessage();
                            initConversations();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private void refreshUIWithNewMessage() {
        if (adapter == null) {
            return;
        }

        runOnUiThread(new Runnable() {
            public void run() {
                adapter.refreshSelectLast();
            }
        });
    }

    private void refreshUI() {
        if (adapter == null) {
            return;
        }

        runOnUiThread(new Runnable() {
            public void run() {
                adapter.refresh();
            }
        });
    }

    private void loadMoreChatHistory() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isloading && haveMoreData) {
                    List<EMMessage> messages;
                    try {
                        if (chatType == CHATTYPE_SINGLE) {
                            messages = conversation.loadMoreMsgFromDB(adapter.getItem(0).getMsgId(), pagesize);
                        } else {
                            messages = conversation.loadMoreGroupMsgFromDB(adapter.getItem(0).getMsgId(), pagesize);
                        }
                    } catch (Exception e1) {
                        ptrlvChat.onRefreshComplete();
                        return;
                    }
                    if (messages.size() > 0) {
                        adapter.notifyDataSetChanged();
                        adapter.refreshSeekTo(messages.size() - 1);
                        if (messages.size() != pagesize) {
                            haveMoreData = false;
                        }
                    } else {
                        haveMoreData = false;
                    }
                    isloading = false;
                }
                if (!haveMoreData) {
                    Utils.toast(context, Config.NOTE_NO_MORE_MESSAGE);
                }
                ptrlvChat.onRefreshComplete();
            }
        }, 1000);
    }

    private void initConversations() {
        conversation = EMChatManager.getInstance().getConversationByType(toId, EMConversation.EMConversationType.Chat);
        conversation.markAllMessagesAsRead();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshUI();

        // register the event listener when enter the foreground
        HXController ctrl = HXController.getInstance();
        ctrl.pushActivity(this);
        EMChatManager.getInstance().registerEventListener(
                this,
                new EMNotifierEvent.Event[]{EMNotifierEvent.Event.EventNewMessage, EMNotifierEvent.Event.EventOfflineMessage,
                        EMNotifierEvent.Event.EventDeliveryAck, EMNotifierEvent.Event.EventReadAck});
        EMChat.getInstance().setAppInited();
    }

    @Override
    protected void onStop() {
        EMChatManager.getInstance().unregisterEventListener(this);
        HXController ctrl = HXController.getInstance();
        ctrl.popActivity(this);
        super.onStop();
    }

    public ListView getListView() {
        return ptrlvChat.getRefreshableView();
    }

    @Override
    public void onEvent(EMNotifierEvent emNotifierEvent) {
        switch (emNotifierEvent.getEvent()) {
            case EventNewMessage: {
                //获取到message
                EMMessage message = (EMMessage) emNotifierEvent.getData();

                String username;
                //群组消息
                if (message.getChatType() == EMMessage.ChatType.GroupChat || message.getChatType() == EMMessage.ChatType.ChatRoom) {
                    username = message.getTo();
                } else {
                    //单聊消息
                    username = message.getFrom();
                }

                //如果是当前会话的消息，刷新聊天页面
                if (username.equals(toId)) {
                    refreshUIWithNewMessage();
//                    //声音和震动提示有新消息
                    HXController.getInstance().getNotifier().viberateAndPlayTone(message);
                } else {
                    //如果消息不是和当前聊天ID的消息
                    HXController.getInstance().getNotifier().onNewMsg(message);
                }

                break;
            }
            case EventDeliveryAck: {
                //获取到message
//                EMMessage message = (EMMessage) emNotifierEvent.getData();
                refreshUI();
                break;
            }
            case EventReadAck: {
                //获取到message
//                EMMessage message = (EMMessage) emNotifierEvent.getData();
                refreshUI();
                break;
            }
            case EventOfflineMessage: {
                //a list of offline messages
//                List<EMMessage> offlineMessages = (List<EMMessage>) emNotifierEvent.getData();
                refreshUI();
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.btn_send:
                sendText(mEditTextContent.getEditableText().toString());
                break;
            default:
        }
    }

    /**
     * 点击文字输入框
     *
     * @param v do nothing
     */
    public void editClick(View v) {
//        getListView().setSelection(getListView().getCount() - 1);
    }


    public void sendText(String content) {
        if (!EMChatManager.getInstance().isConnected()) {
            Utils.toast(context, Config.ERROR_IM);
            return;
        }
        if (content.length() > 0) {
            EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);

            TextMessageBody txtBody = new TextMessageBody(content);
            // 设置消息body
            message.addBody(txtBody);
            // 设置要发给谁,用户username或者群聊groupid
            if (chatType == CHATTYPE_GROUP) {
                message.setChatType(EMMessage.ChatType.GroupChat);
            }
            message.setReceipt(toId);
            // 把messgage加到conversation中
            conversation.addMessage(message);
            // 通知adapter有消息变动，adapter会根据加入的这条message显示消息和调用sdk的发送方法
            mEditTextContent.setText("");
            adapter.refreshSelectLast();

            setResult(RESULT_OK);
            EMChatManager.getInstance().sendMessage(message, new EMCallBack() {
                @Override
                public void onSuccess() {
                    Logger.i(TAG, "sendText.onSuccess");
                    adapter.refreshSelectLast();
                }

                @Override
                public void onError(int i, String s) {
                    Logger.e(TAG, "sendText.onError");
                    adapter.refreshSelectLast();
                }

                @Override
                public void onProgress(int i, String s) {

                }
            });
        }
    }

}
