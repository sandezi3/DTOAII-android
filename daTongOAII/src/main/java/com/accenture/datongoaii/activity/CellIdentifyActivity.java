package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class CellIdentifyActivity extends Activity implements View.OnClickListener {
    public static final int HANDLER_TAG_DISMISS_PROGRESS_DIALOG = 0;
    public static final int HANDLER_TAG_REFRESH_SECOND_COUNT = 1;
    public static final int HANDLER_TAG_RESTORE_BUTTON_BEHAVIER = 2;
    public static final int HANDLER_TAG_GET_CODE_SUCCESS = 3;

    private View btnBack;
    private Button btnGetVerifyCode;
    private Button btnNext;
    private EditText etCell;
    private EditText etCode;
    private ProgressDialog progressDialog;

    private String mCell;
    private Timer mTimer;
    private Integer count;
    public int prevFunction;

    static class CellIdentifyActivityHandler extends Handler {
        WeakReference<CellIdentifyActivity> mActivity;

        CellIdentifyActivityHandler(CellIdentifyActivity activity) {
            mActivity = new WeakReference<CellIdentifyActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            CellIdentifyActivity theActivity = mActivity.get();
            switch (msg.what) {
                case HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    if (theActivity.progressDialog != null) {
                        theActivity.progressDialog.dismiss();
                        theActivity.progressDialog = null;
                    }
                    break;
                case HANDLER_TAG_REFRESH_SECOND_COUNT:
                    theActivity.btnGetVerifyCode.setText("再获取" + "("
                            + (theActivity.count + 1) + ")");
                    break;
                case HANDLER_TAG_RESTORE_BUTTON_BEHAVIER:
                    theActivity.btnGetVerifyCode.setEnabled(true);
                    theActivity.btnGetVerifyCode
                            .setBackgroundResource(R.drawable.button);
                    theActivity.btnGetVerifyCode.setText("获取验证码");
                    break;
                case HANDLER_TAG_GET_CODE_SUCCESS:
                    Toast.makeText(theActivity, Config.SUCCESS_GET_VERIFY_CODE,
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private Handler handler = new CellIdentifyActivityHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cell_identify);

        btnBack = findViewById(R.id.btnBack);
        btnGetVerifyCode = (Button) findViewById(R.id.btnGetVerfyCode);
        btnNext = (Button) findViewById(R.id.btnNext);
        etCell = (EditText) findViewById(R.id.etCell);
        etCode = (EditText) findViewById(R.id.etCode);

        btnBack.setOnClickListener(this);
        btnGetVerifyCode.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        if (this.getIntent().hasExtra(Constants.BUNDLE_TAG_FUNCTION)) {
            prevFunction = this.getIntent().getIntExtra(Constants.BUNDLE_TAG_FUNCTION, -1);
        }
    }


    @Override
    public void onClick(View view) {
        if (view.equals(btnBack)) {
            this.finish();
            return;
        }
        if (view.equals(btnGetVerifyCode)) {
            if (isCellValid()) {
                getVerifyCode(etCell.getEditableText().toString().trim());
            }
            return;
        }
        if (view.equals(btnNext)) {
            if (isDataValid()) {
                String cell = etCell.getEditableText().toString().trim();
                String code = etCode.getEditableText().toString().trim();
                startVerficationConnect(cell, code);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private boolean isCellValid() {
        mCell = etCell.getEditableText().toString().trim();
        if (mCell.length() == 0) {
            Utils.toast(this, Config.NOTE_CELL_EMPTY);
            etCell.requestFocus();
            return false;
        }
        if (mCell.length() != 11) {
            Utils.toast(this, Config.NOTE_CELL_NUMBER);
            etCell.requestFocus();
            return false;
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            Long.parseLong(mCell);
        } catch (Exception e) {
            e.printStackTrace();
            etCell.requestFocus();
            Utils.toast(this, Config.NOTE_CELL_FORMAT);
            etCell.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isDataValid() {
        if (!isCellValid()) {
            return false;
        }
        String code = etCode.getEditableText().toString().trim();
        if (code.length() == 0) {
            Utils.toast(this, Config.NOTE_VERIFY_CODE_EMPTY);
            etCode.requestFocus();
            return false;
        }
        return true;
    }

    private void getCodeBtnInvaidForSeconds() {
        count = Config.GET_VERIFY_CODE_INVALID_SECONDS;
        if (mTimer == null) {
            mTimer = new Timer();
            setTimerTask();
        }
    }

    private void setTimerTask() {
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (count > 0) {
                    count--;
                    handler.sendEmptyMessage(HANDLER_TAG_REFRESH_SECOND_COUNT);
                } else {
                    mTimer.cancel();
                    mTimer = null;
                    handler.sendEmptyMessage(HANDLER_TAG_RESTORE_BUTTON_BEHAVIER);
                }
            }
        }, 0, 1000);
    }

    private void getVerifyCode(String number) {
        String url = Config.SERVER_HOST + Config.URL_REQUIRE_VERIFY_CODE + number;
        progressDialog = Utils.showProgressDialog(this, progressDialog, null, Config.PROGRESS_GET);
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (!result.equals("fail")) {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            handler.sendEmptyMessage(HANDLER_TAG_GET_CODE_SUCCESS);
                            btnGetVerifyCode.setEnabled(false);
                            btnGetVerifyCode
                                    .setBackgroundResource(R.drawable.button_disable);
                            getCodeBtnInvaidForSeconds();
                        } else {
                            Utils.toast(CellIdentifyActivity.this, Intepreter.getCommonStatusFromJson(result).statusMsg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.toast(CellIdentifyActivity.this, Config.ERROR_INTERFACE);
                    }
                } else {
                    Utils.toast(CellIdentifyActivity.this, Config.ERROR_NETWORK);
                }
                handler.sendEmptyMessage(HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
            }
        });
    }

    private void startVerficationConnect(String cell, String code) {
        String url = Config.SERVER_HOST + Config.URL_VERIFICATION;
        JSONObject obj = new JSONObject();
        try {
            obj.put("cell", cell);
            obj.put("code", code);
        } catch (JSONException e) {
            if (Logger.DEBUG) {
                e.printStackTrace();
            }
        }
        progressDialog = Utils.showProgressDialog(this, progressDialog, null, Config.PROGRESS_SEND);
        new HttpConnection().post(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            switch (prevFunction) {
                                case Constants.FUNCTION_TAG_REGISTER: {
                                    Intent intent = new Intent(CellIdentifyActivity.this, RegisterActivity.class);
                                    intent.putExtra(Constants.BUNDLE_TAG_FUNCTION, mCell);
                                    CellIdentifyActivity.this.startActivity(intent);
                                    CellIdentifyActivity.this.finish();
                                }
                                break;
                                case Constants.FUNCTION_TAG_FORGET_PASSWORD: {
                                    Intent intent = new Intent(CellIdentifyActivity.this, ChangePswdActivity.class);
                                    intent.putExtra(Constants.BUNDLE_TAG_FUNCTION, mCell);
                                    CellIdentifyActivity.this.startActivity(intent);
                                    CellIdentifyActivity.this.finish();
                                }
                                break;
                            }
                        } else {
                            Utils.toast(CellIdentifyActivity.this, Intepreter.getCommonStatusFromJson(result).statusMsg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.toast(CellIdentifyActivity.this, Config.ERROR_INTERFACE);
                    }
                } else {
                    Utils.toast(CellIdentifyActivity.this, Config.ERROR_NETWORK);
                }
            }
        });
    }

}
