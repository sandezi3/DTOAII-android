package com.accenture.datongoaii.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.SelectUserActivity;
import com.accenture.datongoaii.adapter.ContactListAdapter;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.Dept;

import java.util.ArrayList;
import java.util.List;

public class ContactRootFragment extends Fragment implements AdapterView.OnItemClickListener {
    private Context context;
    private List<Object> orgList;
    private List<Object> groupList;
    private List<Object> userList;

    private ListView lvOrg;
    private ListView lvGroup;
    private ListView lvContact;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        View layoutContact = inflater.inflate(R.layout.frag_contact_root, container,
                false);

        lvOrg = (ListView) layoutContact.findViewById(R.id.lvOrg);
        lvGroup = (ListView) layoutContact.findViewById(R.id.lvGroup);
        lvContact = (ListView) layoutContact.findViewById(R.id.lvContact);

        orgList = new ArrayList<Object>();
        groupList = new ArrayList<Object>();
        userList = new ArrayList<Object>();
        appendLocalData();

        ContactListAdapter adapterOrg = new ContactListAdapter(context, orgList);
        ContactListAdapter adapterGroup = new ContactListAdapter(context, groupList);
        ContactListAdapter adapterContact = new ContactListAdapter(context, userList);
        lvOrg.setAdapter(adapterOrg);
        lvGroup.setAdapter(adapterGroup);
        lvContact.setAdapter(adapterContact);
        lvOrg.setOnItemClickListener(this);
        lvGroup.setOnItemClickListener(this);
        lvContact.setOnItemClickListener(this);

        adjustHeight();
        return layoutContact;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Object obj = view.getTag(view.getId());
        ((SelectUserActivity) context).onFragmentItemClick(obj);
    }

    // 私有方法
    private void appendLocalData() {
        if (Account.getInstance().getOrgList() != null && Account.getInstance().getOrgList().size() > 0) {
            orgList.addAll(Account.getInstance().getOrgList());
        }
        Dept d1 = new Dept();
        d1.id = Dept.DEPT_ID_MY_FRIENDS;
        d1.name = "我的好友";
        d1.img = null;
        d1.mFirstPinYin = "#";
        Dept d2 = new Dept();
        d2.id = Dept.DEPT_ID_PHONE_CONTACT;
        d2.name = "手机通讯录";
        d2.img = null;
        d2.mFirstPinYin = "#";
        groupList.add(d1);
        groupList.add(d2);
    }

    private void adjustHeight() {
        int height = (int) getResources().getDimension(R.dimen.phone_contact_list_item_height);
        LinearLayout.LayoutParams paramsOrg = (LinearLayout.LayoutParams) lvOrg.getLayoutParams();
        LinearLayout.LayoutParams paramsGroup = (LinearLayout.LayoutParams) lvGroup.getLayoutParams();
        LinearLayout.LayoutParams paramsContact = (LinearLayout.LayoutParams) lvContact.getLayoutParams();
        paramsOrg.height = height * orgList.size() + lvOrg.getDividerHeight() * (orgList.size() - 1);
        paramsGroup.height = height * groupList.size() + lvGroup.getDividerHeight() * (groupList.size() - 1);
        paramsContact.height = height * userList.size() + lvContact.getDividerHeight() * (userList.size() - 1);
        lvOrg.setLayoutParams(paramsOrg);
        lvGroup.setLayoutParams(paramsGroup);
        lvContact.setLayoutParams(paramsContact);
    }

}
