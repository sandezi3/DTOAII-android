package com.accenture.datongoaii.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.SelectUserActivity;
import com.accenture.datongoaii.adapter.ContactListAdapter;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.common.Config;
import com.accenture.datongoaii.common.Intepreter;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeptFragment extends Fragment implements AdapterView.OnItemClickListener {
    public Context context;
    public Dept mDept;
    public List<Object> viewList;

    private ContactListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dept, container, false);
        context = this.getActivity();

        ListView lvDept = (ListView) view.findViewById(R.id.lvDept);

        viewList = new ArrayList<Object>();
        adapter = new ContactListAdapter(this.getActivity(), viewList);
        lvDept.setAdapter(adapter);
        lvDept.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ((SelectUserActivity) context).onFragmentItemClick(view.getTag(view.getId()));
    }

    // 公有方法
    public void setDisplayData(Dept dept) {
        this.mDept = dept;
        startGetDeptConnect(dept.id);
    }

    // 私有方法
    public void refreshData() {
        viewList.clear();
        viewList.addAll(mDept.subDept);
        viewList.addAll(mDept.contactList);
        adapter.notifyDataSetChanged();
    }

    // 网络数据
    private void startGetDeptConnect(Integer deptId) {
//        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_GET);
        String url = Config.SERVER_HOST + Config.URL_DEPT.replace("{groupId}", deptId + "");
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
//                handler.sendEmptyMessage(HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr == null) {
                            Utils.toast(context, Config.ERROR_INTERFACE);
                            return;
                        }
                        if (cr.statusCode == 0) {
                            mDept = Dept.fromJSON(new JSONObject(result).getJSONObject("data"));
                            refreshData();
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
