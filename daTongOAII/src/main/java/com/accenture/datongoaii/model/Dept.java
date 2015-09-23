package com.accenture.datongoaii.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.accenture.datongoaii.util.Logger;

public class Dept extends FirstPinYin implements Serializable {
    public static final int DEPT_ID_MY_FRIENDS = -2;
    @SuppressWarnings("unused")
    public static final int DEPT_ID_MY_GROUPS = -3;
    public static final int DEPT_ID_PHONE_CONTACT = -4;
    public static final int DEPT_ID_ROOT_CONTACT = -5;

    public Integer id;
    public String version;
    public String name;
    public String img;
    public Integer userCount;
    public Dept parent;
    public List<Dept> subDept;
    public List<Contact> contactList;

    public static Dept fromJSON(JSONObject json) {
        try {
            Dept d = new Dept();
            if (json.has("group")) {
                try {
                    JSONObject j = json.getJSONObject("group");
                    d.id = j.getInt("groupId");
                    d.name = j.getString("groupName");
                    d.userCount = j.getInt("userNum");
                    d.img = j.getString("logo");
                } catch (JSONException e) {
                    // DO NOTHING
                }
            }
            d.mFirstPinYin = "#";
            if (json.has("parentGroup")) {
                try {
                    JSONObject j = json.getJSONObject("parentGroup");
                    Dept parent = new Dept();
                    parent.id = j.getInt("groupId");
                    parent.name = j.getString("groupName");
                    d.parent = parent;
                } catch (JSONException e) {
                    // DO NOTHING
                }
            }
            d.subDept = new ArrayList<Dept>();
            if (json.has("subGroups")) {
                try {
                    JSONArray dArray = json.getJSONArray("subGroups");
                    for (int i = 0; i < dArray.length(); i++) {
                        JSONObject o = dArray.getJSONObject(i);
                        Dept dd = new Dept();
                        dd.id = o.getInt("groupId");
                        dd.name = o.getString("groupName");
                        dd.img = "";
                        dd.userCount = o.getInt("userNum");
                        dd.mFirstPinYin = "#";
                        d.subDept.add(dd);
                    }
                } catch (JSONException e) {
                    // DO NOTHING
                }
            }
            d.contactList = new ArrayList<Contact>();
            if (json.has("userList")) {
                try {
                    JSONArray pArray = json.getJSONArray("userList");
                    for (int i = 0; i < pArray.length(); i++) {
                        JSONObject o = pArray.getJSONObject(i);
                        Contact c = Contact.fromJSON(o);
                        if (c != null) {
                            c.parent = d;
                            d.contactList.add(c);
                        }
                    }
                } catch (JSONException e) {
                    // DO NOTHING
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

    public static List<Dept> parentsFromJSON(JSONObject json) {
        List<Dept> list = new ArrayList<Dept>();
        try {
            JSONArray array = json.getJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                JSONObject j = array.getJSONObject(i);
                Dept dept = new Dept();
                dept.id = j.getInt("groupId");
                dept.name = j.getString("groupName");
                list.add(dept);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean contains(List<Dept> list, Dept dept) {
        for (Dept d : list) {
            if (d.id.equals(dept.id)) {
                return true;
            }
        }
        return false;
    }

    public static void removes(List<Dept> list, Dept dept) {
        for (int i = 0; i < list.size(); i++) {
            Dept d = list.get(i);
            if (d.id.equals(dept.id)) {
                list.remove(i);
                break;
            }
        }
    }

}
