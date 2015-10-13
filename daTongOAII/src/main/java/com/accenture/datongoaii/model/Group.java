package com.accenture.datongoaii.model;

import com.accenture.datongoaii.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Group extends FirstPinYin {
    public static final String TAG = "Group";
    public Integer id;
    public String imId;
    public String version;
    public String name;
    public String img;
    public Contact owner;
    public Integer userNum;
    public List<Contact> contactList;

    public static Group fromJSON(JSONObject json) {
        try {
            Group g = new Group();
            g.id = json.getInt("groupId");
            g.imId = json.getString("chatGroupId");
            g.name = json.getString("groupName");
            g.img = json.getString("logo");
            g.userNum = json.getInt("userNum");
            g.mFirstPinYin = "*";
            g.contactList = new ArrayList<Contact>();
            return g;
        } catch (Exception e) {
            if (Logger.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Group updateFromJSON(Group group, JSONObject json) {
        try {
            if (json.has("groupId")) {
                group = Group.fromJSON(json);
                assert (group != null);
            }
            group.owner = Contact.fromJSON(json.getJSONObject("owner"));
            List<Contact> list = Contact.getContactsFromJSON(json, "members");
            if (list != null && group.owner != null) {
                list.add(0, group.owner);
            }
            group.contactList = list;

        } catch (JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return group;
    }

    public static List<Group> getGroupListFromJSON(JSONObject json) {
        List<Group> list = new ArrayList<Group>();
        try {
            JSONArray a = json.getJSONArray("data");
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

    public static Contact getMemberByImid(Group group, String imId) {
        for (Contact contact : group.contactList) {
            if (contact.imId.equals(imId)) {
                return contact;
            }
        }
        return null;
    }
}
