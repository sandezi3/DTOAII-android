package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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
    private List<Dept> selectParents;

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
        setContentView(R.layout.activity_manage_org);

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
        assert (mDept.parents != null);
        selectParents = mDept.parents;
        setParentName(selectParents);
    }


    @Override
    public void onClick(View view) {
        if (view.equals(btnBack)) {
            finish();
            return;
        }
        if (view.equals(btnDeptName)) {
            final EditText etName = new EditText(context);
            etName.setBackgroundColor(getResources().getColor(R.color.transparent));
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
            intent.putExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_ID, mDept.id);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_DEPT_MULTI_MODE, true);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_TAG_MANAGE_DEPT_SELECT_PARENT, (Serializable) selectParents);
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
            List<Dept> list =  (List<Dept>) data.getSerializableExtra(Constants.BUNDLE_TAG_SELECT_DEPT);
            if (list != null && list.size() > 0) {
                selectParents.clear();
                selectParents.addAll(list);
            }
            setParentName(selectParents);
        }
    }

    // 私有方法
    private void setParentName(List<Dept> list) {
        if (list.size() > 1) {
            tvParentName.setText(list.get(0).name + " 等");
        } else if (list.size() == 1) {
            tvParentName.setText(list.get(0).name);
        }
    }

    // 网络请求
    private void startChangeDeptNameConnect(Integer deptId, String name) {
        String url = Config.SERVER_HOST + Config.URL_CHANGE_DEPT_NAME.replace("{deptId}", deptId + "");
        JSONObject obj = new JSONObject();
        new HttpConnection().put(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                alertDialog.dismiss();
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            // TODO
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
        });    }

    private void startDeleteDeptConnect(Integer deptId) {
        String url = Config.SERVER_HOST + Config.URL_DELETE_DEPT.replace("{deptId}", deptId + "");
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
