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
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;


public class ManageOrgActivity extends Activity implements View.OnClickListener {
    private Context context;

    private View btnBack;
    private View btnOrgName;
    private TextView tvOrgName;
    private View btnOrg;
    private View btnDismiss;
    private ProgressDialog progressDialog;

    private Handler handler = new ActivityHandler(this);

    static class ActivityHandler extends Handler {
        WeakReference<ManageOrgActivity> mActivity;

        ActivityHandler(ManageOrgActivity activity) {
            this.mActivity = new WeakReference<ManageOrgActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    ManageOrgActivity a = mActivity.get();
                    if (a.progressDialog.isShowing()) {
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
        setContentView(R.layout.activity_manage_org);

        btnBack = findViewById(R.id.btnBack);
        btnOrgName = findViewById(R.id.btnOrgName);
        tvOrgName = (TextView) findViewById(R.id.tvOrgName);
        btnOrg = findViewById(R.id.btnOrg);
        btnDismiss = findViewById(R.id.btnDismiss);

        btnBack.setOnClickListener(this);
        btnOrgName.setOnClickListener(this);
        btnOrg.setOnClickListener(this);
        btnDismiss.setOnClickListener(this);

        tvOrgName.setText(Account.getInstance().getOrg().orgName);
    }


    @Override
    public void onClick(View view) {
        if (view.equals(btnBack)) {
            finish();
            return;
        }
        if (view.equals(btnOrgName)) {
            final EditText etName = new EditText(context);
            etName.setBackgroundColor(getResources().getColor(R.color.white));
            etName.setText(Account.getInstance().getOrg().orgName);
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(etName)
                    .setCancelable(false)
                    .setPositiveButton("更改", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String name = etName.getEditableText().toString().trim();
                            if (name.length() == 0) {
                                Utils.toast(context, Config.NOTE_ORG_NAME_EMPTY);
                                return;
                            }
                            startChangeDeptNameConnect(Account.getInstance().getOrg().orgId, name);
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
            return;
        }
        if (view.equals(btnOrg)) {
            Intent intent = new Intent(this, DeptActivity.class);
            intent.putExtra(Constants.BUNDLE_TAG_ORG_IS_MANAGE_MODE, true);
            intent.putExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_ID, Account.getInstance().getOrg().orgId);
            intent.putExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_NAME, Account.getInstance().getOrg().orgName);
            startActivity(intent);
            return;
        }
        if (view.equals(btnDismiss)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(Config.ALERT_DELETE_ORG)
                    .setCancelable(false)
                    .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startDeleteOrgConnect(Account.getInstance().getOrg().orgId);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CHANGE_ORG_NAME && resultCode == RESULT_OK) {
            this.setResult(Activity.RESULT_OK);
        }
    }

    // 网络请求
    private void startChangeDeptNameConnect(Integer deptId, final String name) {
        String url = Config.SERVER_HOST + Config.URL_MODIFY_DEPT.replace("{groupId}", deptId + "");
        JSONObject obj = new JSONObject();
        try {
            obj.put("groupName", name);
        } catch (JSONException e) {
            Utils.toast(context, Config.ERROR_APP);
            return;
        }
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_SEND);
        new HttpConnection().put(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            ((Activity) context).setResult(RESULT_OK);
                            tvOrgName.setText(name);
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

    private void startDeleteOrgConnect(Integer orgId) {
        String url = Config.SERVER_HOST + Config.URL_DELETE_ORG.replace("{groupId}", orgId + "");
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_SEND);
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
