package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class CreateDeptActivity extends Activity implements View.OnClickListener {
    private Context context;
    private List<Dept> parents;

    private EditText etName;
    private View btnSelect;
    private View btnBack;
    private View btnCreate;
    private ProgressDialog progressDialog;
    private Handler handler = new ActivityHandler(this);

    public static class ActivityHandler extends Handler {
        WeakReference<CreateDeptActivity> mActivity;

        ActivityHandler(CreateDeptActivity activity) {
            this.mActivity = new WeakReference<CreateDeptActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    CreateDeptActivity a = mActivity.get();
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
        setContentView(R.layout.activity_create_dept);

        etName = (EditText) findViewById(R.id.etName);
        btnSelect = findViewById(R.id.layoutSelect);
        btnBack = findViewById(R.id.btnBack);
        btnCreate = findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnSelect.setOnClickListener(this);

        parents = new ArrayList<Dept>();

        Dept dept = new Dept();
        dept.id = getIntent().getIntExtra(Constants.BUNDLE_TAG_CREATE_DEPT_DEPT_ID, -1);
        dept.name = getIntent().getStringExtra(Constants.BUNDLE_TAG_CREATE_DEPT_DEPT_NAME);
        parents.add(dept);
        refreshParent();
    }

    @Override
    public void onClick(View view) {
        if (view.equals(btnSelect)) {
            Intent intent = new Intent(view.getContext(), SelectDeptActivity.class);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_DEPT_MULTI_MODE, false);
            intent.putExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_ID, Account.getInstance().getOrg().orgId);
            intent.putExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_NAME, Account.getInstance().getOrg().orgName);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_TAG_MANAGE_DEPT_SELECT_PARENT, (Serializable) parents);
            intent.putExtras(bundle);
            startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_DEPT);
            return;
        }
        if (view.equals(btnBack)) {
            finish();
            return;
        }
        if (view.equals(btnCreate)) {
            String name = etName.getEditableText().toString().trim();
            if (isDataValid()) {
                startCreateDeptConnect(name, parents.get(0).id, Account.getInstance().getOrg().orgId);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_SELECT_DEPT
                && resultCode == RESULT_OK) {
            parents.clear();
            parents.addAll((List<Dept>) data.getSerializableExtra(Constants.BUNDLE_TAG_SELECT_DEPT));
            refreshParent();
        }
    }

    // Private
    private void refreshParent() {
        String value = "";
        if (parents.size() > 0) {
            value += parents.get(0).name;
        }
        if (parents.size() > 1) {
            value += " 等";
        }
        TextView tvValue = (TextView) findViewById(R.id.tvValue);
        tvValue.setText(value);
    }

    private boolean isDataValid() {
        String name = etName.getEditableText().toString().trim();
        if (name.length() == 0) {
            Utils.toast(this, Config.NOTE_DEPT_NAME_EMPTY);
            etName.requestFocus();
            return false;
        }
        return true;
    }

    // Request & Response
    public void startCreateDeptConnect(String name, Integer parentDeptId, Integer orgId) {
        String url = Config.SERVER_HOST + Config.URL_CREATE_DEPT;
        JSONObject obj = new JSONObject();
        try {
            obj.put("groupName", name);
            obj.put(Config.GROUP_TYPE_TAG, Config.GROUP_TYPE_DEPT);
            if (parentDeptId != -1) {
                obj.put("parentGroupId", parentDeptId);
            }
            obj.put("rootGroupId", orgId);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        Logger.i("CreateDeptActivity", "startCreateDeptConnect");
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_SUBMIT);
        new HttpConnection().post(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr == null) {
                            Utils.toast(context, Config.ERROR_INTERFACE);
                            return;
                        }
                        if (cr.statusCode == 0) {
                            Utils.toast(context, Config.SUCCESS_CREATE);
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
