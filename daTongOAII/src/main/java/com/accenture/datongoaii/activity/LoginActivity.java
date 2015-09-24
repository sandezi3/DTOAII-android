package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.common.Config;
import com.accenture.datongoaii.common.Constants;
import com.accenture.datongoaii.common.Intepreter;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

public class LoginActivity extends Activity implements OnClickListener {
    private EditText editPhoneNumber;
    private EditText editPassword;

    private ProgressDialog progressDialog;

    private Handler handler = new ClearProgressDialogHandler(this);

    static class ClearProgressDialogHandler extends Handler {
        WeakReference<LoginActivity> mActivity;

        ClearProgressDialogHandler(LoginActivity activity) {
            mActivity = new WeakReference<LoginActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 0:
                    if (theActivity.progressDialog != null) {
                        theActivity.progressDialog.dismiss();
                        theActivity.progressDialog = null;
                    }
                    break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editPhoneNumber = (EditText) findViewById(R.id.editCell);
        editPassword = (EditText) findViewById(R.id.editPassword);
        TextView tVRegister = (TextView) findViewById(R.id.tVRegister);
        TextView tVForgetPswd = (TextView) findViewById(R.id.tVForgetPswd);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        tVRegister.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tVForgetPswd.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        tVRegister.setOnClickListener(this);
        tVForgetPswd.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (Config.DEBUG_AUTO_LOGIN) {
            Utils.closeSoftKeyboard(this, null);
            String token = Utils.getUserInfo(this);
            if (token != null && token.length() > 0) {
                startLoginConnect(token);
            }
        }
    }

    @Override
    public void onClick(View v) {
        Utils.closeSoftKeyboard(this, v);
        switch (v.getId()) {
            case R.id.btnBack:
                this.finish();
                break;
            case R.id.tVRegister: {
                Intent intent = new Intent(LoginActivity.this, CellIdentifyActivity.class);
                intent.putExtra(Constants.BUNDLE_TAG_FUNCTION, Constants.FUNCTION_TAG_REGISTER);
                this.startActivityForResult(intent, Constants.REQUEST_CODE_FORGET_PW_LOGIN);
            }
            break;
            case R.id.btnLogin:
                if (isDataValid()) {
                    Utils.closeSoftKeyboard(this, v);
                    startLoginConnect(editPhoneNumber.getEditableText().toString()
                            .trim(), editPassword.getEditableText().toString()
                            .trim());
                }
                break;
            case R.id.tVForgetPswd: {
                Intent intent = new Intent(LoginActivity.this, CellIdentifyActivity.class);
                intent.putExtra(Constants.BUNDLE_TAG_FUNCTION, Constants.FUNCTION_TAG_FORGET_PASSWORD);
                this.startActivityForResult(intent, Constants.REQUEST_CODE_FORGET_PW_LOGIN);
            }
            break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_FORGET_PW_LOGIN
                && resultCode == RESULT_OK) {
            finishAndReturn();
        }
    }

    private boolean isDataValid() {
        if (editPhoneNumber.getEditableText().toString().trim().length() == 0) {
            show(Config.NOTE_CELL_EMPTY);
            return false;
        }
        if (editPassword.getEditableText().toString().trim().length() == 0) {
            editPassword.requestFocus();
            show(Config.NOTE_PASSWORD_EMPTY);
            return false;
        }
        return true;
    }

    private void startLoginConnect(String token) {
        progressDialog = Utils.showProgressDialog(this, progressDialog, null, Config.PROGRESS_LOGIN);
        String url = Config.SERVER_HOST + Config.URL_AUTO_LOGIN.replace("{token}", token);
        HttpConnection connection = new HttpConnection();
        connection.get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(0);
                if (!result.equals("fail")) {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            Account.getInstance().fromJson(new JSONObject(result));
                            Utils.saveUserInfo(LoginActivity.this, Account.getInstance().getToken());
                            finishAndReturn();
                        } else {
                            Logger.i("Login", "Failed!");
                            show(Intepreter.getCommonStatusFromJson(result).statusMsg);
                        }
                    } catch (JSONException e) {
                        Logger.e("Login", "Exception!");
                        show(Config.ERROR_INTERFACE);
                    }
                } else {
                    Logger.i("Login", "Network Error!");
                    show(Config.ERROR_NETWORK);
                }
            }
        });
    }

    private void startLoginConnect(final String userId, final String password) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, null, Config.PROGRESS_LOGIN);
        }
        String url = Config.SERVER_HOST + Config.URL_LOGIN;
        Logger.i("startLoginConnect", url);
        JSONObject obj = new JSONObject();
        try {
            obj.put("cell", userId);
            obj.put("password", Utils.md5(password));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Logger.i("startLoginConnect", obj.toString());
        HttpConnection connection = new HttpConnection();
        connection.post(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(0);
                if (!result.equals("fail")) {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            Account.getInstance().fromJson(new JSONObject(result));
                            Utils.saveUserInfo(LoginActivity.this, Account.getInstance().getToken());
                            finishAndReturn();
                        } else {
                            Logger.i("Login", "Failed!");
                            show(Intepreter.getCommonStatusFromJson(result).statusMsg);
                        }
                    } catch (JSONException e) {
                        Logger.e("Login", "Exception!");
                        show(Config.ERROR_INTERFACE);
                    }
                } else {
                    Logger.i("Login", "Network Error!");
                    show(Config.ERROR_NETWORK);
                }
            }
        });
    }

    protected void finishAndReturn() {
        setResult(RESULT_OK);
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        this.finish();
    }

    protected void show(CharSequence str) {
        try {
            Toast.makeText(this, Utils.getDecodedString((String) str),
                    Toast.LENGTH_SHORT).show();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
