package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.DTOARequest;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.adapter.GroupListAdapter;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.Group;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 10/9/15.
 * 聊天群列表
 */
public class MyGroupsActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MyGroupsActivity";
    private Context context;
    private ProgressDialog progressDialog;
    private Handler handler = new ActivityHandler(this);
    private List<Group> groupList;
    private GroupListAdapter adapter;

    static class ActivityHandler extends Handler {
        WeakReference<MyGroupsActivity> mActivity;

        public ActivityHandler(MyGroupsActivity activity) {
            this.mActivity = new WeakReference<MyGroupsActivity>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            MyGroupsActivity a = mActivity.get();
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
        setContentView(R.layout.activity_my_groups);
        groupList = new ArrayList<Group>();
        adapter = new GroupListAdapter(context, groupList);
        PullToRefreshListView lvGroups = (PullToRefreshListView) findViewById(R.id.lvGroups);
        lvGroups.setAdapter(adapter);
        lvGroups.setOnItemClickListener(this);

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getGroups();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_MANAGE_GROUP && resultCode == RESULT_OK) {
            getGroups();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Group group = (Group) adapter.getItem(position - 1);
        Intent intent = new Intent(context, GroupProfileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.BUNDLE_TAG_GROUP_PROFILE, group);
        intent.putExtras(bundle);
        this.startActivityForResult(intent, Constants.REQUEST_CODE_MANAGE_GROUP);
    }

    private void getGroups() {
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_GET);
        DTOARequest.startGetGroups(Account.getInstance().getImId(), new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            List<Group> list = Group.getGroupListFromJSON(new JSONObject(result));
                            if (list != null && list.size() > 0) {
                                groupList.clear();
                                groupList.addAll(list);
                                refresh();
                            }
                        }
                    } catch (JSONException e) {
                        Logger.e(TAG, e.getMessage());
                    }
                }
            }
        });
    }

    private void refresh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
}
