package com.accenture.datongoaii.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.accenture.datongoaii.db.DeptDao;
import com.accenture.datongoaii.util.Logger;

public class Dept extends FirstPinYin {
    public String id;
    public String version;
    public String name;
    public String img;
    public Dept parent;
    public List<Dept> subDept;
    public List<Contact> contactList;

    public static Dept fromJSON(JSONObject json) {
        try {
            json = json.getJSONObject("data");
            Dept d = new Dept();
            d.id = json.getString("id");
            d.version = json.getString("version");
            d.name = json.getString("name");
            d.img = json.getString("img");
            d.mFirstPinYin = "#";
            d.parent = null;
            d.subDept = new ArrayList<Dept>();
            JSONArray dArray = json.getJSONArray("dList");
            for (int i = 0; i < dArray.length(); i++) {
                JSONObject o = dArray.getJSONObject(i);
                Dept dd = Dept.fromJSON(o);
                dd.parent = d;
                d.subDept.add(dd);
            }
            d.contactList = new ArrayList<Contact>();
            JSONArray pArray = json.getJSONArray("pList");
            for (int i = 0; i < pArray.length(); i++) {
                JSONObject o = pArray.getJSONObject(i);
                Contact c = Contact.fromJSON(o);
                c.parent = d;
                d.contactList.add(c);
            }
            return d;
        } catch (Exception e) {
            if (Logger.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Dept fromDataById(Context context, String id) {
        DeptDao dd = new DeptDao(context);
        if (id.length() == 0) {
            return dd.getRootDept();
        } else {
            return dd.getDeptById(id);
        }
    }

    public static void updateData(Context context, Dept dept) {
        DeptDao dd = new DeptDao(context);
        if (dd.getDeptById(dept.id) != null) {
            dd.deleteDeptById(dept.id);
        }
        dd.add(dept);
    }


}
