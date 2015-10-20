package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Jsons.JsonAccount;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class RegisterActivity extends Activity implements OnClickListener {
    private EditText etUsername;
    private EditText editPassword;
    private EditText etRptPswd;
    private ProgressDialog progressDialog;

    private String mCell;

    static class RegisterActivityHandler extends Handler {
        WeakReference<RegisterActivity> mActivity;

        RegisterActivityHandler(RegisterActivity activity) {
            mActivity = new WeakReference<RegisterActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            RegisterActivity theActivity = mActivity.get();
            switch (msg.what) {
                case Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    if (theActivity.progressDialog != null) {
                        theActivity.progressDialog.dismiss();
                        theActivity.progressDialog = null;
                    }
                    break;
            }
        }
    }

    private Handler handler = new RegisterActivityHandler(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = (EditText) findViewById(R.id.editUsername);
        editPassword = (EditText) findViewById(R.id.editPassword);
        etRptPswd = (EditText) findViewById(R.id.editRptPassword);

        findViewById(R.id.btnRegister).setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);

        if (getIntent().hasExtra(Constants.BUNDLE_TAG_FUNCTION)) {
            mCell = getIntent().getStringExtra(Constants.BUNDLE_TAG_FUNCTION);
        }
        assert (mCell != null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRegister:
                if (isDataValid()) {
                    startRegisterConnection(mCell, etUsername.getEditableText().toString()
                            .trim(), Utils.md5(editPassword.getEditableText().toString()
                            .trim()));
                }
                break;
            case R.id.btnBack:
                this.finish();
                break;
        }
    }

    private void startRegisterConnection(String cell, String username, String password) {
        progressDialog = ProgressDialog.show(this, null,
                Config.PROGRESS_REGISTER);
        String url = Config.SERVER_HOST + Config.URL_REGISTER;
        JSONObject obj = new JSONObject();
        try {
            obj.put(JsonAccount.cell, cell);
            obj.put(JsonAccount.username, username);
            obj.put(JsonAccount.password, password);
        } catch (JSONException e) {
            Utils.toast(this, Config.ERROR_APP);
            e.printStackTrace();
            return;
        }
        HttpConnection connection = new HttpConnection();
        connection.post(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            Utils.toast(RegisterActivity.this, Config.SUCCESS_REGISTER);
                            RegisterActivity.this.finish();
                        } else {
                            Utils.toast(RegisterActivity.this, Intepreter.getCommonStatusFromJson(result).statusMsg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.toast(RegisterActivity.this, Config.ERROR_INTERFACE);
                    }
                } else {
                    Utils.toast(RegisterActivity.this, Config.ERROR_NETWORK);
                }
            }
        });
    }

    private boolean isDataValid() {
        if (etUsername.getEditableText().toString().trim().length() == 0) {
            etUsername.requestFocus();
            Utils.toast(this, Config.NOTE_USERNAME_EMPTY);
            return false;
        }
        String pswd = editPassword.getEditableText().toString().trim();
        if (pswd.length() == 0) {
            editPassword.requestFocus();
            Utils.toast(this, Config.NOTE_PASSWORD_EMPTY);
            return false;
        }
        String rptPswd = etRptPswd.getEditableText().toString().trim();
        if (rptPswd.length() == 0) {
            etRptPswd.requestFocus();
            Utils.toast(this, Config.NOTE_RPT_PASSWORD_EMPTY);
            return false;
        }
        if (!pswd.equals(rptPswd)) {
            editPassword.setText("");
            etRptPswd.setText("");
            editPassword.requestFocus();
            Utils.toast(this, Config.NOTE_PASSWORD_UNMATCH);
            return false;
        }

        return true;
    }

}
