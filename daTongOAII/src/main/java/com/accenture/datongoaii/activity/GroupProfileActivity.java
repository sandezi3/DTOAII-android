package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.DTOARequest;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.adapter.UserGridAdapter;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Group;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;
import com.accenture.datongoaii.vendor.HX.ChatActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 10/3/15.
 * 群聊详情
 */
public class GroupProfileActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "GroupProfileActivity";
    private Context context;
    private Group mGroup;
    private List<Contact> dataList;
    private UserGridAdapter adapter;
    private TextView tvName;
    private GridView gvUsers;
    private boolean isAdmin;
    private ProgressDialog progressDialog;
    private Handler handler = new ActivityHandler(this);

    static class ActivityHandler extends Handler {
        WeakReference<GroupProfileActivity> mActivity;

        public ActivityHandler(GroupProfileActivity activity) {
            this.mActivity = new WeakReference<GroupProfileActivity>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            GroupProfileActivity a = mActivity.get();
            switch (message.what) {
                case Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    if (a.progressDialog != null) {
                        a.progressDialog.dismiss();
                        a.progressDialog = null;
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);
        context = this;
        isAdmin = false;

        ImageView ivHead = (ImageView) findViewById(R.id.ivHead);
        tvName = (TextView) findViewById(R.id.tvName);
        gvUsers = (GridView) findViewById(R.id.gvUsers);

        mGroup = (Group) getIntent().getSerializableExtra(Constants.BUNDLE_TAG_GROUP_PROFILE);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(mGroup.img, ivHead, Config.getDisplayOptions());
        tvName.setText(mGroup.name);

        dataList = new ArrayList<Contact>();
        syncData();
        adapter = new UserGridAdapter(context, dataList, false);
        gvUsers.setAdapter(adapter);
        gvUsers.setOnItemClickListener(this);

        findViewById(R.id.btnBack).setOnClickListener(this);
        findViewById(R.id.btnChat).setOnClickListener(this);
        findViewById(R.id.btnQuit).setOnClickListener(this);

        if (mGroup.imId == null || mGroup.imId.length() == 0) {
            findViewById(R.id.btnSendMessage).setVisibility(View.INVISIBLE);
            Utils.toast(context, Config.ERROR_IM);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshGroup();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_GROUP_INVITE_MEMBER && resultCode == RESULT_OK) {
            List<Contact> selectedUsers = (List<Contact>) data.getSerializableExtra(Constants.BUNDLE_TAG_SELECT_USER_MULTI_MODE_RESULT);
            if (selectedUsers != null && selectedUsers.size() > 0) {
                startInviteGroupMembers(mGroup.imId, selectedUsers);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                finish();
                break;
            case R.id.btnChat: {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("groupId", mGroup.imId);
                intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                startActivity(intent);
                finish();
            }
            break;
            case R.id.btnQuit:
                if (isAdmin) {
                    // 解散群
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(Config.ALERT_DISMISS_GROUP)
                            .setCancelable(false)
                            .setPositiveButton("解散", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startDismissGroup(mGroup.imId);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                    builder.create();
                } else {
                    // 退出群
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(Config.ALERT_QUIT_GROUP)
                            .setCancelable(false)
                            .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startQuitGroup(mGroup.imId, Account.getInstance().getImId());
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                    builder.create();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == adapter.getCount() - 1) {
            /**
             * 邀请成员，多选
             * 初始状态：选择0个成员
             */
            List<Contact> selectedUsers = new ArrayList<Contact>();
            Intent intent = new Intent(context, SelectUserActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_TAG_SELECT_USER_MULTI_MODE_RESULT, (Serializable) selectedUsers);
            intent.putExtras(bundle);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_MULTI_MODE, true);
            startActivityForResult(intent, Constants.REQUEST_CODE_GROUP_INVITE_MEMBER);
            return;
        }
        if (adapter.delectable) {
            final Contact user = (Contact) adapter.getItem(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(Config.ALERT_KICK_MEMBER.replace("{}", user.name))
                    .setCancelable(false)
                    .setPositiveButton("移除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startQuitGroup(mGroup.imId, user.imId);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
            builder.create();
        }
    }

    private void syncData() {
        dataList.clear();
        dataList.addAll(mGroup.contactList);
        Contact addBtn = new Contact();
        addBtn.id = Contact.CONTACT_BUTTON_INVALID_ID;
        dataList.add(addBtn);
    }

    private void adjustHeight() {
        gvUsers.measure(0, 0);
        int col = 4;
        int totalHeight = gvUsers.getMeasuredHeight();
        int space = (int) getResources().getDimension(R.dimen.phone_group_user_grid_vertical_space_height);
        for (int i = 4; i < adapter.getCount(); i += col) {
            View view = adapter.getView(0, null, gvUsers);
            view.measure(0, 0);
            totalHeight += view.getMeasuredHeight();
            totalHeight += space;
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) gvUsers.getLayoutParams();
        params.height = totalHeight;
        gvUsers.setLayoutParams(params);
    }

    private void refreshUIByAdmin() {
        if (isAdmin) {
            ((TextView) findViewById(R.id.btnQuit)).setText("解散该群");
            findViewById(R.id.ivArrow1).setVisibility(View.VISIBLE);
            findViewById(R.id.btnRename).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final EditText etName = new EditText(context);
                    etName.setBackgroundColor(getResources().getColor(R.color.white));
                    etName.setText(tvName.getText());
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(etName)
                            .setTitle(Config.ALERT_RENAME_GROUP)
                            .setCancelable(false)
                            .setPositiveButton("更改", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startRenameGroup(mGroup.imId, etName.getEditableText().toString().trim());
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                    builder.create();
                }
            });
            adapter.delectable = true;
        }
    }

    // 网络访问
    private void refreshGroup() {
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_GET);
        DTOARequest.startGetGroup(mGroup.imId, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            mGroup = Group.updateFromJSON(mGroup, new JSONObject(result).getJSONObject("data"));
                            isAdmin = mGroup.owner.imId.equals(Account.getInstance().getImId());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refreshUIByAdmin();
                                    syncData();
                                    adapter.notifyDataSetChanged();
                                    adjustHeight();
                                }
                            });
                            return;
                        }
                    } catch (JSONException e) {
                        Logger.e(TAG, e.getMessage());
                        Utils.toast(context, Config.ERROR_INTERFACE);
                    }
                }
                Utils.toast(context, Config.ERROR_NETWORK);
            }
        });
    }

    private void startRenameGroup(String groupId, final String name) {
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_SUBMIT);
        DTOARequest.startRenameGroup(groupId, name, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            Utils.toast(context, Config.SUCCESS_UPDATE);
                            setResult(RESULT_OK);
                            tvName.setText(name);
                            return;
                        }
                        Utils.toast(context, cr.statusMsg);
                    } catch (JSONException e) {
                        Logger.e(TAG, e.getMessage());
                        Utils.toast(context, Config.ERROR_INTERFACE);
                    }
                }
                Utils.toast(context, Config.ERROR_NETWORK);
            }
        });
    }

    private void startQuitGroup(String groupId, String imId) {
        String msg = isAdmin ? Config.PROGRESS_SUBMIT : Config.PROGRESS_QUIT;
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, msg);
        DTOARequest.startQuitGroup(groupId, imId, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            String msg = isAdmin ? Config.SUCCESS_UPDATE : Config.SUCCESS_QUIT;
                            Utils.toast(context, msg);
                            setResult(RESULT_OK);
                            if (isAdmin) {
                                // 管理员 踢人调用此方法
                                refreshGroup();
                            } else {
                                // 普通用户 退群调用此方法
                                finish();
                            }
                            return;
                        }
                        Utils.toast(context, cr.statusMsg);
                    } catch (JSONException e) {
                        Logger.e(TAG, e.getMessage());
                        Utils.toast(context, Config.ERROR_INTERFACE);
                    }
                }
                Utils.toast(context, Config.ERROR_NETWORK);
            }
        });
    }

    private void startInviteGroupMembers(String groupId, List<Contact> list) {
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_SEND);
        DTOARequest.startInviteGroupMembers(groupId, list, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            Utils.toast(context, Config.SUCCESS_INVITE);
                            setResult(RESULT_OK);
                            refreshGroup();
                            return;
                        }
                        Utils.toast(context, cr.statusMsg);
                    } catch (JSONException e) {
                        Logger.e(TAG, e.getMessage());
                        Utils.toast(context, Config.ERROR_INTERFACE);
                    }
                }
                Utils.toast(context, Config.ERROR_NETWORK);
            }
        });
    }

    private void startDismissGroup(String groupId) {
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_SUBMIT);
        DTOARequest.startDismissGroup(groupId, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            Utils.toast(context, Config.SUCCESS_DISMISS);
                            setResult(RESULT_OK);
                            finish();
                            return;
                        }
                        Utils.toast(context, cr.statusMsg);
                    } catch (JSONException e) {
                        Logger.e(TAG, e.getMessage());
                        Utils.toast(context, Config.ERROR_INTERFACE);
                    }
                }
                Utils.toast(context, Config.ERROR_NETWORK);
            }
        });
    }

}
