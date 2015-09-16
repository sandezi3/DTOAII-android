package com.accenture.datongoaii.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.accenture.datongoaii.db.DeptDao;
import com.accenture.datongoaii.util.Logger;

public class Dept extends FirstPinYin implements Serializable {
    public static final int DEPT_ID_MY_FRIENDS = -2;
    public static final int DEPT_ID_MY_GROUPS = -3;
    public static final int DEPT_ID_PHONE_CONTACT = -4;

    public Integer id;
    public String version;
    public String name;
    public String img;
    public Integer userCount;
    public List<Dept> subDept;
    public List<Contact> contactList;

    public static Dept fromJSON(JSONObject json) {
        try {
            Dept d = new Dept();
            d.subDept = new ArrayList<Dept>();
            if (json.has("subDepts")) {
                JSONArray dArray = json.getJSONArray("subDepts");
                for (int i = 0; i < dArray.length(); i++) {
                    JSONObject o = dArray.getJSONObject(i);
                    Dept dd = new Dept();
                    dd.id = o.getInt("deptId");
                    dd.name = o.getString("deptName");
                    dd.img = "";
                    dd.userCount = o.getInt("userNum");
                    dd.mFirstPinYin = "#";
                    d.subDept.add(dd);
                }
            }
            d.contactList = new ArrayList<Contact>();
            if (json.has("userList")) {
                JSONArray pArray = json.getJSONArray("userList");
                for (int i = 0; i < pArray.length(); i++) {
                    JSONObject o = pArray.getJSONObject(i);
                    Contact c = Contact.fromJSON(o);
                    c.parent = d;
                    d.contactList.add(c);
                }
            }
            return d;
        } catch (Exception e) {
            if (Logger.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
