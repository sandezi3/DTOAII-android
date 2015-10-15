package com.accenture.datongoaii.vendor.HX.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.DTOARequest;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Group;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;
import com.accenture.datongoaii.vendor.HX.CommonUtils;
import com.accenture.datongoaii.vendor.HX.HXController;
import com.accenture.datongoaii.vendor.HX.adapter.MessageAdapter;
import com.easemob.EMCallBack;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

/**
 * Created by leon on 9/28/15.
 * 单聊
 */
public class ChatActivity extends Activity implements View.OnClickListener, EMEventListener {

    public static final int CHATTYPE_SINGLE = 1;
    public static final int CHATTYPE_GROUP = 2;
    private static final String TAG = "ChatActivity";
    private static final int pageSize = 20;

    public PullToRefreshListView ptrlvChat;
    private EditText mEditTextContent;
    private LinearLayout emojiIconContainer;
    private LinearLayout btnContainer;
    private View more;
    private ImageView iv_emoticons_normal;
    private ImageView iv_emoticons_checked;

    private Context context;
    private String toId;
    private int chatType;
    private EMConversation conversation;
    private MessageAdapter adapter;
    private boolean isLoading = false;
    private boolean haveMoreData = true;
    private File cameraFile;

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
        btnContainer = (LinearLayout) findViewById(R.id.ll_btn_container);
        emojiIconContainer = (LinearLayout) findViewById(R.id.ll_face_container);
        more = findViewById(R.id.more);
        iv_emoticons_normal = (ImageView) findViewById(R.id.iv_emoticons_normal);
        iv_emoticons_checked = (ImageView) findViewById(R.id.iv_emoticons_checked);

        btnSend.setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);
        iv_emoticons_normal.setVisibility(View.VISIBLE);
        iv_emoticons_checked.setVisibility(View.INVISIBLE);
        btnContainer.setVisibility(View.GONE);

        mEditTextContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                more.setVisibility(View.GONE);
                iv_emoticons_normal.setVisibility(View.VISIBLE);
                iv_emoticons_checked.setVisibility(View.INVISIBLE);
                emojiIconContainer.setVisibility(View.GONE);
                btnContainer.setVisibility(View.GONE);
            }
        });
        ptrlvChat.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        ptrlvChat.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                loadMoreChatHistory();
            }
        });
        ptrlvChat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utils.closeSoftKeyboard(context, v);
                more.setVisibility(View.GONE);
                iv_emoticons_normal.setVisibility(View.VISIBLE);
                iv_emoticons_checked.setVisibility(View.INVISIBLE);
                emojiIconContainer.setVisibility(View.GONE);
                btnContainer.setVisibility(View.GONE);
                return false;
            }
        });
        initAdapter();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_CAMERA) {
            // 发送照片
            if (cameraFile != null && cameraFile.exists())
                sendPicture(cameraFile.getAbsolutePath());
        } else if (requestCode == Constants.REQUEST_CODE_LOCAL) { // 发送本地图片
            if (data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    sendPicByUri(selectedImage);
                }
            }
        }
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
            case R.id.btn_take_picture:
                // 点击照相图标
                selectPicFromCamera();
                break;
            case R.id.btn_picture:
                // 点击图片图标
                selectPicFromLocal();
                break;
            default:
        }
    }

    public ListView getListView() {
        return ptrlvChat.getRefreshableView();
    }

    /**
     * 点击文字输入框
     *
     * @param v do nothing
     */
    public void editClick(View v) {
        getListView().setSelection(getListView().getCount() - 1);
        if (more.getVisibility() == View.VISIBLE) {
            more.setVisibility(View.GONE);
            iv_emoticons_normal.setVisibility(View.VISIBLE);
            iv_emoticons_checked.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 显示或隐藏图标按钮页
     *
     * @param view do nothing
     */
    public void toggleMore(View view) {
        if (more.getVisibility() == View.GONE) {
            Logger.d(TAG, "more gone");
            Utils.closeSoftKeyboard(context, view);
            more.setVisibility(View.VISIBLE);
            btnContainer.setVisibility(View.VISIBLE);
            emojiIconContainer.setVisibility(View.GONE);
        } else {
            if (emojiIconContainer.getVisibility() == View.VISIBLE) {
                emojiIconContainer.setVisibility(View.GONE);
                btnContainer.setVisibility(View.VISIBLE);
                iv_emoticons_normal.setVisibility(View.VISIBLE);
                iv_emoticons_checked.setVisibility(View.INVISIBLE);
            } else {
                more.setVisibility(View.GONE);
            }
        }
    }

    private void sendText(String content) {
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

    /**
     * 发送图片
     *
     * @param filePath 图片文件路径
     */
    private void sendPicture(final String filePath) {
        if (!EMChatManager.getInstance().isConnected()) {
            Utils.toast(context, Config.ERROR_IM);
            return;
        }
        String to = toId;
        // create and add image message in view
        final EMMessage message = EMMessage.createSendMessage(EMMessage.Type.IMAGE);
        // 如果是群聊，设置chattype,默认是单聊
        if (chatType == CHATTYPE_GROUP) {
            message.setChatType(EMMessage.ChatType.GroupChat);
        }

        message.setReceipt(to);
        ImageMessageBody body = new ImageMessageBody(new File(filePath));
        // 默认超过100k的图片会压缩后发给对方，可以设置成发送原图
        // body.setSendOriginalImage(true);
        message.addBody(body);
        conversation.addMessage(message);

        ptrlvChat.setAdapter(adapter);
        adapter.refreshSelectLast();
        setResult(RESULT_OK);
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
                if (!isLoading && haveMoreData) {
                    List<EMMessage> messages;
                    try {
                        if (chatType == CHATTYPE_SINGLE) {
                            messages = conversation.loadMoreMsgFromDB(adapter.getItem(0).getMsgId(), pageSize);
                        } else {
                            messages = conversation.loadMoreGroupMsgFromDB(adapter.getItem(0).getMsgId(), pageSize);
                        }
                    } catch (Exception e1) {
                        ptrlvChat.onRefreshComplete();
                        return;
                    }
                    if (messages.size() > 0) {
                        adapter.notifyDataSetChanged();
                        adapter.refreshSeekTo(messages.size() - 1);
                        if (messages.size() != pageSize) {
                            haveMoreData = false;
                        }
                    } else {
                        haveMoreData = false;
                    }
                    isLoading = false;
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

    /**
     * 照相获取图片
     */
    private void selectPicFromCamera() {
        if (!CommonUtils.isExitsSdcard()) {
            Utils.toast(context, Config.NOTE_NO_STORAGE_CARD);
            return;
        }

        String fileName = Environment.getExternalStorageDirectory().getPath() + "/TEMP" + "/temp" + System.currentTimeMillis() + ".jpg";
        cameraFile = new File(fileName);
        cameraFile.getParentFile().mkdirs();
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
                Constants.REQUEST_CODE_CAMERA);
    }


    /**
     * 从图库获取图片
     */
    public void selectPicFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, Constants.REQUEST_CODE_LOCAL);
    }

    /**
     * 根据图库图片uri发送图片
     *
     * @param selectedImage 图库图片Uri
     */
    private void sendPicByUri(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        String st8 = Config.NOTE_NO_LOCAL_PIC;
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            if (picturePath == null || picturePath.equals("null")) {
                Utils.toast(context, st8);
                return;
            }
            sendPicture(picturePath);
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                Utils.toast(context, st8);
                return;
            }
            sendPicture(file.getAbsolutePath());
        }
    }
}
