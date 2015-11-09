package com.accenture.datongoaii.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.DTOARequest;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.SelectUserActivity;
import com.accenture.datongoaii.db.ContactDao;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.FirstPinYin;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.CharacterParser;
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

public class PhoneContactFragment extends Fragment implements SectionListView.OnSectionItemClickedListener {
    public boolean isSelectMode;
    public boolean isMultiMode;

    private Activity context;
    private List<Object> dataList;
    private List<Contact> contactList;
    private ProgressDialog progressDialog;
    private Handler handler = new FragmentHandler(this);

    static class FragmentHandler extends Handler {
        WeakReference<PhoneContactFragment> mFragment;

        FragmentHandler(PhoneContactFragment fragment) {
            this.mFragment = new WeakReference<PhoneContactFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    PhoneContactFragment a = mFragment.get();
                    if (a.progressDialog.isShowing()) {
                        a.progressDialog.dismiss();
                        a.progressDialog = null;
                    }
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getActivity();
        View view = inflater.inflate(R.layout.frag_phone_contact, container, false);

        SectionListView slvContact = (SectionListView) view.findViewById(R.id.slvContact);
        contactList = new ArrayList<Contact>();
        slvContact.setAdapter(adapter);
        slvContact.setOnSectionItemClickedListener(this);

        getPhoneContact();
        return view;
    }

    @Override
    public void onSectionItemClicked(SectionListView listView, View view, int section, int position) {
        Object object = dataList.get(section);
        @SuppressWarnings("unchecked")
        List<Contact> list = (List<Contact>) object;
        Contact c = list.get(position);
        onContactClicked(c);
    }

    private void onContactClicked(Contact c) {
        if (isMultiMode) {
            ((SelectUserActivity) context).onFragmentItemClick(c);
            return;
        }
        if (isSelectMode) {
            finishAndReturn(c);
        }
    }

    @SuppressLint("DefaultLocale")
    @SuppressWarnings("unchecked")
    private void getPhoneContact() {
        ContentResolver resolver = context.getContentResolver();
        Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                Contact c = new Contact();
                c.id = -1;
                c.selected = false;
                c.head = "";
                // 得到联系人名称
                c.name = phoneCursor.getString(0);
                // 得到手机号码
                c.cell = resolveCell(phoneCursor.getString(1));
                // 当手机号码为空的或者为空字段 跳过当前循环
                if (c.cell == null)
                    continue;
                CharacterParser cp = CharacterParser.getInstance();
                String pinyin = cp.getSelling(c.name);
                c.mFirstPinYin = pinyin.substring(0, 1).toUpperCase();
                contactList.add(c);
            }

            phoneCursor.close();
        }
        refreshData();
        startGetContactsStatusConnect();
    }

    @SuppressWarnings("unchecked")
    private void refreshData() {
        dataList = (List<Object>) FirstPinYin.createPinYinGroupedList(contactList);
        adapter.notifyDataSetChanged();
    }

    private String resolveCell(String cell) {
        cell = cell.trim().replace(" ", "");
        if (cell.contains("+86") && cell.length() > 13) {
            return cell.substring(cell.length() - 11);
        }
        if (TextUtils.isEmpty(cell)) {
            return null;
        }
        if (!Utils.isValidCellNumber(cell)) {
            return null;
        }
        if (cell.equals(Account.getInstance().getCell())) {
            return null;
        }
        return cell;
    }

    private void finishAndReturn(Contact c) {
        Intent intent = new Intent();
        intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_CELL, c.cell);
        intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_NAME, c.name);
        intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_ID, c.id);
        context.setResult(Activity.RESULT_OK, intent);
        context.finish();
    }

    private void startGetContactsStatusConnect() {
        String url = Config.SERVER_HOST + Config.URL_GET_USER_STATUS;
        JSONObject obj = new JSONObject();
        try {
            JSONArray array = new JSONArray();
            for (Object o : contactList) {
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
                            Contact.resolveContactList(new JSONObject(result), contactList);
                            refreshData();
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

    private void startAddFriendsConnect(Integer id, Contact.FriendStatus status) {
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
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_SEND);
        new HttpConnection().post(url, object, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr.statusCode == 0) {
                            // 清数据库，更新好友
                            new ContactDao(context).saveFriends(null);
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

    private void startInviteFriendConnect(String cell) {
        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_SEND);
        DTOARequest.getInstance(context).startInviteFriendConnect(cell, new DTOARequest.RequestListener() {
            @Override
            public void callback(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                startGetContactsStatusConnect();
            }
        });
    }

    private final SectionListView.SectionListAdapter adapter = new SectionListView.SectionListAdapter() {
        @Override
        public int getSectionCount() {
            if (dataList == null) {
                return 0;
            } else {
                return dataList.size();
            }
        }

        @Override
        public int getSectionItemCount(int section) {
            Object object = dataList.get(section);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) object;
            return list.size();
        }

        @Override
        public View getSectionHeaderView(int section, View convertView,
                                         ViewGroup parent) {
            Contact c = (Contact) getItem(section, 0);
            View view = convertView;
            if (view == null) {
                view = View.inflate(parent.getContext(),
                        R.layout.section_list_header, null);
            }
            TextView tv = (TextView) view.findViewById(R.id.txtLabel);
            tv.setText(c.mFirstPinYin);
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
                        tvAdded.setText(getResources().getText(R.string.label_added));
                        btnAdd.setVisibility(View.GONE);
                    }
                    break;
                    case FRIENDS_STATUS_FROM_ME_NOT_ACCEPT: {
                        tvAdded.setVisibility(View.VISIBLE);
                        tvAdded.setText(getResources().getText(R.string.label_wait_accept));
                        btnAdd.setVisibility(View.GONE);
                    }
                    break;
                    case FRIENDS_STATUS_TO_ME_NOT_ACCEPT: {
                        tvAdded.setVisibility(View.GONE);
                        btnAdd.setVisibility(View.VISIBLE);
                        btnAdd.setText(getResources().getText(R.string.btn_accept));
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
                        btnAdd.setText(getResources().getText(R.string.btn_add));
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
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Contact c = (Contact) view.getTag();
                        onContactClicked(c);
                    }
                });
            } else {
                if (!Utils.isValidCellNumber(c.cell)) {
                    tvAdded.setText(getResources().getText(R.string.label_not_cell));
                    tvAdded.setVisibility(View.VISIBLE);
                    btnAdd.setVisibility(View.GONE);
                } else if (c.isInvited) {
                    tvAdded.setText(getResources().getText(R.string.label_invited));
                    tvAdded.setVisibility(View.VISIBLE);
                    btnAdd.setVisibility(View.GONE);
                } else {
                    tvAdded.setVisibility(View.GONE);
                    btnAdd.setVisibility(View.VISIBLE);
                    btnAdd.setText(getResources().getText(R.string.btn_invite));
                    btnAdd.setTag(c);
                    btnAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Contact c = (Contact) view.getTag();
                            startInviteFriendConnect(c.cell);
                        }
                    });
                }
            }

            if (isSelectMode) {
                btnAdd.setVisibility(View.INVISIBLE);
                tvAdded.setVisibility(View.INVISIBLE);
            }
            view.setTag(c);
            return view;
        }

        @Override
        public Object getItem(int section, int position) {
            Object object = dataList.get(section);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) object;
            return list.get(position);
        }

        @Override
        public String getSectionLabel(int section) {
            Contact c = (Contact) getItem(section, 0);
            return c.mFirstPinYin;
        }
    };
}
