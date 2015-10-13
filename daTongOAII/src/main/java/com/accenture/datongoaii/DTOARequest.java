package com.accenture.datongoaii;

import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by leon on 9/29/15.
 * 网络请求
 */
public class DTOARequest {
    public static final String TAG = "DTOARequest";

    public static void startGetUserByImId(String imId, HttpConnection.CallbackListener listener) {
        String url = Config.SERVER_HOST + Config.URL_GET_USER_BY_IMID.replace("{imId}", imId);
        new HttpConnection().get(url, listener);
    }

    public static void startGetUsersByImIds(List<String> ids, HttpConnection.CallbackListener listener) {
        String url = Config.SERVER_HOST + Config.URL_GET_USERS_BY_IMIDS;
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray(ids);
        try {
            object.put("imIdList", array);
        } catch (JSONException e) {
            Logger.e(TAG, e.getMessage());
            return;
        }
        new HttpConnection().post(url, object, listener);
    }

    /**
     * Created by leon on 10/12/15.
     * Group
     */
    public static void startCreateGroup(String name, String ownerId, String desc, List<Contact> users, HttpConnection.CallbackListener listener) {
        String url = Config.SERVER_HOST + Config.URL_CREATE_GROUP;
        JSONObject object = new JSONObject();
        JSONArray array1 = new JSONArray();
        JSONArray array2 = new JSONArray();
        try {
            for (Contact contact : users) {
                if (contact.imId != null && contact.imId.equals(ownerId)) {
                    continue;
                }
                if (contact.imId != null && contact.imId.length() > 0) {
                    array1.put(contact.imId);
                } else {
                    array2.put(contact.cell);
                }
            }
            object.put("members", array1);
            object.put("cells", array2);
            object.put("owner", ownerId);
            object.put("groupname", name);
            object.put("desc", desc);
        } catch (JSONException e) {
            Logger.e(TAG, e.getMessage());
            return;
        }
        new HttpConnection().post(url, object, listener);
    }

    public static void startGetGroups(String imId, HttpConnection.CallbackListener listener) {
        String url = Config.SERVER_HOST + Config.URL_GET_GROUPS.replace("{imId}", imId);
        new HttpConnection().get(url, listener);
    }

    public static void startGetGroup(String groupId, HttpConnection.CallbackListener listener) {
        String url = Config.SERVER_HOST + Config.URL_GET_GROUP.replace("{chatGroupId}", String.valueOf(groupId));
        new HttpConnection().get(url, listener);
    }

    public static void startInviteGroupMembers(String groupId, List<Contact> users, HttpConnection.CallbackListener listener) {
        String url = Config.SERVER_HOST + Config.URL_GROUP_INVITE_MEMBER.replace("{groupId}", groupId);
        JSONArray array1 = new JSONArray();
        JSONArray array2 = new JSONArray();
        JSONObject object = new JSONObject();
        try {
            for (Contact contact : users) {
                if (contact.imId != null && contact.imId.length() > 0) {
                    array1.put(contact.imId);
                } else {
                    array2.put(contact.cell);
                }
            }
            object.put("imIds", array1);
            object.put("cells", array2);
        } catch (JSONException e) {
            Logger.e(TAG, e.getMessage());
            return;
        }
        new HttpConnection().post(url, object, listener);
    }

    public static void startQuitGroup(String groupId, String imId, HttpConnection.CallbackListener listener) {
        String url = Config.SERVER_HOST + Config.URL_QUIT_GROUP.replace("{groupId}", groupId).replace("{imId}", imId);
        new HttpConnection().delete(url, listener);
    }

    public static void startRenameGroup(String groupId, String name, HttpConnection.CallbackListener listener) {
        String url = Config.SERVER_HOST + Config.URL_RENAME_GROUP.replace("{chatGroupId}", groupId);
        JSONObject obj = new JSONObject();
        try {
            obj.put("groupname", name);
        } catch (JSONException e) {
            Logger.e(TAG, e.getMessage());
            return;
        }
        new HttpConnection().put(url, obj, listener);
    }

    public static void startDismissGroup(String groupId, HttpConnection.CallbackListener listener) {
        String url = Config.SERVER_HOST + Config.URL_DISMISS_GROUP.replace("{groupId}", groupId);
        new HttpConnection().delete(url, listener);
    }

    public static void startGetGroupsByIds(List<String> ids, HttpConnection.CallbackListener listener) {
        String url = Config.SERVER_HOST + Config.URL_GET_GROUPS_BY_IDS;
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray(ids);
        try {
            object.put("groupIdList", array);
        } catch (JSONException e) {
            Logger.e(TAG, e.getMessage());
            return;
        }
        new HttpConnection().post(url, object, listener);
    }
}
