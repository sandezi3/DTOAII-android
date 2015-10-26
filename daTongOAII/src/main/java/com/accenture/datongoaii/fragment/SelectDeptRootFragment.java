package com.accenture.datongoaii.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.SelectDeptActivity;
import com.accenture.datongoaii.adapter.SelectDeptListAdapter;
import com.accenture.datongoaii.model.Dept;

import java.util.ArrayList;
import java.util.List;

public class SelectDeptRootFragment extends Fragment implements AdapterView.OnItemClickListener {
    public SelectDeptActivity activity;
    public Dept mDept;

    public List<Dept> dataList;
    private SelectDeptListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_dept, container, false);
        activity = (SelectDeptActivity) getActivity();

        ListView lvDept = (ListView) view.findViewById(R.id.lvDept);
        View pbLayout = view.findViewById(R.id.pbLayout);
        pbLayout.setVisibility(View.INVISIBLE);

        dataList = new ArrayList<Dept>();
        adapter = new SelectDeptListAdapter(activity, dataList);
        lvDept.setAdapter(adapter);
        lvDept.setOnItemClickListener(this);

        refreshData();

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        activity.onFragmentItemClick((Dept) adapterView.getAdapter().getItem(i));
    }

    // 公有方法
    public void setDisplayData(Dept dept) {
        this.mDept = dept;
    }

    // 私有方法
    public void refreshData() {
        dataList.clear();
        dataList.addAll(mDept.subDept);
        adapter.refreshWithSelected();
    }

}
