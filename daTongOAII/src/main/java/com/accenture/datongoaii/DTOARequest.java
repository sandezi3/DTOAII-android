package com.accenture.datongoaii;

import android.content.Context;

import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;

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
    private static DTOARequest instance;
    private static HttpConnection.CallbackListener defaultListener;
    private RequestListener mListener;
    private static Context mAppContext;
    private boolean mIsSilent;

    public interface RequestListener {
        void callback(String result);
    }

    private DTOARequest(Context appContext) {
        super();
        mAppContext = appContext;
        instance = this;
        mListener = null;
        mIsSilent = false;
        defaultListener = new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (result.equals("fail")) {
                    if (!mIsSilent) {
                        Utils.toast(mAppContext, Config.ERROR_NETWORK);
                    }
                    return;
                }
                try {
                    CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                    if (cr.statusCode == 0) {
                        mListener.callback(result);
                    } else {
                        if (!mIsSilent) {
                            Utils.toast(mAppContext, cr.statusMsg);
                        }
                    }
                } catch (JSONException e) {
                    Logger.e(TAG, e.getMessage());
                    if (!mIsSilent) {
                        Utils.toast(mAppContext, Config.ERROR_INTERFACE);
                    }
                }
            }
        };
    }

    public static DTOARequest getInstance(Context appContext) {
        instance = new DTOARequest(appContext);
        instance.mIsSilent = false;
        return instance;
    }

    public static DTOARequest getInstance(Context appContext, Boolean isSilent) {
        instance = new DTOARequest(appContext);
        instance.mIsSilent = isSilent;
        return instance;
    }

    /**
     * 待办
     */

    /**
     * 用户
     */
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
     * 好友
     */
    public static void startDeleteFriend(Integer userId, HttpConnection.CallbackListener listener) {
        String url = Config.SERVER_HOST + Config.URL_DELETE_FRIEND.replace("{userId}", Account.getInstance().getUserId().toString()).replace("{friendUserId}", userId.toString());
        new HttpConnection().delete(url, listener);
    }

    public void startInviteFriendConnect(String cell, RequestListener listener) {
        String url = Config.SERVER_HOST + Config.URL_INVITE_FRIEND.replace("{cell}", cell);
        mListener = listener;
        new HttpConnection().put(url, defaultListener);
    }

    /**
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
            if (array1.length() > 0) {
                object.put("members", array1);
            }
            if (array2.length() > 0) {
                object.put("cells", array2);
            }
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
            if (array1.length() > 0) {
                object.put("imIds", array1);
            }
            if (array2.length() > 0) {
                object.put("cells", array2);
            }
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

    public void startGetGroupsByIds(String ids, RequestListener listener) {
        String url = Config.SERVER_HOST + Config.URL_GET_GROUPS_BY_IDS.replace("{chatGroupIds}", ids);
        mListener = listener;
        new HttpConnection().get(url, defaultListener);
    }

    /**
     * 应用
     */
    public void startGetAppsByUserId(Integer userId, RequestListener listener) {
        String url = Config.SERVER_HOST + Config.URL_GET_APPS_BY_USER_ID.replace("{userId}", String.valueOf(userId));
        mListener = listener;
        new HttpConnection().get(url, defaultListener);
    }
}
