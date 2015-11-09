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
import android.widget.BaseAdapter;
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
import com.accenture.datongoaii.adapter.AppGridAdapter;
import com.accenture.datongoaii.adapter.UserGridAdapter;
import com.accenture.datongoaii.db.GroupDao;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.App;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Group;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;
import com.accenture.datongoaii.vendor.HX.activity.ChatActivity;
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
    public static GroupProfileActivity instance;

    private static final String TAG = "GroupProfileActivity";
    private Context context;
    private Group mGroup;
    private List<Contact> userList;
    private UserGridAdapter userAdapter;
    private TextView tvName;
    private GridView gvUsers;
    private List<App> appList;
    private AppGridAdapter appAdapter;
    private GridView gvApps;
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
        instance = this;
        isAdmin = false;

        ImageView ivHead = (ImageView) findViewById(R.id.ivHead);
        tvName = (TextView) findViewById(R.id.tvName);
        gvUsers = (GridView) findViewById(R.id.gvUsers);
        gvApps = (GridView) findViewById(R.id.gvApps);

        mGroup = (Group) getIntent().getSerializableExtra(Constants.BUNDLE_TAG_GROUP_PROFILE);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(mGroup.img, ivHead, Config.getDisplayOptions());
        tvName.setText(mGroup.name);

        userList = new ArrayList<Contact>();
        syncData();
        userAdapter = new UserGridAdapter(context, userList, false);
        gvUsers.setAdapter(userAdapter);
        gvUsers.setOnItemClickListener(this);

        appList = new ArrayList<App>();
        appAdapter = new AppGridAdapter(context, appList);
        gvApps.setAdapter(appAdapter);
        gvApps.setOnItemClickListener(this);

        findViewById(R.id.btnBack).setOnClickListener(this);
        findViewById(R.id.btnChat).setOnClickListener(this);
        findViewById(R.id.btnQuit).setOnClickListener(this);

        if (mGroup.imId == null || mGroup.imId.length() == 0) {
            findViewById(R.id.btnSendMessage).setVisibility(View.INVISIBLE);
            Utils.toast(context, Config.ERROR_IM);
        }
        refreshGroup();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_GROUP_INVITE_MEMBER && resultCode == RESULT_OK) {
            List<Contact> selectedUsers = (List<Contact>) data.getSerializableExtra(Constants.BUNDLE_TAG_SELECT_USER_MULTI_MODE_RESULT);
            handleExistedMembers(selectedUsers, mGroup.contactList);
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
                            .setPositiveButton(getResources().getText(R.string.btn_dismiss), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startDismissGroup(mGroup.imId);
                                }
                            })
                            .setNegativeButton(getResources().getText(R.string.btn_cancel), new DialogInterface.OnClickListener() {
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
                            .setPositiveButton(getResources().getText(R.string.btn_quit), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startQuitGroup(mGroup.imId, Account.getInstance().getImId());
                                }
                            })
                            .setNegativeButton(getResources().getText(R.string.btn_cancel), new DialogInterface.OnClickListener() {
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
        if (parent.getAdapter().equals(userAdapter)) {
            if (position == userAdapter.getCount() - 1) {
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
            if (userAdapter.delectable) {
                final Contact user = (Contact) userAdapter.getItem(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(Config.ALERT_KICK_MEMBER.replace("{}", user.name))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getText(R.string.btn_remove), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startQuitGroup(mGroup.imId, user.imId);
                            }
                        })
                        .setNegativeButton(getResources().getText(R.string.btn_cancel), null)
                        .show();
                builder.create();
            }
        } else if (parent.getAdapter().equals(appAdapter)) {
            App app = appList.get(position);
            Intent intent = new Intent(context, AppActivity.class);
            intent.putExtra(Constants.BUNDLE_TAG_APP, app);
            startActivity(intent);
            finish();
        }
    }

    private void syncData() {
        userList.clear();
        if (mGroup.contactList != null) {
            userList.addAll(mGroup.contactList);
        }
        Contact addBtn = new Contact();
        addBtn.id = Contact.CONTACT_BUTTON_INVALID_ID;
        userList.add(addBtn);
    }

    private void adjustHeight(GridView gridView, BaseAdapter adapter) {
        gridView.measure(0, 0);
        int col = 4;
        int totalHeight = gridView.getMeasuredHeight();
        int space = (int) getResources().getDimension(R.dimen.phone_group_user_grid_vertical_space_height);
        for (int i = 4; i < adapter.getCount(); i += col) {
            View view = adapter.getView(0, null, gridView);
            view.measure(0, 0);
            totalHeight += view.getMeasuredHeight();
            totalHeight += space;
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) gridView.getLayoutParams();
        params.height = totalHeight + space;
        gridView.setLayoutParams(params);
    }

    private void refreshUIByAdmin() {
        if (isAdmin) {
            ((TextView) findViewById(R.id.btnQuit)).setText(getResources().getText(R.string.btn_dismiss_group));
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
                            .setPositiveButton(getResources().getText(R.string.btn_modify), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startRenameGroup(mGroup.imId, etName.getEditableText().toString().trim());
                                }
                            })
                            .setNegativeButton(getResources().getText(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                    builder.create();
                }
            });
            userAdapter.delectable = true;
        }
    }

    private void refreshUI() {
        isAdmin = mGroup.owner.imId.equals(Account.getInstance().getImId());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshUIByAdmin();
                syncData();
                appAdapter.notifyDataSetChanged();
                userAdapter.notifyDataSetChanged();
                adjustHeight(gvUsers, userAdapter);
                adjustHeight(gvApps, appAdapter);
            }
        });
    }

    private void handleExistedMembers(List<Contact> list, List<Contact> members) {
        for (int i = list.size() - 1; i >= 0; i--) {
            Contact contact = list.get(i);
            if (Contact.contains(members, contact)) {
                list.remove(i);
            }
        }
    }

    // 网络访问
    private void refreshGroup() {
        GroupDao dao = new GroupDao(context);
        if (dao.isExisted(mGroup)) {
            Group group = dao.getById(mGroup.id);
            if (group.owner != null) {
                mGroup = group;
                refreshUI();
//                return;
            }
        }
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_GET);
        DTOARequest.startGetGroup(mGroup.imId, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            mGroup = Group.updateFromJSON(mGroup, new JSONObject(result).getJSONObject("data"));
                            GroupDao dao = new GroupDao(context);
                            if (dao.isExisted(mGroup)) {
                                dao.update(mGroup);
                            } else {
                                // should not come to here
                                dao.save(mGroup);
                            }
                            List<App> list = App.listFromJSON(new JSONObject(result).getJSONObject("data"));
                            if (list != null && list.size() > 0) {
                                appList.clear();
                                appList.addAll(list);
                            }
                            refreshUI();
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
                            mGroup.name = name;
                            new GroupDao(context).update(mGroup);
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

    private void startQuitGroup(String groupId, final String imId) {
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
                            GroupDao dao = new GroupDao(context);
                            if (isAdmin) {
                                // 管理员 踢人调用此方法
                                Group.removeMemberByImId(mGroup, imId);
                                mGroup.userNum--;
                                if (dao.isExisted(mGroup)) {
                                    dao.update(mGroup);
                                }
                                refreshGroup();
                            } else {
                                // 普通用户 退群调用此方法
                                dao.delete(mGroup);
                                dao.deleteFromMyGroups(mGroup);
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

    private void startInviteGroupMembers(String groupId, final List<Contact> list) {
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
                            mGroup.owner = null;
                            new GroupDao(context).update(mGroup);
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
                            GroupDao dao = new GroupDao(context);
                            dao.delete(mGroup);
                            dao.deleteFromMyGroups(mGroup);
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
