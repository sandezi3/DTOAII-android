package com.accenture.datongoaii.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SelectPhoneContactActivity extends Activity implements
        OnSectionItemClickedListener {
    private Context context;
    private List<Object> dataList;
    private List<Contact> contactList;
    private boolean isSelectMode;

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
            btnAdd.setVisibility(View.INVISIBLE);
            tvAdded.setVisibility(View.INVISIBLE);
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
                        SelectPhoneContactActivity.this.finish();
                    }
                });

        SectionListView slvContact = (SectionListView) findViewById(R.id.slvContact);
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
        setResult(Activity.RESULT_OK, intent);
        finish();
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

    @Override
    public void onSectionItemClicked(SectionListView listView, View view,
                                     int section, int position) {
        Object object = dataList.get(section);
        @SuppressWarnings("unchecked")
        List<Contact> list = (List<Contact>) object;
        Contact c = list.get(position);
        if (isSelectMode) {
            finishAndReturn(c);
        }
    }
}