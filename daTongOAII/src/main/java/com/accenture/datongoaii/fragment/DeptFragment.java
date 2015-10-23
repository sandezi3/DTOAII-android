package com.accenture.datongoaii.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.DeptActivity;
import com.accenture.datongoaii.activity.SelectUserActivity;
import com.accenture.datongoaii.adapter.ContactListAdapter;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeptFragment extends Fragment implements AdapterView.OnItemClickListener {
    public Context context;
    public Dept mDept;

    private boolean isManageMode;

    public List<Object> viewList;
    private ContactListAdapter adapter;

    private View pbLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_dept, container, false);
        context = this.getActivity();

        ListView lvDept = (ListView) view.findViewById(R.id.lvDept);
        pbLayout = view.findViewById(R.id.pbLayout);
        pbLayout.setVisibility(View.VISIBLE);

        viewList = new ArrayList<Object>();
        adapter = new ContactListAdapter(this.getActivity(), viewList);
        lvDept.setAdapter(adapter);
        lvDept.setOnItemClickListener(this);

        adapter.isManageMode = isManageMode;

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (context instanceof SelectUserActivity) {
            ((SelectUserActivity) context).onFragmentItemClick(adapterView.getAdapter().getItem(i));
        } else if (context instanceof DeptActivity) {
            ((DeptActivity) context).onFragmentItemClick(adapterView.getAdapter().getItem(i));
        }
    }

    // 公有方法
    public void setDisplayData(Dept dept) {
        this.mDept = dept;
        startGetDeptConnect(dept.id);
    }

    public void setIsManageMode(boolean isManageMode) {
        this.isManageMode = isManageMode;
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
        String url = Config.SERVER_HOST + Config.URL_DEPT.replace("{groupId}", deptId + "");
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                pbLayout.setVisibility(View.INVISIBLE);
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
