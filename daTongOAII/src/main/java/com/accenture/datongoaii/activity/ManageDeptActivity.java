package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Config;
import com.accenture.datongoaii.util.Constants;
import com.accenture.datongoaii.util.Intepreter;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
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
    private AlertDialog alertDialog;

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
                    .setPositiveButton("更改", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startChangeDeptNameConnect(mDept.id, etName.getEditableText().toString().trim());
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
            alertDialog = builder.create();
            return;
        }
        if (view.equals(btnParent)) {
            Intent intent = new Intent(context, SelectDeptActivity.class);
            intent.putExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_NAME, Account.getInstance().getOrg().orgName);
            intent.putExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_ID, Account.getInstance().getOrg().orgId);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_DEPT_MULTI_MODE, false);
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
            builder.setMessage(Config.ALERT_DELETE_ORG)
                    .setCancelable(false)
                    .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startDeleteDeptConnect(mDept.id);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
            alertDialog = builder.create();
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
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startChangeParentDeptConnect(mDept.id, tmpParent.id);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
            alertDialog = builder.create();
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
        new HttpConnection().put(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                alertDialog.dismiss();
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            ((Activity) context).setResult(RESULT_OK);
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
        new HttpConnection().put(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                alertDialog.dismiss();
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            ((Activity) context).setResult(RESULT_OK);
                            tvDeptName.setText(name);
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
        new HttpConnection().delete(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                alertDialog.dismiss();
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