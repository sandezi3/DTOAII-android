package com.accenture.datongoaii.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.FirstPinYin;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.CharacterParser;
import com.accenture.datongoaii.util.Config;
import com.accenture.datongoaii.util.Constants;
import com.accenture.datongoaii.util.Intepreter;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;
import com.accenture.datongoaii.widget.SectionListView;
import com.accenture.datongoaii.widget.SectionListView.OnSectionItemClickedListener;
import com.accenture.datongoaii.widget.SectionListView.SectionListAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PhoneContactActivity extends Activity implements
        OnSectionItemClickedListener {
    public static final int HANDLER_TAG_DISMISS_PROGRESS_DIALOG = 0;

    private Context context;
    private SectionListView slvContact;
    private List<Object> dataList;
    private List<Contact> contactList;
    private boolean isSelectMode;
    private ProgressDialog progressDialog;
    private Handler handler = new ActivityHandler(this);

    static class ActivityHandler extends Handler {
        WeakReference<PhoneContactActivity> mActivity;

        ActivityHandler(PhoneContactActivity activity) {
            this.mActivity = new WeakReference<PhoneContactActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    PhoneContactActivity a = mActivity.get();
                    if (a.progressDialog.isShowing()) {
                        a.progressDialog.dismiss();
                        a.progressDialog = null;
                    }
            }
        }

    }

    private final SectionListAdapter adapter = new SectionListAdapter() {
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
                        btnAdd.setOnClickListener(new OnClickListener() {
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
                        btnAdd.setOnClickListener(new OnClickListener() {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_phone_contact);

        this.findViewById(R.id.btnBack).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PhoneContactActivity.this.finish();
                    }
                });

        slvContact = (SectionListView) findViewById(R.id.slvContact);
        contactList = new ArrayList<Contact>();
        slvContact.setAdapter(adapter);
        slvContact.setOnSectionItemClickedListener(this);

        isSelectMode = getIntent().getBooleanExtra(Constants.BUNDLE_TAG_SELECT_PHONE_CONTACT, false);

        getPhoneContact();
    }

    @SuppressLint("DefaultLocale")
    @SuppressWarnings("unchecked")
    private void getPhoneContact() {
        ContentResolver resolver = this.getContentResolver();
        Cursor phoneCursor = resolver.query(Phone.CONTENT_URI, new String[]{
                Phone.DISPLAY_NAME, Phone.NUMBER}, null, null, null);
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
                String fLetter = pinyin.substring(0, 1).toUpperCase();
                c.mFirstPinYin = fLetter;
                contactList.add(c);
            }

            phoneCursor.close();
        }
        refreshData();
        startGetContactsStatusConnect();
    }

    private void refreshData() {
        dataList = (List<Object>) FirstPinYin
                .createPinYinGroupedList(contactList);
        adapter.notifyDataSetChanged();
    }

    private String resolveCell(String cell) {
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
                            Contact.resolveContactListByIsUser(new JSONObject(result), contactList);
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
        if(progressDialog != null) {
            progressDialog.setMessage(Config.PROGRESS_SEND);
            progressDialog.show();
        } else {
            progressDialog = ProgressDialog.show(context, null, Config.PROGRESS_SEND);
        }
        String op = null;
        switch (status) {
            case FRIENDS_STATUS_TO_BE_FRIEND:
                op ="add";
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

    @Override
    public void onSectionItemClicked(SectionListView listView, View view,
                                     int section, int position) {
        Object object = dataList.get(section);
        @SuppressWarnings("unchecked")
        List<Contact> list = (List<Contact>) object;
        Contact c = list.get(position);
        if (isSelectMode) {
            Intent intent = new Intent();
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_CELL, c.cell);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_NAME, c.name);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_ID, c.id);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
}
