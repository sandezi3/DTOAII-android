package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.DTOARequest;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.adapter.UserGridAdapter;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONException;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CreateGroupActivity extends Activity implements OnClickListener {
    private static final String TAG = "CreateGroupActivity";
    private Context context;
    private EditText etName;
    private ProgressDialog progressDialog;
    private Handler handler = new ActivityHandler(this);
    private List<Contact> selectedUsers;
    private UserGridAdapter adapter;

    static class ActivityHandler extends Handler {
        WeakReference<CreateGroupActivity> mActivity;

        public ActivityHandler(CreateGroupActivity activity) {
            this.mActivity = new WeakReference<CreateGroupActivity>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            CreateGroupActivity a = mActivity.get();
            switch (message.what) {
                case Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    if (a.progressDialog != null) {
                        a.progressDialog.dismiss();
                        a.progressDialog = null;
                    }
                    break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_create_group);

        etName = (EditText) findViewById(R.id.etName);
        View btnSelect = findViewById(R.id.layoutSelect);
        View btnCreate = findViewById(R.id.btnCreate);
        View btnBack = findViewById(R.id.btnBack);
        GridView gvUsers = (GridView) findViewById(R.id.gvUsers);

        btnCreate.setOnClickListener(this);
        btnSelect.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        selectedUsers = new ArrayList<Contact>();
        initMeButton();
        adapter = new UserGridAdapter(context, selectedUsers, true);
        gvUsers.setAdapter(adapter);
        gvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Contact contact = (Contact) adapter.getItem(position);
                    selectedUsers.remove(contact);
                    refreshSelectedUsers();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CREATE_GROUP && resultCode == RESULT_OK) {
            List<Contact> users = (List<Contact>) data.getSerializableExtra(Constants.BUNDLE_TAG_SELECT_USER_MULTI_MODE_RESULT);
            if (users != null && users.size() > 0) {
                selectedUsers.clear();
                selectedUsers.addAll(users);
                refreshSelectedUsers();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layoutSelect: {
                Intent intent = new Intent(context, SelectUserActivity.class);
                if (selectedUsers.size() > 0) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_TAG_SELECT_USER_MULTI_MODE_RESULT, (Serializable) selectedUsers);
                    intent.putExtras(bundle);
                }
                intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_MULTI_MODE, true);
                startActivityForResult(intent, Constants.REQUEST_CODE_CREATE_GROUP);
            }
            break;
            case R.id.btnCreate:
                if (isDataValid()) {
                    progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_SUBMIT);
                    DTOARequest.startCreateGroup(getGroupName(), Account.getInstance().getImId(), "", selectedUsers, new HttpConnection.CallbackListener() {
                        @Override
                        public void callBack(String result) {
                            handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                            if (!result.equals("fail")) {
                                try {
                                    if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                                        Utils.toast(context, Config.SUCCESS_CREATE);
                                        ((Activity) context).setResult(RESULT_OK);
                                        ((Activity) context).finish();
                                    }
                                } catch (JSONException e) {
                                    Logger.e(TAG, e.getMessage());
                                }
                            }
                        }
                    });
                }
                break;
            case R.id.btnBack:
                ((Activity) context).finish();
                break;
        }
    }

    private boolean isDataValid() {
        if (selectedUsers.size() == 1) {
            Utils.toast(context, Config.NOTE_IM_GROUP_USER_EMPTY);
            return false;
        }
        return true;
    }

    private void refreshSelectedUsers() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private String getGroupName() {
        String name = etName.getEditableText().toString().trim().replace(" ", "");
        if (name.length() == 0) {
            for (Contact contact : selectedUsers) {
                name += contact.name + ",";
            }
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    private void initMeButton() {
        Contact me = new Contact();
        me.name = Account.getInstance().getUsername();
        me.imId = Account.getInstance().getImId();
        me.id = Account.getInstance().getUserId();
        me.cell = Account.getInstance().getCell();
        selectedUsers.add(me);
    }
}
