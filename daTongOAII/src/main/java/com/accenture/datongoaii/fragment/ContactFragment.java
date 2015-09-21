package com.accenture.datongoaii.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.DeptActivity;
import com.accenture.datongoaii.activity.ManageOrgActivity;
import com.accenture.datongoaii.activity.MyFriendActivity;
import com.accenture.datongoaii.activity.PhoneContactActivity;
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactFragment extends Fragment implements
        OnSectionItemClickedListener {
    public Org org;
    private List<Object> viewList;
    //    private List<Object> groupList;
    private List<Contact> friends;
    //    private List<Object> uList;
    private List<Object> tmpList;
    private Org selectOrg;

    private ImageView ivSearch;
    private TextView tvSearch;

    //    public static class ContactFragHandler extends Handler {
//        WeakReference<ContactFragment> theFrag = null;
//    }
    private void clearData() {
        tmpList.clear();
        viewList.clear();
//		groupList.clear();
//		uList.clear();
    }

    private void syncData() {
        viewList.add(org);
        appendLocalData();
        tmpList.clear();
        tmpList.addAll(viewList);
        viewList.clear();
        viewList.addAll(FirstPinYin.createPinYinGroupedList(tmpList));
        tmpList.clear();
        tmpList.addAll(viewList);
        adapter.notifyDataSetChanged();
    }

    private void appendLocalData() {
        Dept d0 = new Dept();
        d0.id = Dept.DEPT_ID_MY_FRIENDS;
        d0.name = "我的好友";
        d0.img = null;
        d0.mFirstPinYin = "*";
        Dept d1 = new Dept();
        d1.id = Dept.DEPT_ID_PHONE_CONTACT;
        d1.name = "手机通讯录";
        d1.img = null;
        d1.mFirstPinYin = "*";
        viewList.add(d0);
        viewList.add(d1);
    }

    private final SectionListAdapter adapter = new SectionListAdapter() {
        @Override
        public int getSectionCount() {
            if (viewList == null) {
                return 0;
            } else {
                return viewList.size();
            }
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
                Dept department = (Dept) o;
                if (department.id.equals(-2)) {
                    tv.setText("*");
                }
            } else if (o instanceof Org) {
                tv.setText("组织架构");
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
            ImageLoader il = ImageLoader.getInstance();
            if (o instanceof Dept) {
                Dept d = (Dept) o;
                if (d.img != null && d.img.length() > 0) {
                    il.displayImage(d.img, iv, Config.getDisplayOptions());
                } else {
                    iv.setImageDrawable(view.getContext().getResources().getDrawable(R.drawable.ic_contact_c));
                }
                tv.setText(d.name);
            } else if (o instanceof Org) {
                Org org = (Org) o;
                if (org.logo != null && org.logo.length() > 0) {
                    il.displayImage(org.logo, iv, Config.getDisplayOptions());
                } else {
                    iv.setImageDrawable(view.getContext().getResources().getDrawable(R.drawable.ic_contact_c));
                }
                tv.setText(org.orgName);
                if (org.createUserId.equals(Account.getInstance().getUserId())) {
                    Button btnAdd = (Button) view.findViewById(R.id.btnAdd);
                    btnAdd.setVisibility(View.VISIBLE);
                    btnAdd.setText("管理");
                    btnAdd.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(view.getContext(), ManageOrgActivity.class);
                            ContactFragment.this.startActivityForResult(intent, Constants.REQUEST_CODE_MANAGE_ORG);
                        }
                    });
                    view.setTag(org);
                    view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Org org1 = (Org) v.getTag();
                            Intent intent = new Intent(v.getContext(), DeptActivity.class);
                            intent.putExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_ID, org1.orgId);
                            intent.putExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_NAME, org1.orgName);
                            startActivity(intent);
                        }
                    });
                }
            } else if (o instanceof Group) {
                Group g = (Group) o;
                if (g.img.length() > 0) {
                    il.displayImage(g.img, iv, Config.getDisplayOptions());
                }
                tv.setText(g.name);
            } else if (o instanceof Contact) {
                Contact c = (Contact) o;
                if (c.head.length() > 0) {
                    il.displayImage(c.head, iv);
                } else {
                    iv.setImageResource(R.drawable.ic_contact_p);
                }
                tv.setText(c.name);
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

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layoutContact = inflater.inflate(R.layout.frag_contact, container,
                false);

        EditText etSearch = (EditText) layoutContact.findViewById(R.id.etSearch);
        ivSearch = (ImageView) layoutContact.findViewById(R.id.ivSearch);
        tvSearch = (TextView) layoutContact.findViewById(R.id.tvSearch);
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

        SectionListView slvContact = (SectionListView) layoutContact
                .findViewById(R.id.slvContact);
        viewList = new ArrayList<Object>();
        tmpList = new ArrayList<Object>();
        slvContact.setAdapter(adapter);
        slvContact.setOnSectionItemClickedListener(this);
        getOrg(Account.getInstance().getUserId());

        ImageButton ibAdd = (ImageButton) layoutContact.findViewById(R.id.ibAdd);
        ibAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactAddPopupWindow capw = new ContactAddPopupWindow(v.getContext(), v, Account.getInstance().getOrg() == null);
                capw.showAsDropDown();
            }
        });

        ImageButton ibMore = (ImageButton) layoutContact
                .findViewById(R.id.ibMore);
        ibMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MorePopupWindow mpw = new MorePopupWindow(v.getContext(), v);
                mpw.showAsDropDown();
            }
        });

        return layoutContact;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CREATE_DEPT
                && resultCode == Activity.RESULT_OK) {
            getOrg(Account.getInstance().getUserId());
            return;
        }
        if (requestCode == Constants.REQUEST_CODE_CREATE_ORG && resultCode == Activity.RESULT_OK) {
            getOrg(Account.getInstance().getUserId());
            return;
        }
        if (requestCode == Constants.REQUEST_CODE_MANAGE_ORG
                && resultCode == Activity.RESULT_OK) {
            getOrg(Account.getInstance().getUserId());
            return;
        }
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

    public void getOrg(Integer userId) {
//        Dept tmp = Dept.fromDataById(this.getActivity(), "");
//        if (tmp != null) {
//            clearData();
//            dept = tmp;
//            syncData();
//            return;
//        }
        String url = Config.SERVER_HOST + Config.URL_ORG.replace("{userId}", userId + "");
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (!result.equals("fail")) {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            clearData();
                            org = Org.fromJSON(new JSONObject(result));
                            Account.getInstance().setOrg(org);
//                            else {
//                                Dept root = Dept.getRootDept(dept);
//                                Dept dest = Dept.getDestDeptById(root, deptId);
//                                Dept.updateDept(dest, Dept.childFromJson((new JSONObject(result)).getJSONObject("data"), dest));
//                                Account.getInstance().setDept(root);
//                            }
//                            groupList.addAll(Group
//                                    .getGroupListFromJSON(new JSONObject(result)));
//                            uList.addAll(Contact
//                                    .getFriendsFromJSON(new JSONObject(result)));
//                            Dept.updateData(getActivity(), dept);
                            syncData();
                        }
                    } catch (Exception e) {
                        if (Logger.DEBUG) {
                            e.printStackTrace();
                        }
                        show(Config.ERROR_INTERFACE);
                    }
                } else {
                    show(Config.ERROR_NETWORK);
                }

            }
        });
    }

    private void getFriends(Integer userId) {
        String url = Config.SERVER_HOST + Config.URL_GET_CONTACTS.replace("{userId}", userId + "");
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (!result.equals("fail")) {
                    try {
                        friends = new ArrayList<Contact>();
                        List<Contact> list = Contact.getFriendsFromJSON(new JSONObject(result));
                        if (list != null) {
                            friends.addAll(list);
                            Intent intent = new Intent(ContactFragment.this.getActivity(), MyFriendActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Constants.BUNDLE_TAG_FRIENDS, (Serializable) friends);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    public void onSectionItemClicked(SectionListView listView, View view,
                                     int section, int position) {
        Object o = view.getTag();
        if (o instanceof Dept) {
            // 组织
            Dept d = (Dept) o;
            if (d.id.equals(Dept.DEPT_ID_PHONE_CONTACT)) {
                Intent intent = new Intent(view.getContext(),
                        PhoneContactActivity.class);
                startActivity(intent);
            } else if (d.id.equals(Dept.DEPT_ID_MY_FRIENDS)) {
                getFriends(Account.getInstance().getUserId());
            }
        } else if (o instanceof Org) {
            Intent intent = new Intent(view.getContext(), DeptActivity.class);
            intent.putExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_ID, org.orgId);
            intent.putExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_NAME, org.orgName);
            startActivity(intent);
        } else {
            // 个人信息
            // TODO
            Logger.i("onSectionItemClicked", "个人信息");
        }
    }

    private void show(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

}
