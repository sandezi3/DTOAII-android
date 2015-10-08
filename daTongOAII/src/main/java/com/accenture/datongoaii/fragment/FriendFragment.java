package com.accenture.datongoaii.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.ContactProfileActivity;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.FirstPinYin;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;
import com.accenture.datongoaii.widget.SectionListView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class FriendFragment extends Fragment implements SectionListView.OnSectionItemClickedListener {
    public static final int HANDLER_TAG_DISMISS_PROGRESS_DIALOG = 0;
    private Context context;
    private List<Object> viewList;
    private List<Contact> friends;
    private List<Object> tmpList;
    public Boolean isSelectMode;

    private ProgressDialog progressDialog;

    private Handler handler = new FragmentHandler(this);

    static class FragmentHandler extends Handler {
        WeakReference<FriendFragment> mFragment;

        FragmentHandler(FriendFragment fragment) {
            this.mFragment = new WeakReference<FriendFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    FriendFragment a = mFragment.get();
                    if (a.progressDialog.isShowing()) {
                        a.progressDialog.dismiss();
                        a.progressDialog = null;
                    }
            }
        }

    }

    private void clearData() {
        tmpList.clear();
        viewList.clear();
    }

    private void syncData() {
        viewList.addAll(friends);
        tmpList.clear();
        tmpList.addAll(viewList);
        viewList.clear();
        viewList.addAll(FirstPinYin.createPinYinGroupedList(tmpList));
        tmpList.clear();
        tmpList.addAll(viewList);
        adapter.notifyDataSetChanged();
    }

    private final SectionListView.SectionListAdapter adapter = new SectionListView.SectionListAdapter() {
        @Override
        public int getSectionCount() {
            return viewList.size();
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
            if (o instanceof Contact) {
                tv.setText(((Contact) o).mFirstPinYin);
            }
            return view;
        }

        @Override
        public View getSectionItemView(int section, int position,
                                       View convertView, ViewGroup parent) {
            Contact c = (Contact) getItem(section, position);
            View view = convertView;
            if (view == null) {
                view = View.inflate(parent.getContext(),
                        R.layout.list_cell_contact, null);
            }
            TextView tv = (TextView) view.findViewById(R.id.tvName);
            ImageView iv = (ImageView) view.findViewById(R.id.ivIcon);
            TextView tvAdded = (TextView) view.findViewById(R.id.tvAdded);
            Button btnAdd = (Button) view.findViewById(R.id.btnAdd);
            ImageLoader il = ImageLoader.getInstance();
            if (c.head.length() > 0) {
                il.displayImage(c.head, iv);
            } else {
                iv.setImageResource(R.drawable.ic_contact_p);
            }
            tv.setText(c.name);
            if (c.isUser) {
                switch (c.friendStatus) {
                    case FRIENDS_STATUS_FRIEND: {
                        tvAdded.setVisibility(View.VISIBLE);
                        tvAdded.setText("已添加");
                        btnAdd.setVisibility(View.GONE);
                    }
                    break;
                    case FRIENDS_STATUS_FROM_ME_NOT_ACCEPT: {
                        tvAdded.setVisibility(View.VISIBLE);
                        tvAdded.setText("等待对方接受");
                        btnAdd.setVisibility(View.GONE);
                    }
                    break;
                    case FRIENDS_STATUS_TO_ME_NOT_ACCEPT: {
                        tvAdded.setVisibility(View.GONE);
                        btnAdd.setVisibility(View.VISIBLE);
                        btnAdd.setText("接受");
                        btnAdd.setTag(c);
                        btnAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Contact c = (Contact) view.getTag();
                                startAddFriendsConnect(c.id, c.friendStatus);
                            }
                        });
                    }
                    break;
                    case FRIENDS_STATUS_TO_BE_FRIEND: {
                        tvAdded.setVisibility(View.GONE);
                        btnAdd.setVisibility(View.VISIBLE);
                        btnAdd.setText("添加");
                        btnAdd.setTag(c);
                        btnAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Contact c = (Contact) view.getTag();
                                startAddFriendsConnect(c.id, c.friendStatus);
                            }
                        });
                    }
                }
            } else {
                if (!Utils.isValidCellNumber(c.cell)) {
                    tvAdded.setText("非手机号");
                    tvAdded.setVisibility(View.VISIBLE);
                    btnAdd.setVisibility(View.GONE);
                } else {
                    tvAdded.setVisibility(View.GONE);
                    btnAdd.setVisibility(View.GONE);
                }
            }
            view.setTag(c);
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
    public void onSectionItemClicked(SectionListView listView, View view, int section, int position) {
        Object object = viewList.get(section);
        @SuppressWarnings("unchecked")
        List<Contact> list = (List<Contact>) object;
        Contact c = list.get(position);
        if (isSelectMode) {
            Intent intent = new Intent();
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_CELL, c.cell);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_NAME, c.name);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_ID, c.id);
            ((Activity)context).setResult(Activity.RESULT_OK, intent);
            ((Activity)context).finish();
        } else {
            Intent intent = new Intent(view.getContext(), ContactProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_TAG_CONTACT_PROFILE, c);
            intent.putExtras(bundle);
            startActivity(intent);
            ((Activity)context).finish();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = this.getActivity();
        View layoutContact = inflater.inflate(R.layout.frag_friends, container,
                false);

        SectionListView slvContact = (SectionListView) layoutContact.findViewById(R.id.slvContact);
        viewList = new ArrayList<Object>();
        friends = new ArrayList<Contact>();
        tmpList = new ArrayList<Object>();
        slvContact.setAdapter(adapter);
        slvContact.setOnSectionItemClickedListener(this);
        getFriends(Account.getInstance().getUserId());

        return layoutContact;
    }

    private void getFriends(Integer userId) {
        String url = Config.SERVER_HOST + Config.URL_GET_CONTACTS.replace("{userId}", userId + "");
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (!result.equals("fail")) {
                    try {
                        List<Contact> list = Contact.getFriendsFromJSON(new JSONObject(result));
                        if (list != null) {
                            clearData();
                            friends.addAll(list);
                            syncData();
                            if (list.size() > 0) {
                                startGetContactsStatusConnect();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void startAddFriendsConnect(Integer id, Contact.FriendStatus status) {
        if (progressDialog != null) {
            progressDialog.setMessage(Config.PROGRESS_SEND);
            progressDialog.show();
        } else {
            progressDialog = ProgressDialog.show(context, null, Config.PROGRESS_SEND);
        }
        String op = null;
        switch (status) {
            case FRIENDS_STATUS_TO_BE_FRIEND:
                op = "add";
                break;
            case FRIENDS_STATUS_TO_ME_NOT_ACCEPT:
                op = "accept";
                break;
        }
        String url = Config.SERVER_HOST + Config.URL_ADD_FRIEND;
        JSONObject object = new JSONObject();
        try {
            object.put("toUserId", id);
            object.put("op", op);
        } catch (JSONException e) {
            Utils.toast(context, Config.ERROR_APP);
        }
        new HttpConnection().post(url, object, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            startGetContactsStatusConnect();
                        } else {
                            Utils.toast(context, cr.statusMsg);
                        }
                    } catch (JSONException e) {
                        Utils.toast(context, e.getMessage());
                    }
                }
            }
        });
    }

    private void startGetContactsStatusConnect() {
        String url = Config.SERVER_HOST + Config.URL_GET_USER_STATUS;
        JSONObject obj = new JSONObject();
        try {
            JSONArray array = new JSONArray();
            for (Object o : friends) {
                Contact c = (Contact) o;
                array.put(c.cell);
            }
            obj.put("cells", array);
        } catch (JSONException e) {
            Utils.toast(context, e.getMessage());
            return;
        }
        new HttpConnection().post(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        assert (cr != null);
                        if (cr.statusCode == 0) {
                            clearData();
                            Contact.resolveContactList(new JSONObject(result), friends);
                            syncData();
                        } else {
                            Utils.toast(context, cr.statusMsg);
                        }
                    } catch (JSONException e) {
                        Logger.e("startGetContactsStatusConnect", e.getMessage());
                    }
                }
            }
        });
    }
}
