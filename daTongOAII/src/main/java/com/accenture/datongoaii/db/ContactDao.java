package com.accenture.datongoaii.db;

import java.util.ArrayList;
import java.util.List;

import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.util.CharacterParser;
import com.accenture.datongoaii.util.Logger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

@SuppressLint("DefaultLocale")
public class ContactDao {
	private Context context;

	public static final String CONTACT_TABLE = "contact_table";

	public static final String CONTACT_TABLE_ID = "id";
	public static final String CONTACT_TABLE_NAME = "name";
	public static final String CONTACT_TABLE_HEAD = "head";
	public static final String CONTACT_TABLE_PDID = "pdid";

	private static final String TAG = "ContactCacheDb";

	public ContactDao(Context context) {
		this.context = context;
	}

	public void add(Contact c) {
		try {
			SQLiteDatabase db = DBHelper.getInstance(context)
					.getWritableDatabase();
			StringBuffer sb = new StringBuffer();
			sb.append("insert into ");
			sb.append(CONTACT_TABLE);
			sb.append("(");
			sb.append(CONTACT_TABLE_ID);
			sb.append(",");
			sb.append(CONTACT_TABLE_NAME);
			sb.append(",");
			sb.append(CONTACT_TABLE_HEAD);
			sb.append(",");
			sb.append(CONTACT_TABLE_PDID);
			sb.append(") values(?,?,?,?)");
			Logger.i("ContactCacheDB", "id = " + c.id);
			Object[] oArray = null;
			if (c.parent != null) {
				oArray = new Object[] { c.id, c.name, c.head, c.parent.id };
			} else {
				oArray = new Object[] { c.id, c.name, c.head, "" };
			}
			db.execSQL(sb.toString(), oArray);
			db.close();
		} catch (Exception e) {
			Logger.e(TAG, "ContactCacheDB add is error:" + e.getMessage());
		}
	}

	public List<Contact> getListByPdid(String pdid) {
		List<Contact> contactList = new ArrayList<Contact>();
		try {
			SQLiteDatabase db = DBHelper.getInstance(context)
					.getReadableDatabase();
			StringBuffer sb = new StringBuffer();
			sb.append("select * from ");
			sb.append(CONTACT_TABLE);
			sb.append(" where ");
			sb.append(CONTACT_TABLE_PDID);
			sb.append("=?");
			Cursor c = db.rawQuery(sb.toString(), new String[] { pdid });
			if (c != null) {
				c.moveToFirst();
				while (!c.isAfterLast()) {
					Contact cc = new Contact();
					cc.id = c.getString(c.getColumnIndex(CONTACT_TABLE_ID));
					cc.name = c.getString(c.getColumnIndex(CONTACT_TABLE_NAME));
					cc.head = c.getString(c.getColumnIndex(CONTACT_TABLE_HEAD));
					cc.mFirstPinYin = CharacterParser.getInstance()
							.getSelling(cc.name).substring(0, 1).toUpperCase();
					contactList.add(cc);
					c.moveToNext();
				}
				if (c != null) {
					c.close();
				}
			}
			db.close();

		} catch (Exception e) {
			Logger.e(
					TAG,
					"ContactCacheDB getContactListByPdid is error:"
							+ e.getMessage());
		}

		return contactList;
	}

	public void deleteByPdid(String id) {
		try {
			SQLiteDatabase db = DBHelper.getInstance(context)
					.getWritableDatabase();
			db.delete(CONTACT_TABLE, CONTACT_TABLE_PDID + "=?",
					new String[] { id });
			db.close();
		} catch (Exception e) {
			Logger.e(
					TAG,
					"ContactCacheDB deleteContactByPdid is error:"
							+ e.getMessage());
		}
	}

}
