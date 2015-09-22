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
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Config;
import com.accenture.datongoaii.util.Constants;
import com.accenture.datongoaii.util.Intepreter;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ManageUserActivity extends Activity implements View.OnClickListener {
    private Context context;
    private Contact mContact;
    private List<Dept> tmpParents;

    private View btnBack;
    private View btnUsername;
    private TextView tvUsername;
    private View btnParent;
    private TextView tvParentName;
    private View btnDismiss;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.activity_manage_user);

        btnBack = findViewById(R.id.btnBack);
        btnUsername = findViewById(R.id.btnUsername);
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        btnParent = findViewById(R.id.btnParent);
        tvParentName = (TextView) findViewById(R.id.tvParentName);
        btnDismiss = findViewById(R.id.btnDismiss);

        btnBack.setOnClickListener(this);
        btnUsername.setOnClickListener(this);
        btnParent.setOnClickListener(this);
        btnDismiss.setOnClickListener(this);

        mContact = (Contact) getIntent().getSerializableExtra(Constants.BUNDLE_TAG_MANAGE_USER);
        tvUsername.setText(mContact.name);

        getParents(Account.getInstance().getOrg().orgId, mContact.id);
    }

    private String getParentName(List<Dept> parentDepts) {
        if (parentDepts.size() > 1) {
            return parentDepts.get(0).name + "等";
        } else if (parentDepts.size() == 1) {
            return parentDepts.get(0).name;
        }
        return "";
    }

    @Override
    public void onClick(View view) {
        if (view.equals(btnBack)) {
            finish();
            return;
        }
        if (view.equals(btnUsername)) {
            final EditText etName = new EditText(context);
            etName.setBackgroundColor(getResources().getColor(R.color.white));
            etName.setText(tvUsername.getText());
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(etName)
                    .setCancelable(false)
                    .setPositiveButton("更改", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String name = etName.getEditableText().toString().trim();
                            if (name.length() == 0) {
                                Utils.toast(context, Config.NOTE_USERNAME_EMPTY);
                                return;
                            }
                            startChangeUserNameConnect(mContact.id, name);
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
            intent.putExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_ID, Account.getInstance().getOrg().orgId);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_DEPT_MULTI_MODE, true);
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
                            startDeleteDeptUserConnect(mContact.parent.id, mContact.id);
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
            tmpParents = (List<Dept>) data.getSerializableExtra(Constants.BUNDLE_TAG_SELECT_DEPT);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(Config.ALERT_PARENT_DEPT.replace("{}", "\"" + getParentName(tmpParents) + "\""))
                    .setCancelable(false)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startChangeParentDeptConnect(Account.getInstance().getOrg().orgId, mContact.id, tmpParents);
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

    private void getParents(Integer orgId, Integer userId) {
        String url = Config.SERVER_HOST + Config.URL_GET_USER_PARENT.replace("{userId}", userId + "").replace("{rootGroupId}", orgId + "");
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            List<Dept> parents = Dept.parentsFromJSON(new JSONObject(result));
                            tvParentName.setText(getParentName(parents));
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

    private void startChangeParentDeptConnect(Integer orgId, Integer userId, List<Dept> destParents) {
        String url = Config.SERVER_HOST + Config.URL_MODIFY_USER.replace("{userId}", userId + "");
        JSONObject obj = new JSONObject();
        List<Integer> ids = new ArrayList<Integer>();
        for (Dept dept : destParents) {
            ids.add(dept.id);
        }
        try {
            JSONArray array = new JSONArray(ids);
            obj.put("groupIds", array);
            obj.put("rootGroupId", orgId);
        } catch (JSONException e) {
            Utils.toast(context, Config.ERROR_APP);
            return;
        }
        new HttpConnection().put(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                alertDialog.dismiss();
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            ((Activity) context).setResult(RESULT_OK);
                            Utils.toast(context, Config.SUCCESS_UPDATE);
                            getParents(Account.getInstance().getOrg().orgId, mContact.id);
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


    // 网络请求
    private void startChangeUserNameConnect(Integer userId, final String name) {
        String url = Config.SERVER_HOST + Config.URL_MODIFY_USER.replace("{userId}", userId + "");
        JSONObject obj = new JSONObject();
        try {
            obj.put("username", name);
        } catch (JSONException e) {
            Utils.toast(context, Config.ERROR_APP);
            return;
        }
        new HttpConnection().put(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                alertDialog.dismiss();
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            ((Activity) context).setResult(RESULT_OK);
                            tvUsername.setText(name);
                            Utils.toast(context, Config.SUCCESS_UPDATE);
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

    private void startDeleteDeptUserConnect(Integer deptId, Integer userId) {
        String url = Config.SERVER_HOST + Config.URL_DELETE_USER.replace("{groupId}", deptId + "").replace("{userId}", userId + "");
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
