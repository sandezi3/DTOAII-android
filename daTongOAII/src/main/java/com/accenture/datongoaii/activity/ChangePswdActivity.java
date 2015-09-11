package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Jsons;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Config;
import com.accenture.datongoaii.util.Constants;
import com.accenture.datongoaii.util.Intepreter;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class ChangePswdActivity extends Activity implements View.OnClickListener {
    public static final int HANDLER_TAG_DISMISS_PROGRESS_DIALOG = 0;

    private EditText etPassword;
    private EditText etRptPswd;
    private ProgressDialog progressDialog;

    public String mCell;

    static class ChangePswdActivityHandler extends Handler {
        WeakReference<ChangePswdActivity> mActivity;

        ChangePswdActivityHandler(ChangePswdActivity activity) {
            mActivity = new WeakReference<ChangePswdActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ChangePswdActivity theActivity = mActivity.get();
            switch (msg.what) {
                case HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    if (theActivity.progressDialog != null) {
                        theActivity.progressDialog.dismiss();
                        theActivity.progressDialog = null;
                    }
                    break;
            }
        }
    }

    private Handler handler = new ChangePswdActivityHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pswd);

        etPassword = (EditText) findViewById(R.id.etPassword);
        etRptPswd = (EditText) findViewById(R.id.etRptpassword);

        findViewById(R.id.btnSubmit).setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);

        if(getIntent().hasExtra(Constants.BUNDLE_TAG_FUNCTION)) {
            mCell = getIntent().getStringExtra(Constants.BUNDLE_TAG_FUNCTION);
        }
        assert (mCell != null);    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                this.finish();
                break;
            case R.id.btnSubmit:
                if (isDataValid()) {
                    startChangePasswordConnect(mCell, etPassword.getEditableText().toString().trim());
                }
                break;
        }
    }

    private void startChangePasswordConnect(String cell, String password) {
        progressDialog = ProgressDialog.show(this, null,
                Config.PROGRESS_SUBMIT);
        String url = Config.SERVER_HOST + Config.URL_CHANGE_PASSWORD;
        JSONObject obj = new JSONObject();
        try {
            obj.put(Jsons.JsonAccount.cell, cell);
            obj.put(Jsons.JsonAccount.newPassword, Utils.md5(password));
        } catch (JSONException e) {
            Utils.toast(this, Config.ERROR_APP);
            e.printStackTrace();
            return;
        }
        HttpConnection connection = new HttpConnection();
        connection.put(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            Utils.toast(ChangePswdActivity.this, Config.SUCCESS_REGISTER);
                            ChangePswdActivity.this.finish();
                        } else {
                            Utils.toast(ChangePswdActivity.this, Intepreter.getCommonStatusFromJson(result).statusMsg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.toast(ChangePswdActivity.this, Config.ERROR_INTERFACE);
                    }
                } else {
                    Utils.toast(ChangePswdActivity.this, Config.ERROR_NETWORK);
                }
            }
        });
    }

    private boolean isDataValid() {
        String pswd = etPassword.getEditableText().toString().trim();
        if (pswd.length() == 0) {
            etPassword.requestFocus();
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
            etPassword.setText("");
            etRptPswd.setText("");
            etPassword.requestFocus();
            Utils.toast(this, Config.NOTE_PASSWORD_UNMATCH);
            return false;
        }

        return true;
    }
}
