package com.accenture.datongoaii.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.SelectDeptActivity;
import com.accenture.datongoaii.adapter.SelectDeptListAdapter;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SelectDeptFragment extends Fragment implements AdapterView.OnItemClickListener {
    public SelectDeptActivity activity;
    public Dept mDept;

    public List<Dept> dataList;
    private SelectDeptListAdapter adapter;

    private View pbLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_dept, container, false);
        activity = (SelectDeptActivity) getActivity();

        ListView lvDept = (ListView) view.findViewById(R.id.lvDept);
        pbLayout = view.findViewById(R.id.pbLayout);
        pbLayout.setVisibility(View.VISIBLE);

        dataList = new ArrayList<Dept>();
        adapter = new SelectDeptListAdapter(activity, dataList);
        lvDept.setAdapter(adapter);
        lvDept.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        activity.onFragmentItemClick((Dept) adapterView.getAdapter().getItem(i));
    }

    // 公有方法
    public void setDisplayData(Dept dept) {
        this.mDept = dept;
        startGetDeptConnect(dept.id);
    }

    // 私有方法
    public void refreshData() {
        dataList.clear();
        dataList.addAll(mDept.subDept);
        adapter.refreshWithSelected();
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
                            Utils.toast(activity, Config.ERROR_INTERFACE);
                            return;
                        }
                        if (cr.statusCode == 0) {
                            mDept = Dept.fromJSON(new JSONObject(result).getJSONObject("data"));
                            refreshData();
                        } else {
                            Utils.toast(activity, cr.statusMsg);
                        }
                    } catch (JSONException e) {
                        Utils.toast(activity, Config.ERROR_INTERFACE);
                    }
                } else {
                    Utils.toast(activity, Config.ERROR_NETWORK);
                }
            }
        });
    }
}
