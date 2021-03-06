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
import android.widget.EditText;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ManageUserActivity extends Activity implements View.OnClickListener {
    private Context context;
    private Contact mContact;
    private List<Dept> tmpParents;

    private View btnBack;
    private View btnUsername;
    private TextView tvUsername;
    private View btnParent;
    private TextView tvParentName;
    private View btnDismiss;
    private ProgressDialog progressDialog;

    private Handler handler = new ActivityHandler(this);

    static class ActivityHandler extends Handler {
        WeakReference<ManageUserActivity> mActivity;

        ActivityHandler(ManageUserActivity activity) {
            this.mActivity = new WeakReference<ManageUserActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    ManageUserActivity a = mActivity.get();
                    if (a.progressDialog != null && a.progressDialog.isShowing()) {
                        a.progressDialog.dismiss();
                        a.progressDialog = null;
                    }
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.activity_manage_user);

        btnBack = findViewById(R.id.btnBack);
        btnUsername = findViewById(R.id.btnUsername);
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        btnParent = findViewById(R.id.btnParent);
        tvParentName = (TextView) findViewById(R.id.tvParentName);
        btnDismiss = findViewById(R.id.btnDismiss);

        btnBack.setOnClickListener(this);
        btnUsername.setOnClickListener(this);
        btnParent.setOnClickListener(this);
        btnDismiss.setOnClickListener(this);

        mContact = (Contact) getIntent().getSerializableExtra(Constants.BUNDLE_TAG_MANAGE_USER);
        tvUsername.setText(mContact.name);

        getParents(Account.getInstance().getCreatedOrg().orgId, mContact.id);
    }

    private String getParentName(List<Dept> parentDepts) {
        if (parentDepts.size() > 1) {
            String name = "";
            for (Dept d : parentDepts) {
                name += d.name + ",";
            }
            return name.substring(0, name.length() - 1);
        } else if (parentDepts.size() == 1) {
            return parentDepts.get(0).name;
        }
        return "";
    }

    @Override
    public void onClick(View view) {
        if (view.equals(btnBack)) {
            finish();
            return;
        }
        if (view.equals(btnUsername)) {
            final EditText etName = new EditText(context);
            etName.setBackgroundColor(getResources().getColor(R.color.white));
            etName.setText(tvUsername.getText());
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(etName)
                    .setCancelable(false)
                    .setPositiveButton("更改", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String name = etName.getEditableText().toString().trim();
                            if (name.length() == 0) {
                                Utils.toast(context, Config.NOTE_USERNAME_EMPTY);
                                return;
                            }
                            startChangeUserNameConnect(mContact.id, name);
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
            return;
        }
        if (view.equals(btnParent)) {
            Intent intent = new Intent(context, SelectDeptActivity.class);
            intent.putExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_NAME, Account.getInstance().getCreatedOrg().orgName);
            intent.putExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_ID, Account.getInstance().getCreatedOrg().orgId);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_DEPT_MULTI_MODE, true);
            this.startActivityForResult(intent, Constants.REQUEST_CODE_CHANGE_DEPT_PARENT);
            return;
        }
        if (view.equals(btnDismiss)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(Config.ALERT_DELETE_USER)
                    .setCancelable(false)
                    .setPositiveButton(getResources().getText(R.string.btn_delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startDeleteDeptUserConnect(mContact.parent.id, mContact.id);
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
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CHANGE_DEPT_PARENT && resultCode == RESULT_OK) {
            this.setResult(Activity.RESULT_OK);
            tmpParents = (List<Dept>) data.getSerializableExtra(Constants.BUNDLE_TAG_SELECT_DEPT);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(Config.ALERT_PARENT_DEPT.replace("{}", "\"" + getParentName(tmpParents) + "\""))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getText(R.string.btn_confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startChangeParentDeptConnect(Account.getInstance().getCreatedOrg().orgId, mContact.id, tmpParents);
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
    }

    private void getParents(Integer orgId, Integer userId) {
        String url = Config.SERVER_HOST + Config.URL_GET_USER_PARENT.replace("{userId}", userId + "").replace("{rootGroupId}", orgId + "");
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_GET);
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            mContact.parents = Dept.parentsFromJSON(new JSONObject(result));
                            tvParentName.setText(getParentName(mContact.parents));
                        } else {
                            Utils.toast(context, cr.statusMsg);
                        }
                    } catch (JSONException e) {
                        Utils.toast(context, Config.ERROR_INTERFACE);
                    }
                } else {
                    Utils.toast(context, Config.ERROR_NETWORK);
                }
            }
        });
    }

    private void startChangeParentDeptConnect(Integer orgId, Integer userId, List<Dept> destParents) {
        String url = Config.SERVER_HOST + Config.URL_MODIFY_USER.replace("{userId}", userId + "");
        JSONObject obj = new JSONObject();
        List<Integer> ids = new ArrayList<Integer>();
        for (Dept dept : destParents) {
            ids.add(dept.id);
        }
        try {
            JSONArray array = new JSONArray(ids);
            obj.put("groupIds", array);
            obj.put("rootGroupId", orgId);
        } catch (JSONException e) {
            Utils.toast(context, Config.ERROR_APP);
            return;
        }
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_SUBMIT);
        new HttpConnection().put(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            ((Activity) context).setResult(RESULT_OK);
                            Utils.toast(context, Config.SUCCESS_UPDATE);
                            getParents(Account.getInstance().getCreatedOrg().orgId, mContact.id);
                        } else {
                            Utils.toast(context, cr.statusMsg);
                        }
                    } catch (JSONException e) {
                        Utils.toast(context, Config.ERROR_INTERFACE);
                    }
                } else {
                    Utils.toast(context, Config.ERROR_NETWORK);
                }
            }
        });
    }


    // 网络请求
    private void startChangeUserNameConnect(Integer userId, final String name) {
        String url = Config.SERVER_HOST + Config.URL_MODIFY_USER.replace("{userId}", userId + "");
        JSONObject obj = new JSONObject();
        try {
            obj.put("username", name);
        } catch (JSONException e) {
            Utils.toast(context, Config.ERROR_APP);
            return;
        }
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_SUBMIT);
        new HttpConnection().put(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            ((Activity) context).setResult(RESULT_OK);
                            tvUsername.setText(name);
                            Utils.toast(context, Config.SUCCESS_UPDATE);
                        } else {
                            Utils.toast(context, cr.statusMsg);
                        }
                    } catch (JSONException e) {
                        Utils.toast(context, Config.ERROR_INTERFACE);
                    }
                } else {
                    Utils.toast(context, Config.ERROR_NETWORK);
                }
            }
        });
    }

    private void startDeleteDeptUserConnect(Integer deptId, Integer userId) {
        String url = Config.SERVER_HOST + Config.URL_DELETE_USER.replace("{groupId}", deptId + "").replace("{userId}", userId + "");
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_SUBMIT);
        new HttpConnection().delete(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            Utils.toast(context, Config.SUCCESS_DELETE);
                            ((Activity) context).setResult(Activity.RESULT_OK);
                            ((Activity) context).finish();
                        } else {
                            Utils.toast(context, cr.statusMsg);
                        }
                    } catch (JSONException e) {
                        Utils.toast(context, Config.ERROR_INTERFACE);
                    }
                } else {
                    Utils.toast(context, Config.ERROR_NETWORK);
                }
            }
        });
    }
}
