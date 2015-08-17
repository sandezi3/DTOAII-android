package com.accenture.datongoaii.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;

import com.accenture.datongoaii.db.ContactDao;
import com.accenture.datongoaii.util.CharacterParser;
import com.accenture.datongoaii.util.Logger;

public class Contact extends FirstPinYin {
	public String id;
	public String name;
	public String cell;
	public String head;
	public Dept parent;
	public boolean selected;

	@SuppressLint("DefaultLocale")
	public static Contact fromJSON(JSONObject json) {
		try {
			Contact c = new Contact();
			c.id = json.getString("id");
			c.name = json.getString("name");
			c.head = json.getString("head");
			CharacterParser cp = CharacterParser.getInstance();
			c.mFirstPinYin = cp.getSelling(c.name).substring(0, 1).toUpperCase();
			c.parent = null;
			c.selected = false;
			return c;
		} catch (Exception e) {
			if (Logger.DEBUG) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static List<Contact> getUListFromJSON(JSONObject json) {
		try {
			List<Contact> list = new ArrayList<Contact>();
			JSONArray array = json.getJSONArray("uList");
			for (int i = 0; i < array.length(); i++) {
				JSONObject o = array.getJSONObject(i);
				Contact c = Contact.fromJSON(o);
				list.add(c);
			}
			return list;
		} catch (Exception e) {
			if (Logger.DEBUG) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static List<Contact> getAListFromJSON(JSONObject json) {
		try {
			List<Contact> list = new ArrayList<Contact>();
			JSONArray array = json.getJSONArray("aList");
			for (int i = 0; i < array.length(); i++) {
				JSONObject o = array.getJSONObject(i);
				Contact c = Contact.fromJSON(o);
				list.add(c);
			}
			return list;
		} catch (Exception e) {
			if (Logger.DEBUG) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static List<Contact> getListFromDataByPdid(Context context, String pdid) {
		ContactDao cd = new ContactDao(context);
		return cd.getListByPdid(pdid);
	}
}
