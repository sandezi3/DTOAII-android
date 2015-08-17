package com.accenture.datongoaii.activity;

import java.util.ArrayList;
import java.util.List;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.FirstPinYin;
import com.accenture.datongoaii.util.CharacterParser;
import com.accenture.datongoaii.widget.SectionListView;
import com.accenture.datongoaii.widget.SectionListView.OnSectionItemClickedListener;
import com.accenture.datongoaii.widget.SectionListView.SectionListAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class PhoneContactActivity extends Activity implements
		OnSectionItemClickedListener {
	private SectionListView slvContact;
	private List<Object> contactList;

	private final SectionListAdapter adapter = new SectionListAdapter() {
		@Override
		public int getSectionCount() {
			if (contactList == null) {
				return 0;
			} else {
				return contactList.size();
			}
		}

		@Override
		public int getSectionItemCount(int section) {
			Object object = contactList.get(section);
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
			ImageLoader il = ImageLoader.getInstance();
			if (c.head.length() > 0) {
				il.displayImage(c.head, iv);
			} else {
				iv.setImageResource(R.drawable.ic_contact_p);
			}
			tv.setText(c.name);
			view.setTag(c);
			return view;
		}

		@Override
		public Object getItem(int section, int position) {
			Object object = contactList.get(section);
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
		setContentView(R.layout.activity_phone_contact);

		this.findViewById(R.id.btnBack).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						PhoneContactActivity.this.finish();
					}
				});

		slvContact = (SectionListView) findViewById(R.id.slvContact);
		contactList = new ArrayList<Object>();
		slvContact.setAdapter(adapter);
		slvContact.setOnSectionItemClickedListener(this);
		getPhoneContact();
	}

	@SuppressLint("DefaultLocale")
	@SuppressWarnings("unchecked")
	private void getPhoneContact() {
		ContentResolver resolver = this.getContentResolver();
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI, new String[] {
				Phone.DISPLAY_NAME, Phone.NUMBER }, null, null, null);
		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {
				Contact c = new Contact();
				c.id = "";
				c.selected = false;
				c.head = "";
				// 得到联系人名称
				c.name = phoneCursor.getString(0);
				// 得到手机号码
				c.cell = phoneCursor.getString(1);
				// 当手机号码为空的或者为空字段 跳过当前循环
				if (TextUtils.isEmpty(c.cell))
					continue;
				CharacterParser cp = CharacterParser.getInstance();
				String pinyin = cp.getSelling(c.name);
				String fLetter = pinyin.substring(0, 1).toUpperCase();
				c.mFirstPinYin = fLetter;
				contactList.add(c);
			}

			phoneCursor.close();
		}
		contactList = (List<Object>) FirstPinYin
				.createPinYinGroupedList(contactList);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onSectionItemClicked(SectionListView listView, View view,
			int section, int position) {
		// TODO Auto-generated method stub

	}
}
