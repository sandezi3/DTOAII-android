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
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class ManageDeptActivity extends Activity implements View.OnClickListener {
    private Context context;
    private Dept mDept;
    private Dept parent;
    private Dept tmpParent;

    private View btnBack;
    private View btnDeptName;
    private TextView tvDeptName;
    private View btnParent;
    private TextView tvParentName;
    private View btnDismiss;
    private ProgressDialog progressDialog;
    private Handler handler = new ActivityHandler(this);

    public static class ActivityHandler extends Handler {
        WeakReference<ManageDeptActivity> mActivity;

        ActivityHandler(ManageDeptActivity activity) {
            this.mActivity = new WeakReference<ManageDeptActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    ManageDeptActivity a = mActivity.get();
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
        setContentView(R.layout.activity_manage_dept);

        btnBack = findViewById(R.id.btnBack);
        btnDeptName = findViewById(R.id.btnDeptName);
        tvDeptName = (TextView) findViewById(R.id.tvDeptName);
        btnParent = findViewById(R.id.btnParent);
        tvParentName = (TextView) findViewById(R.id.tvParentName);
        btnDismiss = findViewById(R.id.btnDismiss);

        btnBack.setOnClickListener(this);
        btnDeptName.setOnClickListener(this);
        btnParent.setOnClickListener(this);
        btnDismiss.setOnClickListener(this);

        mDept = (Dept) getIntent().getSerializableExtra(Constants.BUNDLE_TAG_MANAGE_DEPT);
        tvDeptName.setText(mDept.name);
        assert (mDept.parent != null);
        parent = mDept.parent;
        tvParentName.setText(parent.name);
    }


    @Override
    public void onClick(View view) {
        if (view.equals(btnBack)) {
            finish();
            return;
        }
        if (view.equals(btnDeptName)) {
            final EditText etName = new EditText(context);
            etName.setBackgroundColor(getResources().getColor(R.color.white));
            etName.setText(tvDeptName.getText());
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(etName)
                    .setCancelable(false)
                    .setPositiveButton(getResources().getText(R.string.btn_modify), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startChangeDeptNameConnect(mDept.id, etName.getEditableText().toString().trim());
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
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_DEPT_MULTI_MODE, false);
            intent.putExtra(Constants.BUNDLE_TAG_PARENT_DEPT_INVALID, mDept.id);
            Bundle bundle = new Bundle();
            List<Dept> list = new ArrayList<Dept>();
            list.add(parent);
            bundle.putSerializable(Constants.BUNDLE_TAG_MANAGE_DEPT_SELECT_PARENT, (Serializable) list);
            intent.putExtras(bundle);
            this.startActivityForResult(intent, Constants.REQUEST_CODE_CHANGE_DEPT_PARENT);
            return;
        }
        if (view.equals(btnDismiss)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(Config.ALERT_DELETE_DEPT)
                    .setCancelable(false)
                    .setPositiveButton(getResources().getText(R.string.btn_delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startDeleteDeptConnect(mDept.id);
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
            List<Dept> list = (List<Dept>) data.getSerializableExtra(Constants.BUNDLE_TAG_SELECT_DEPT);
            if (list != null && list.size() > 0) {
                tmpParent = list.get(0);
            }
            if (mDept.id.equals(tmpParent.id)) {
                Utils.toast(context, Config.NOTE_DEPT_PARENT_SELF);
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(Config.ALERT_PARENT_DEPT.replace("{}", "\"" + tmpParent.name + "\""))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getText(R.string.btn_confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startChangeParentDeptConnect(mDept.id, tmpParent.id);
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

    private void startChangeParentDeptConnect(Integer sourceId, Integer parentId) {
        String url = Config.SERVER_HOST + Config.URL_MODIFY_DEPT.replace("{groupId}", sourceId + "");
        JSONObject obj = new JSONObject();
        try {
            obj.put("parentGroupId", parentId);
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
                            ((Activity) context).setResult(RESULT_FIRST_USER);
                            Utils.toast(context, Config.SUCCESS_UPDATE);
                            parent = tmpParent;
                            tvParentName.setText(parent.name);
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
    private void startChangeDeptNameConnect(Integer deptId, final String name) {
        String url = Config.SERVER_HOST + Config.URL_MODIFY_DEPT.replace("{groupId}", deptId + "");
        JSONObject obj = new JSONObject();
        try {
            obj.put("groupName", name);
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
                            tvDeptName.setText(name);
                            Intent intent = new Intent();
                            intent.putExtra(Constants.BUNDLE_TAG_RENAME_DEPT_NEW_NAME, name);
                            ((Activity) context).setResult(RESULT_OK, intent);
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

    private void startDeleteDeptConnect(Integer deptId) {
        String url = Config.SERVER_HOST + Config.URL_DELETE_DEPT.replace("{groupId}", deptId + "");
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
                            ((Activity) context).setResult(Activity.RESULT_FIRST_USER);
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
