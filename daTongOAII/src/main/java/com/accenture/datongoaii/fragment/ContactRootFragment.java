package com.accenture.datongoaii.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.DeptActivity;
import com.accenture.datongoaii.activity.ManageOrgActivity;
import com.accenture.datongoaii.activity.MyFriendActivity;
import com.accenture.datongoaii.activity.PhoneContactActivity;
import com.accenture.datongoaii.activity.SelectUserActivity;
import com.accenture.datongoaii.adapter.ContactListAdapter;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.model.FirstPinYin;
import com.accenture.datongoaii.model.Group;
import com.accenture.datongoaii.model.Org;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Config;
import com.accenture.datongoaii.util.Constants;
import com.accenture.datongoaii.util.Intepreter;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.widget.ContactAddPopupWindow;
import com.accenture.datongoaii.widget.MorePopupWindow;
import com.accenture.datongoaii.widget.SectionListView;
import com.accenture.datongoaii.widget.SectionListView.OnSectionItemClickedListener;
import com.accenture.datongoaii.widget.SectionListView.SectionListAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
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

    private ContactListAdapter adapterOrg;
    private ContactListAdapter adapterGroup;
    private ContactListAdapter adapterContact;

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

        adapterOrg = new ContactListAdapter(context, orgList);
        adapterGroup = new ContactListAdapter(context, groupList);
        adapterContact = new ContactListAdapter(context, userList);
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
        Dept d0 = new Dept();
        d0.id = Account.getInstance().getOrg().orgId;
        d0.name = Account.getInstance().getOrg().orgName;
        orgList.add(d0);
        Dept d1 = new Dept();
        d1.id = Dept.DEPT_ID_MY_FRIENDS;
        d1.name = "我的好友";
        d1.img = null;
        d1.mFirstPinYin = "*";
        Dept d2 = new Dept();
        d2.id = Dept.DEPT_ID_PHONE_CONTACT;
        d2.name = "手机通讯录";
        d2.img = null;
        d2.mFirstPinYin = "*";
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
