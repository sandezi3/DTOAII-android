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
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Config;
import com.accenture.datongoaii.util.Constants;
import com.accenture.datongoaii.util.Intepreter;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ManageOrgActivity extends Activity implements View.OnClickListener {
    private Context context;

    private View btnBack;
    private View btnOrgName;
    private TextView tvOrgName;
    private View btnOrg;
    private View btnDismiss;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.activity_manage_org);

        btnBack = findViewById(R.id.btnBack);
        btnOrgName = findViewById(R.id.btnOrgName);
        tvOrgName = (TextView) findViewById(R.id.tvOrgName);
        btnOrg = findViewById(R.id.btnOrg);
        btnDismiss = findViewById(R.id.btnDismiss);

        btnBack.setOnClickListener(this);
        btnOrgName.setOnClickListener(this);
        btnOrg.setOnClickListener(this);
        btnDismiss.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (view.equals(btnBack)) {
            finish();
            return;
        }
        if (view.equals(btnOrgName)) {
            //TODO
            return;
        }
        if (view.equals(btnOrg)) {
            //TODO
            return;
        }
        if (view.equals(btnDismiss)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(Config.ALERT_DELETE_ORG)
                    .setCancelable(false)
                    .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startDeleteOrgConnect(Account.getInstance().getOrg().orgId);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CHANGE_ORG_NAME && resultCode == RESULT_OK) {
            this.setResult(Activity.RESULT_OK);
        }
    }

    // 网络请求
    private void startDeleteOrgConnect(Integer orgId) {
        String url = Config.SERVER_HOST + Config.URL_DELETE_ORG;
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
