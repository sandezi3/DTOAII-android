package com.accenture.datongoaii.model;

import android.annotation.SuppressLint;
import android.content.Context;

import com.accenture.datongoaii.db.ContactDao;
import com.accenture.datongoaii.util.CharacterParser;
import com.accenture.datongoaii.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    public String imId;
    public Dept parent;
    public List<Dept> parents;
    public boolean selected;
    public boolean isUser;
    public boolean isInvited;
    public FriendStatus friendStatus;

    @SuppressLint("DefaultLocale")
    public static Contact fromJSON(JSONObject json) {
        try {
            Contact c = new Contact();
            c.id = json.getInt("userId");
            c.name = json.getString("username");
            c.head = json.getString("photo");
            c.cell = json.getString("cell");
            if (json.has("imId")) {
                c.imId = json.getString("imId");
            } else {
                c.imId = "";
            }
            c.isUser = false;
            c.isInvited = false;
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

    public static List<Contact> resolveContactList(JSONObject obj, List<Contact> list) throws JSONException {
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

    public static boolean contains(List<Contact> list, Contact contact) {
        for (Contact c : list) {
            if (c.id != null && c.id.equals(contact.id) && c.id != -1) {
                return true;
            }
            if (c.cell != null && c.cell.equals(contact.cell)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasResolveContactByJSONArrayAndStatus(Contact c, JSONArray array, FriendStatus status) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = (JSONObject) array.get(i);
            if (c.cell.equals(object.getString("cell"))) {
                if (object.has("userId") && object.getInt("userId") > 0) {
                    c.isUser = true;
                    c.id = object.getInt("userId");
                } else if (object.has("") && object.getBoolean("")) {
                    // TODO
                    c.isInvited = true;
                }
                c.friendStatus = status;
                return true;
            }
        }
        return false;
    }
}
