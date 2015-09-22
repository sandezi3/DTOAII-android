package com.accenture.datongoaii.model;

import java.io.Serializable;
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

public class Contact extends FirstPinYin implements Serializable {
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
    public List<Dept> parents;
    public boolean selected;
    public boolean isUser;
    public FriendStatus friendStatus;

    @SuppressLint("DefaultLocale")
    public static Contact fromJSON(JSONObject json) {
        try {
            Contact c = new Contact();
            c.id = json.getInt("userId");
            c.name = json.getString("username");
            c.head = json.getString("photo");
            c.cell = json.getString("cell");
            c.isUser = false;
            c.friendStatus = FriendStatus.FRIENDS_STATUS_TO_BE_FRIEND;
            CharacterParser cp = CharacterParser.getInstance();
            c.mFirstPinYin = cp.getSelling(c.name).substring(0, 1).toUpperCase();
            c.selected = false;
            return c;
        } catch (Exception e) {
            if (Logger.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<Contact> getFriendsFromJSON(JSONObject json) {
        try {
            List<Contact> list = new ArrayList<Contact>();
            JSONArray array = json.getJSONObject("data").getJSONArray("friends");
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

    public static List<Contact> getListFromDataByPdid(Context context, Integer pdid) {
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
            hasResolveContactByJSONArrayAndStatus(c, toBeFriends, FriendStatus.FRIENDS_STATUS_TO_BE_FRIEND);
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
