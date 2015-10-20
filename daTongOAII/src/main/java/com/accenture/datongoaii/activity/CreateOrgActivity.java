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

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;


public class CreateOrgActivity extends Activity implements View.OnClickListener {
    public static final int HANDLER_TAG_DISMISS_PROGRESS_DIALOG = 0;
    private Context context;

    private EditText etName;
    private View btnSelect;
    private View btnBack;
    private View btnCreate;
    private ProgressDialog progressDialog;
    private Handler handler = new ActivityHandler(this);

    static class ActivityHandler extends Handler {
        WeakReference<CreateOrgActivity> mActivity;

        public ActivityHandler(CreateOrgActivity activity) {
            this.mActivity = new WeakReference<CreateOrgActivity>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            CreateOrgActivity a = mActivity.get();
            switch (message.what) {
                case HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    if (a.progressDialog != null) {
                        a.progressDialog.dismiss();
                        a.progressDialog = null;
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.activity_create_org);

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
            Intent intent = new Intent(view.getContext(), CreateGroupActivity.class);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_MEMBER, Constants.FUNCTION_TAG_CREATE_ORG);
            startActivityForResult(intent, Constants.REQUEST_CODE_CREATE_ORG);
            return;
        }
        if (view.equals(btnBack)) {
            finish();
            return;
        }
        if (view.equals(btnCreate)) {
            if (isDataValid()) {
                String name = etName.getEditableText().toString().trim();
                startCreateOrgConnect(name);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CREATE_ORG
                && resultCode == RESULT_OK) {
            finish();
        }
    }

    private Boolean isDataValid() {
        String name = etName.getEditableText().toString().trim();
        if (name.length() == 0) {
            Utils.toast(this, Config.NOTE_DEPT_NAME_EMPTY);
            etName.requestFocus();
            return false;
        }
        return true;
    }

    public void startCreateOrgConnect(String name) {
        String url = Config.SERVER_HOST + Config.URL_CREATE_ORG;
        JSONObject obj = new JSONObject();
        try {
            obj.put("groupName", name);
            obj.put(Config.GROUP_TYPE_TAG, Config.GROUP_TYPE_COMPANY);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        Logger.i("CreateOrgActivity", "startCreateOrgConnect");
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
