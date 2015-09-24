package com.accenture.datongoaii.model;

import com.accenture.datongoaii.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Group extends FirstPinYin {
    public String id;
    public String version;
    public String name;
    public String img;
    public List<Contact> contactList;

    public static Group fromJSON(JSONObject json) {
        try {
            Group g = new Group();
            g.id = json.getString("id");
            g.name = json.getString("name");
            g.img = json.getString("img");
            g.mFirstPinYin = "*";
            g.contactList = new ArrayList<Contact>();
            JSONArray array = json.getJSONArray("pList");
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                Contact c = Contact.fromJSON(o);
                g.contactList.add(c);
            }
            return g;
        } catch (Exception e) {
            if (Logger.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<Group> getGroupListFromJSON(JSONObject json) {
        List<Group> list = new ArrayList<Group>();
        try {
            JSONArray a = json.getJSONArray("gList");
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                Group g = Group.fromJSON(o);
                list.add(g);
            }
            return list;
        } catch (JSONException e) {
            Logger.e("Group.getGroupListFromJSON", e.getMessage());
        }
        return null;
    }
}
