package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.model.FirstPinYin;
import com.accenture.datongoaii.model.Group;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Config;
import com.accenture.datongoaii.util.Constants;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.widget.SectionListView;
import com.accenture.datongoaii.widget.SectionListView.OnSectionItemClickedListener;
import com.accenture.datongoaii.widget.SectionListView.SectionListAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SelectContactActivity extends Activity implements
        OnSectionItemClickedListener {
    private SectionListView slvContact;
    private Dept dept;
    private List<Object> viewList;
    private List<Object> groupList;
    private List<Object> uList;
    private List<Object> tmpList;
    private int prevFunction;

    private EditText etSearch;
    private ImageView ivSearch;
    private TextView tvSearch;
    private Button btnSubmit;


    private void clearData() {
        tmpList.clear();
        viewList.clear();
//        groupList.clear();
        uList.clear();
    }

    private void syncData() {
//        viewList.addAll(dept.subDept);
//        viewList.addAll(groupList);
//        viewList.addAll(dept.contactList);
        viewList.addAll(uList);
        tmpList.clear();
        tmpList.addAll(viewList);
        viewList.clear();
        viewList.addAll(FirstPinYin.createPinYinGroupedList(tmpList));
        tmpList.clear();
        tmpList.addAll(viewList);
        adapter.notifyDataSetChanged();
    }

    private final SectionListAdapter adapter = new SectionListAdapter() {
        @Override
        public int getSectionCount() {
//            if (dept == null) {
//                return 0;
//            } else {
                return viewList.size();
//            }
        }

        @Override
        public int getSectionItemCount(int section) {
            Object object = viewList.get(section);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) object;
            return list.size();
        }

        @Override
        public View getSectionHeaderView(int section, View convertView,
                                         ViewGroup parent) {
            Object o = getItem(section, 0);
            View view = convertView;
            if (view == null) {
                view = View.inflate(parent.getContext(),
                        R.layout.section_list_header, null);
            }
            TextView tv = (TextView) view.findViewById(R.id.txtLabel);
            if (o instanceof Dept) {
                tv.setText("企业通讯录");
            } else if (o instanceof Group) {
                tv.setText("我的群组");
            } else if (o instanceof Contact) {
                tv.setText(((Contact) o).mFirstPinYin);
            }
            return view;
        }

        @Override
        public View getSectionItemView(int section, int position,
                                       View convertView, ViewGroup parent) {
            Object o = getItem(section, position);
            View view = convertView;
            if (view == null) {
                view = View.inflate(parent.getContext(),
                        R.layout.list_cell_contact, null);
            }
            TextView tv = (TextView) view.findViewById(R.id.tvName);
            ImageView iv = (ImageView) view.findViewById(R.id.ivIcon);
            ImageView ivSelect = (ImageView) view.findViewById(R.id.ivSelect);
            ImageLoader il = ImageLoader.getInstance();
            if (o instanceof Dept) {
                Dept d = (Dept) o;
                if (d.img.length() > 0) {
                    il.displayImage(d.img, iv, Config.getDisplayOptions());
                }
                ivSelect.setVisibility(View.GONE);
                tv.setText(d.name);
            } else if (o instanceof Group) {
                Group g = (Group) o;
                if (g.img.length() > 0) {
                    il.displayImage(g.img, iv, Config.getDisplayOptions());
                }
                ivSelect.setVisibility(View.GONE);
                tv.setText(g.name);
            } else if (o instanceof Contact) {
                Contact c = (Contact) o;
                if (c.head.length() > 0) {
                    il.displayImage(c.head, iv, Config.getDisplayOptions());
                } else {
                    iv.setImageResource(R.drawable.ic_contact_p);
                }
                ivSelect.setVisibility(View.VISIBLE);
                tv.setText(c.name);
                if (c.selected) {
                    ivSelect.setImageResource(R.drawable.ic_selected);
                } else {
                    ivSelect.setImageResource(R.drawable.ic_unselected);
                }
            }
            view.setTag(o);
            return view;
        }

        @Override
        public Object getItem(int section, int position) {
            Object object = viewList.get(section);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) object;
            return list.get(position);
        }

        @Override
        public String getSectionLabel(int section) {
            FirstPinYin o = (FirstPinYin) getItem(section, 0);
            return o.mFirstPinYin;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        etSearch = (EditText) findViewById(R.id.etSearch);
        ivSearch = (ImageView) findViewById(R.id.ivSearch);
        tvSearch = (TextView) findViewById(R.id.tvSearch);
        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Logger.i("afterTextChanged", s.toString());
                if (s.toString().length() > 0) {
                    ivSearch.setVisibility(View.GONE);
                    tvSearch.setVisibility(View.GONE);
                    viewList.clear();
                    viewList.addAll(getFiltedList(tmpList, s.toString()));
                    adapter.notifyDataSetChanged();
                } else {
                    ivSearch.setVisibility(View.VISIBLE);
                    tvSearch.setVisibility(View.VISIBLE);
                    viewList.clear();
                    viewList.addAll(tmpList);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        slvContact = (SectionListView) findViewById(R.id.slvContact);
        viewList = new ArrayList<Object>();
        groupList = new ArrayList<Object>();
        uList = new ArrayList<Object>();
        tmpList = new ArrayList<Object>();
        slvContact.setAdapter(adapter);
        slvContact.setOnSectionItemClickedListener(this);
        getDept(Account.getInstance().getUserId(), "");

        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectContactActivity.this.finish();
            }
        });

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setEnabled(false);
        btnSubmit.setBackgroundResource(R.drawable.button_disable);
        btnSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (prevFunction) {
                    case Constants.FUNCTION_TAG_CREATE_GROUP:
                        startCreateGroupConnect();
                        break;
                    case Constants.FUNCTION_TAG_CREATE_ORG:
                        break;
                }
            }
        });

        if (getIntent().hasExtra(Constants.BUNDLE_TAG_SELECT_MEMBER)) {
            prevFunction = getIntent().getIntExtra(Constants.BUNDLE_TAG_SELECT_MEMBER, -1);
        }
    }

    public void startCreateGroupConnect() {
        this.finish();
    }



    @SuppressWarnings("unused")
    private String getTmpListName(List<Contact> list) {
        StringBuilder sb = new StringBuilder();
        int len = list.size() > 3 ? 3 : list.size();
        for (int i = 0; i < len; i++) {
            Contact c = list.get(i);
            if (i == len - 1) {
                sb.append(c.name);
            } else {
                sb.append(c.name + ",");
            }
        }
        return sb.toString();
    }

    private List<Contact> getSelectedContact(Dept d) {
        List<Contact> tList = new ArrayList<Contact>();
        for (Object obj : d.contactList) {
            Contact c = (Contact) obj;
            if (c.selected && !contains(tList, c.id)) {
                tList.add(c);
            }
        }
        return tList;
    }

    private boolean contains(List<Contact> list, Integer id) {
        for (Contact c : list) {
            if (c.id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private List<List<Object>> getFiltedList(List<?> list, String str) {
        List<List<Object>> tList = new ArrayList<List<Object>>();
        for (Object o : list) {
            List<Object> ll = new ArrayList<Object>();
            List<Object> oList = (List<Object>) o;
            for (Object oo : oList) {
                ll.add(oo);
            }
            tList.add(ll);
        }
        for (int i = tList.size() - 1; i >= 0; i--) {
            List<Object> l = tList.get(i);
            for (int j = l.size() - 1; j >= 0; j--) {
                Object o = l.get(j);
                if (o instanceof Dept) {
                    if (!((Dept) o).name.contains(str)) {
                        l.remove(o);
                    }
                } else if (o instanceof Group) {
                    if (!((Group) o).name.contains(str)) {
                        l.remove(o);
                    }
                } else if (o instanceof Contact) {
                    if (!((Contact) o).name.contains(str)) {
                        l.remove(o);
                    }
                }
            }
            if (l.size() == 0) {
                tList.remove(l);
            }
        }
        return tList;
    }

    @Override
    public void onSectionItemClicked(SectionListView listView, View view,
                                     int section, int position) {
        Object o = view.getTag();
        if (o instanceof Dept) {
            // 组织
            Dept d = (Dept) o;
            if (d.id.equals("906")) {
                Intent intent = new Intent(view.getContext(),
                        PhoneContactActivity.class);
                startActivity(intent);
            } else {
                clearData();
                dept = d;
                syncData();
            }
        } else if (o instanceof Contact) {
            Contact c = (Contact) o;
            c.selected = !c.selected;
            ImageView ivSelect = (ImageView) view.findViewById(R.id.ivSelect);
            if (c.selected) {
                ivSelect.setImageResource(R.drawable.ic_selected);
            } else {
                ivSelect.setImageResource(R.drawable.ic_unselected);
            }
            int count = getSelectedContact(dept).size() + 1;
            btnSubmit.setText("确定(" + count + "/1000)");
            btnSubmit.setEnabled(count > 1);
            if (btnSubmit.isEnabled()) {
                btnSubmit.setBackgroundResource(R.drawable.button_normal);
            } else {
                btnSubmit.setBackgroundResource(R.drawable.button_disable);
            }
        }
    }

    private void getDept(Integer userId, String id) {
        String url = Config.SERVER_HOST + Config.URL_GET_CONTACTS.replace("{userId}", userId + "");
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (!result.equals("fail")) {
                    try {
                        uList.addAll(Contact.getFriendsFromJSON(new JSONObject(result)));
                        syncData();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
//        Dept tmp = Dept.fromDataById(this, "");
//        if (tmp != null) {
//            clearData();
//            dept = tmp;
//            syncData();
//            return;
//        }
//        String code = id.length() > 0 ? "&id=" + id : "";
//        String url;
//        if (id.length() < 0) {
//            url = Config.SERVER_HOST + "contact.json" + "?userId=" + userId
//                    + code;
//        } else {
//            url = Config.SERVER_HOST + "contact" + id + ".json" + "?userId="
//                    + userId + code;
//        }
//        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
//            @Override
//            public void callBack(String result) {
//                if (result != "fail") {
//                    try {
//                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
//                            clearData();
//                            dept = Dept.fromJSON(new JSONObject(result));
//                            groupList.addAll(Group
//                                    .getGroupListFromJSON(new JSONObject(result)));
//                            uList.addAll(Contact
//                                    .getFriendsFromJSON(new JSONObject(result)));
//                            Dept.updateData(CreateGroupActivity.this, dept);
//                            syncData();
//                        }
//                    } catch (Exception e) {
//                        if (Logger.DEBUG) {
//                            e.printStackTrace();
//                        }
//                        show(Config.ERROR_INTERFACE);
//                    }
//                } else {
//                    show(Config.ERROR_NETWORK);
//                }
//
//            }
//        });
    }

    private void show(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
