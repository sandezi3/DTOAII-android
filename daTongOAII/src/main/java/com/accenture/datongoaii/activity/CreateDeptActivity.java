package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.content.Context;
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
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;


public class CreateDeptActivity extends Activity implements View.OnClickListener {
    private Context context;
    private Dept parent;

    private EditText etName;
    private View btnSelect;
    private View btnBack;
    private View btnCreate;

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
    }

    @Override
    public void onClick(View view) {
        if (view.equals(btnSelect)) {
            Intent intent = new Intent(view.getContext(), SelectDeptActivity.class);
            startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_DEPT);
            return;
        }
        if (view.equals(btnBack)) {
            finish();
            return;
        }
        if (view.equals(btnCreate)) {
            if (isDataValid()) {
                String name = etName.getEditableText().toString().trim();
                Integer orgId = Account.getInstance().getOrg().orgId;
                startCreateDeptConnect(name, parent.id, orgId);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_SELECT_DEPT
                && resultCode == RESULT_OK) {
            parent = (Dept) data.getSerializableExtra(Constants.BUNDLE_TAG_SELECT_DEPT);
            TextView tvValue = (TextView) findViewById(R.id.tvValue);
            tvValue.setText(parent.name);
        }
    }

    // Private
    private boolean isDataValid() {
        String name = etName.getEditableText().toString().trim();
        if (name.length() == 0) {
            Utils.toast(this, Config.NOTE_DEPT_NAME_EMPTY);
            etName.requestFocus();
            return false;
        }
        if (parent == null) {
            Utils.toast(this, Config.NOTE_SELECT_PARENT_DEPT);
            return false;
        }
        return true;
    }

    // Request & Response
    public void startCreateDeptConnect(String name, Integer parentDeptId, Integer orgId) {
        String url = Config.SERVER_HOST + Config.URL_CREATE_DEPT;
        JSONObject obj = new JSONObject();
        try {
            obj.put("deptName", name);
            obj.put("parentDeptId", parentDeptId);
            obj.put("orgId", orgId);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        Logger.i("CreateDeptActivity", "startCreateDeptConnect");
        new HttpConnection().post(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
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
