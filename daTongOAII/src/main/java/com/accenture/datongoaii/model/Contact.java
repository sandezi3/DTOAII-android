package com.accenture.datongoaii.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;

import com.accenture.datongoaii.db.ContactDao;
import com.accenture.datongoaii.util.CharacterParser;
import com.accenture.datongoaii.util.Logger;

public class Contact extends FirstPinYin {
    public enum FriendStatus {
        FRIENDS_STATUS_TO_BE_FRIEND,
        FRIENDS_STATUS_FROM_ME_NOT_ACCEPT,
        FRIENDS_STATUS_TO_ME_NOT_ACCEPT,
        FRIENDS_STATUS_FRIEND
    }

    public Integer id;
    public String name;
    public String cell;
    public String head;
    public Dept parent;
    public boolean selected;
    public boolean isUser;
    public FriendStatus friendStatus;

    @SuppressLint("DefaultLocale")
    public static Contact fromJSON(JSONObject json) {
        try {
            Contact c = new Contact();
            c.id = json.getInt("id");
            c.name = json.getString("name");
            c.head = json.getString("head");
            c.isUser = false;
            c.friendStatus = FriendStatus.FRIENDS_STATUS_TO_BE_FRIEND;
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

    public static List<Contact> resolveContactListByIsUser(JSONObject obj, List<Contact> list) throws JSONException {
        JSONObject json = obj.getJSONObject("data");
        JSONArray fromMeNotAccept = json.getJSONArray("fromMes");
        JSONArray toMeNotAccept = json.getJSONArray("toMes");
        JSONArray friends = json.getJSONArray("friends");
        JSONArray toBeFriends = json.getJSONArray("toBeFriends");
        for (Contact c : list) {
            if (hasResolveContactByJSONArrayAndStatus(c, fromMeNotAccept, FriendStatus.FRIENDS_STATUS_FROM_ME_NOT_ACCEPT)) {
                continue;
            }
            if (hasResolveContactByJSONArrayAndStatus(c, toMeNotAccept, FriendStatus.FRIENDS_STATUS_TO_ME_NOT_ACCEPT)) {
                continue;
            }
            if (hasResolveContactByJSONArrayAndStatus(c, friends, FriendStatus.FRIENDS_STATUS_FRIEND)) {
                continue;
            }
            if (hasResolveContactByJSONArrayAndStatus(c, toBeFriends, FriendStatus.FRIENDS_STATUS_TO_BE_FRIEND)) {
                continue;
            }
        }
        return list;
    }

    private static boolean hasResolveContactByJSONArrayAndStatus(Contact c, JSONArray array, FriendStatus status) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = (JSONObject) array.get(i);
            if (c.cell.equals(object.getString("cell"))) {
                c.isUser = true;
                int userId = object.getInt("userId");
                if (userId != 0) {
                    c.id = userId;
                }
                c.friendStatus = status;
                return true;
            }
        }
        return false;
    }
}
