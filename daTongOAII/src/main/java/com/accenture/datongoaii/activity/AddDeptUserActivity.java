package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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


public class AddDeptUserActivity extends Activity implements View.OnClickListener {
    public Dept mDept;

    private Context context;
    private EditText etName;
    private EditText etCell;
    private View ibSelect;
    private Button btnAdd;

    private View btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.activity_add_dept_user);

        btnBack = findViewById(R.id.btnBack);
        etName = (EditText) findViewById(R.id.etName);
        etCell = (EditText) findViewById(R.id.etCell);
        TextView tvDept = (TextView) findViewById(R.id.tvDept);
        ibSelect = findViewById(R.id.ibSelect);
        btnAdd = (Button) findViewById(R.id.btnAdd);

        btnBack.setOnClickListener(this);
        ibSelect.setOnClickListener(this);
        btnAdd.setOnClickListener(this);

        mDept = (Dept) getIntent().getSerializableExtra(Constants.BUNDLE_TAG_ADD_DEPT_USER);
        tvDept.setText(mDept.name);
    }

    @Override
    public void onClick(View view) {
        if (view.equals(btnBack)) {
            finish();
            return;
        }
        if (view.equals(ibSelect)) {
            Intent intent = new Intent(context, SelectUserActivity.class);
            startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_USER);
            return;
        }
        if (view.equals(btnAdd)) {
            if (isDataValid()) {
                String cell = etCell.getEditableText().toString().trim();
                String name = etName.getEditableText().toString().trim();
                startAddUserToDeptConnect(Account.getInstance().getOrg().orgId, mDept.id, cell, name);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_SELECT_USER && resultCode == RESULT_OK) {
            this.setResult(Activity.RESULT_OK);
            if (data.hasExtra(Constants.BUNDLE_TAG_SELECT_USER_NAME) && data.hasExtra(Constants.BUNDLE_TAG_SELECT_USER_CELL)) {
                String cell = data.getStringExtra(Constants.BUNDLE_TAG_SELECT_USER_CELL);
                String name = data.getStringExtra(Constants.BUNDLE_TAG_SELECT_USER_NAME);
                etCell.setText(cell);
                etName.setText(name);
            }
        }
    }

    // 私有方法

    private boolean isDataValid() {
        String cell = etCell.getEditableText().toString().trim();
        String name = etName.getEditableText().toString().trim();
        if (cell.length() == 0) {
            Utils.toast(this, Config.NOTE_CELL_EMPTY);
            etCell.requestFocus();
            return false;
        }
        if (cell.length() != 11) {
            Utils.toast(this, Config.NOTE_CELL_NUMBER);
            etCell.requestFocus();
            return false;
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            Long.parseLong(cell);
        } catch (Exception e) {
            e.printStackTrace();
            etCell.requestFocus();
            Utils.toast(this, Config.NOTE_CELL_FORMAT);
            etCell.requestFocus();
            return false;
        }
        if (name.length() == 0) {
            Utils.toast(this, Config.NOTE_DEPT_NAME_EMPTY);
            etName.requestFocus();
            return false;
        }
        return true;
    }

    // 网络数据
    private void startAddUserToDeptConnect(Integer orgId, Integer deptId, String cell, String name) {
        String url = Config.SERVER_HOST + Config.URL_ADD_DEPT_USER;
        JSONObject obj = new JSONObject();
        try {
            obj.put("deptName", name);
            obj.put("orgId", orgId);
            obj.put("cell", cell);
            obj.put("deptId", deptId);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        Logger.i("AddDeptUserActivity", "startAddUserToDeptConnect");
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
                            Utils.toast(context, Config.SUCCESS_ADD);
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
