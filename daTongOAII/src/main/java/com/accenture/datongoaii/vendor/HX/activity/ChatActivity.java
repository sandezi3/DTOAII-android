package com.accenture.datongoaii.vendor.HX.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.DTOARequest;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.GroupProfileActivity;
import com.accenture.datongoaii.db.ContactDao;
import com.accenture.datongoaii.db.GroupDao;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Group;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;
import com.accenture.datongoaii.vendor.HX.HXController;
import com.accenture.datongoaii.vendor.HX.Utils.CommonUtils;
import com.accenture.datongoaii.vendor.HX.adapter.GroupRemoveListener;
import com.accenture.datongoaii.vendor.HX.adapter.MessageAdapter;
import com.accenture.datongoaii.vendor.HX.adapter.VoicePlayClickListener;
import com.easemob.EMCallBack;
import com.easemob.EMError;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.util.VoiceRecorder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

/**
 * Created by leon on 9/28/15.
 * 聊天
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
    private View recordingContainer;
    private View more;
    private ImageView iv_emoticons_normal;
    private ImageView iv_emoticons_checked;
    private RelativeLayout edittext_layout;
    private View buttonSetModeKeyboard;
    private View buttonSetModeVoice;
    private View buttonPressToSpeak;
    private View buttonSend;
    private Button btnMore;
    private ImageView micImage;
    private TextView recordingHint;

    private Context context;
    private String toId;
    private int chatType;
    private EMConversation conversation;
    private MessageAdapter adapter;
    private boolean isLoading = false;
    private boolean haveMoreData = true;
    private File cameraFile;
    public String playMsgId;
    private VoiceRecorder voiceRecorder;
    private GroupListener groupListener;
    private Handler micImageHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            // 切换msg切换图片
            micImage.setImageDrawable(micImages[msg.what]);
        }
    };
    private Drawable[] micImages;

    @Override
    @SuppressWarnings("Deprecated")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        context = this;
        chatType = getIntent().getIntExtra("chatType", CHATTYPE_SINGLE);
        if (chatType == CHATTYPE_SINGLE) {
            toId = getIntent().getStringExtra("userId");
        } else if (chatType == CHATTYPE_GROUP) {
            toId = getIntent().getStringExtra("groupId");
            // 监听当前会话的群聊解散被T事件
            groupListener = new GroupListener();
            EMGroupManager.getInstance().addGroupChangeListener(groupListener);
        }

        recordingContainer = findViewById(R.id.recording_container);
        mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);
        buttonSend = findViewById(R.id.btn_send);
        ptrlvChat = (PullToRefreshListView) findViewById(R.id.ptrlvChat);
        btnContainer = (LinearLayout) findViewById(R.id.ll_btn_container);
        emojiIconContainer = (LinearLayout) findViewById(R.id.ll_face_container);
        buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
        buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
        buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
        btnMore = (Button) findViewById(R.id.btn_more);
        more = findViewById(R.id.more);
        iv_emoticons_normal = (ImageView) findViewById(R.id.iv_emoticons_normal);
        iv_emoticons_checked = (ImageView) findViewById(R.id.iv_emoticons_checked);
        edittext_layout = (RelativeLayout) findViewById(R.id.edittext_layout);
        micImage = (ImageView) findViewById(R.id.mic_image);
        recordingHint = (TextView) findViewById(R.id.recording_hint);

        buttonSend.setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);
        iv_emoticons_normal.setVisibility(View.VISIBLE);
        iv_emoticons_checked.setVisibility(View.INVISIBLE);
        btnContainer.setVisibility(View.GONE);
        buttonPressToSpeak.setOnTouchListener(new PressToSpeakListen());
        voiceRecorder = new VoiceRecorder(micImageHandler);

        // 动画资源文件,用于录制语音时
        micImages = new Drawable[]{getResources().getDrawable(R.drawable.record_animate_01),
                getResources().getDrawable(R.drawable.record_animate_02),
                getResources().getDrawable(R.drawable.record_animate_03),
                getResources().getDrawable(R.drawable.record_animate_04),
                getResources().getDrawable(R.drawable.record_animate_05),
                getResources().getDrawable(R.drawable.record_animate_06),
                getResources().getDrawable(R.drawable.record_animate_07),
                getResources().getDrawable(R.drawable.record_animate_08),
                getResources().getDrawable(R.drawable.record_animate_09),
                getResources().getDrawable(R.drawable.record_animate_10),
                getResources().getDrawable(R.drawable.record_animate_11),
                getResources().getDrawable(R.drawable.record_animate_12),
                getResources().getDrawable(R.drawable.record_animate_13),
                getResources().getDrawable(R.drawable.record_animate_14)};
        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");
        // 监听文字框
        mEditTextContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    btnMore.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                } else {
                    btnMore.setVisibility(View.VISIBLE);
                    buttonSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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
        updateAdapter();
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
                        EMNotifierEvent.Event.EventDeliveryAck, EMNotifierEvent.Event.EventReadAck, EMNotifierEvent.Event.EventNewCMDMessage});
    }

    @Override
    protected void onStop() {
        EMChatManager.getInstance().unregisterEventListener(this);
        HXController ctrl = HXController.getInstance();
        ctrl.popActivity(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        context = null;
        if (groupListener != null) {
            EMGroupManager.getInstance().removeGroupChangeListener(groupListener);
        }
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
    protected void onPause() {
        super.onPause();
        if (wakeLock.isHeld())
            wakeLock.release();
        if (VoicePlayClickListener.isPlaying && VoicePlayClickListener.currentPlayListener != null) {
            // 停止语音播放
            VoicePlayClickListener.currentPlayListener.stopPlayVoice();
        }

        try {
            // 停止录音
            if (voiceRecorder.isRecording()) {
                voiceRecorder.discardRecording();
                recordingContainer.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onEvent(EMNotifierEvent emNotifierEvent) {
        if (CommonUtils.handleEventSkip(emNotifierEvent)) {
            return;
        }
        switch (emNotifierEvent.getEvent()) {
            case EventNewCMDMessage:
                EMMessage message1 = (EMMessage) emNotifierEvent.getData();
                break;
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
     * 显示语音图标按钮
     *
     * @param view view
     */
    public void setModeVoice(View view) {
        Utils.closeSoftKeyboard(context, view);
        edittext_layout.setVisibility(View.GONE);
        more.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        buttonSetModeKeyboard.setVisibility(View.VISIBLE);
        buttonSend.setVisibility(View.GONE);
        btnMore.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.VISIBLE);
        iv_emoticons_normal.setVisibility(View.VISIBLE);
        iv_emoticons_checked.setVisibility(View.INVISIBLE);
        btnContainer.setVisibility(View.VISIBLE);
        emojiIconContainer.setVisibility(View.GONE);
    }

    /**
     * 显示键盘图标
     *
     * @param view view
     */
    public void setModeKeyboard(View view) {
        // mEditTextContent.setOnFocusChangeListener(new OnFocusChangeListener()
        // {
        // @Override
        // public void onFocusChange(View v, boolean hasFocus) {
        // if(hasFocus){
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        // }
        // }
        // });
        edittext_layout.setVisibility(View.VISIBLE);
        more.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        buttonSetModeVoice.setVisibility(View.VISIBLE);
        // mEditTextContent.setVisibility(View.VISIBLE);
        mEditTextContent.requestFocus();
        // buttonSend.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.GONE);
        if (TextUtils.isEmpty(mEditTextContent.getText())) {
            btnMore.setVisibility(View.VISIBLE);
            buttonSend.setVisibility(View.GONE);
        } else {
            btnMore.setVisibility(View.GONE);
            buttonSend.setVisibility(View.VISIBLE);
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

    /**
     * 发送语音
     *
     * @param filePath 文件路径
     * @param fileName 文件名
     * @param length   文件长度
     * @param isResend 是否重发
     */
    @SuppressWarnings("unused")
    private void sendVoice(String filePath, String fileName, String length, boolean isResend) {
        if (!(new File(filePath).exists())) {
            return;
        }
        try {
            final EMMessage message = EMMessage.createSendMessage(EMMessage.Type.VOICE);
            // 如果是群聊，设置chattype,默认是单聊
            if (chatType == CHATTYPE_GROUP) {
                message.setChatType(EMMessage.ChatType.GroupChat);
            }
            message.setReceipt(toId);
            int len = Integer.parseInt(length);
            VoiceMessageBody body = new VoiceMessageBody(new File(filePath), len);
            message.addBody(body);
            conversation.addMessage(message);
            adapter.refreshSelectLast();
            setResult(RESULT_OK);
            // send file
            // sendVoiceSub(filePath, fileName, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncAdapter(MessageAdapter a) {
        ptrlvChat.setAdapter(a);
        refreshUIWithNewMessage();
        initConversations();
    }

    private void syncAdapterWithContact(Contact contact) {
        ((TextView) findViewById(R.id.textTitle)).setText(contact.name);
        adapter = new MessageAdapter(context, contact);
        syncAdapter(adapter);
    }

    private void syncAdapterWithGroup(Group group) {
        if (group.name != null) {
            ((TextView) findViewById(R.id.textTitle)).setText(group.name);
        }
        adapter = new MessageAdapter(context, group);
        syncAdapter(adapter);
    }

    private void initAdapter() {
        if (chatType == CHATTYPE_SINGLE) {
            Contact contact = new Contact();
            contact.imId = toId;
            adapter = new MessageAdapter(context, contact);
            syncAdapter(adapter);
        } else if (chatType == CHATTYPE_GROUP) {
            Group group = new Group();
            group.imId = toId;
            adapter = new MessageAdapter(context, group);
            syncAdapter(adapter);
        }
    }

    private void updateAdapter() {
        if (chatType == CHATTYPE_SINGLE) {
            Contact contact = new ContactDao(context).getByImId(toId);
            if (contact != null) {
                syncAdapterWithContact(contact);
            } else {
                DTOARequest.startGetUserByImId(toId, new HttpConnection.CallbackListener() {
                    @Override
                    public void callBack(String result) {
                        if (!result.equals("fail")) {
                            try {
                                Contact c = Contact.fromJSON(new JSONObject(result).getJSONObject("data"));
                                assert c != null;
                                ContactDao dao = new ContactDao(context);
                                if (dao.isExisted(c)) {
                                    dao.update(c, false);
                                } else {
                                    dao.save(c);
                                }
                                syncAdapterWithContact(c);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        } else if (chatType == CHATTYPE_GROUP) {
            Group group = new GroupDao(context).getByImId(toId);
            if (group != null) {
                syncAdapterWithGroup(group);
            } else {
                DTOARequest.startGetGroup(toId, new HttpConnection.CallbackListener() {
                    @Override
                    public void callBack(String result) {
                        if (!result.equals("fail")) {
                            try {
                                Group group = new Group();
                                group.imId = toId;
                                group = Group.updateFromJSON(group, new JSONObject(result).getJSONObject("data"));
                                syncAdapterWithGroup(group);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
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

    /**
     * 按住说话listener
     */
    private PowerManager.WakeLock wakeLock;

    class PressToSpeakListen implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!CommonUtils.isExitsSdcard()) {
                        Utils.toast(context, Config.NOTE_RECORD_FAIL_NO_STORAGE_CARD);
                        return false;
                    }
                    try {
                        v.setPressed(true);
                        wakeLock.acquire();
                        if (VoicePlayClickListener.isPlaying)
                            VoicePlayClickListener.currentPlayListener.stopPlayVoice();
                        recordingContainer.setVisibility(View.VISIBLE);
                        recordingHint.setText(Config.NOTE_RECORD_SLIDE_UP_CANCEL);
                        recordingHint.setBackgroundColor(Color.TRANSPARENT);
                        voiceRecorder.startRecording(null, toId, getApplicationContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                        v.setPressed(false);
                        if (wakeLock.isHeld())
                            wakeLock.release();
                        if (voiceRecorder != null)
                            voiceRecorder.discardRecording();
                        recordingContainer.setVisibility(View.INVISIBLE);
                        Utils.toast(context, Config.NOTE_RECORD_FAIL);
                        return false;
                    }

                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (event.getY() < 0) {
                        recordingHint.setText(Config.NOTE_RECORD_RELEASE_CANCEL);
                        recordingHint.setBackgroundResource(R.drawable.recording_text_hint_bg);
                    } else {
                        recordingHint.setText(Config.NOTE_RECORD_SLIDE_UP_CANCEL);
                        recordingHint.setBackgroundColor(Color.TRANSPARENT);
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    v.setPressed(false);
                    recordingContainer.setVisibility(View.INVISIBLE);
                    if (wakeLock.isHeld())
                        wakeLock.release();
                    if (event.getY() < 0) {
                        // discard the recorded audio.
                        voiceRecorder.discardRecording();

                    } else {
                        // stop recording and send voice file
                        String st1 = Config.NOTE_RECORD_NO_PERMISSION;
                        String st2 = Config.NOTE_RECORD_TOO_SHORT;
                        String st3 = Config.NOTE_RECORD_SEND_FAIL;
                        try {
                            int length = voiceRecorder.stopRecoding();
                            if (length > 0) {
                                sendVoice(voiceRecorder.getVoiceFilePath(), voiceRecorder.getVoiceFileName(toId),
                                        Integer.toString(length), false);
                            } else if (length == EMError.INVALID_FILE) {
                                Utils.toast(context, st1);
                            } else {
                                Utils.toast(context, st2);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Utils.toast(context, st3);
                        }

                    }
                    return true;
                default:
                    recordingContainer.setVisibility(View.INVISIBLE);
                    if (voiceRecorder != null)
                        voiceRecorder.discardRecording();
                    return false;
            }
        }
    }

    /**
     * 监测群组解散或者被T事件
     */
    class GroupListener extends GroupRemoveListener {

        @Override
        public void onUserRemoved(final String groupId, String groupName) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (toId.equals(groupId)) {
                        Utils.toast(context, Config.NOTE_GROUP_REMOVED_BY_ADMIN);
                        if (GroupProfileActivity.instance != null)
                            GroupProfileActivity.instance.finish();
                        finish();
                    }
                }
            });
        }

        @Override
        public void onGroupDestroy(final String groupId, String groupName) {
            // 群组解散正好在此页面，提示群组被解散，并finish此页面
            runOnUiThread(new Runnable() {
                public void run() {
                    if (toId.equals(groupId)) {
                        Utils.toast(context, Config.NOTE_GROUP_REMOVED_BY_ADMIN);
                        if (GroupProfileActivity.instance != null)
                            GroupProfileActivity.instance.finish();
                        finish();
                    }
                }
            });
        }

    }
}
