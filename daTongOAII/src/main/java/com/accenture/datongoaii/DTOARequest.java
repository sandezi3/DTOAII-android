package com.accenture.datongoaii;

import android.content.Context;

import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;
import com.accenture.datongoaii.vendor.HX.HXController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by leon on 9/29/15.
 * 网络请求
 */
public class DTOARequest {
    public static final String TAG = "DTOARequest";
    private static DTOARequest instance;
    private static HttpConnection.CallbackListener defaultListener;
    private WeakReference<RequestListener> mListener;
    private RequestListener mStrongListener;
    private static Context mAppContext;
    private boolean mIsSilent;

    public interface RequestListener {
        void callback(String result);

        void callbackError();
    }

    private DTOARequest(Context appContext) {
        super();
        mAppContext = appContext;
        instance = this;
        mListener = null;
        mStrongListener = null;
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
                        if (mListener != null && mListener.get() != null) {
                            mListener.get().callback(result);
                            return;
                        } else if (mStrongListener != null) {
                            mStrongListener.callback(result);
                            mStrongListener = null;
                            return;
                        }
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
                if (mListener != null && mListener.get() != null) {
                    mListener.get().callbackError();
                } else if (mStrongListener != null) {
                    mStrongListener.callbackError();
                    mStrongListener = null;
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
     * Account
     */
    public void requestUploadImage(String path, RequestListener listener) {
//        String url = Config.SERVER_HOST + Config.URL_UPLOAD_HEAD;
        String url = Config.SERVER_HOST.replace("/api", "") + Config.URL_UPLOAD_HEAD1;
        mStrongListener = listener;
//        new HttpConnection().requestUploadImage(url, path, defaultListener);
        new HttpConnection().uploadFile(url, path, defaultListener);
    }

    public void requestModifyHead(Integer userId, String url, RequestListener listener) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("photo", url);
        modifyUserInfo(userId, object, listener);
    }

    public void requestModifyUsername(Integer userId, String newName, RequestListener listener) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("username", newName);
        modifyUserInfo(userId, object, listener);
    }

    public void requestModifyUserSex(Integer userId, String sex, RequestListener listener) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("sex", sex);
        modifyUserInfo(userId, object, listener);
    }

    public void requestModifyUserBirthday(Integer userId, String birthday, RequestListener listener) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("birth", birthday);
        modifyUserInfo(userId, object, listener);
    }

    private void modifyUserInfo(Integer userId, JSONObject object, RequestListener listener) {
        String url = Config.SERVER_HOST + Config.URL_MODIFY_USER_INFO.replace("{userId}", String.valueOf(userId));
        mListener = new WeakReference<RequestListener>(listener);
        new HttpConnection().put(url, object, defaultListener);
    }

    public void requestLogout() {
        String url = Config.SERVER_HOST + Config.URL_LOGOUT;
        new HttpConnection().get(url, null);
        try {
            HXController.getInstance().startLogout();
        } catch (Exception e) {
            // do nothing
        }
        ((DTOAIIApplication) mAppContext).restartApplication();
    }

    /**
     * 用户
     */
    public static void requestGetUsersByImIds(String[] ids, HttpConnection.CallbackListener listener) {
        String url = Config.SERVER_HOST + Config.URL_GET_USERS_BY_IMIDS.replace("{imId}", Utils.combineStrings(ids, ","));
        new HttpConnection().get(url, listener);
    }

    public static void requestGetUsersByImIds(List<String> ids, HttpConnection.CallbackListener listener) {
        String url = Config.SERVER_HOST + Config.URL_GET_USERS_BY_IMIDS.replace("{imId}", Utils.combineStrings(ids, ","));
        new HttpConnection().get(url, listener);
    }

    public void requestGetContactsStatusConnect(String[] cells, RequestListener listener) {
        mListener = new WeakReference<RequestListener>(listener);
        String url = Config.SERVER_HOST + Config.URL_GET_USER_STATUS;
        JSONObject obj = new JSONObject();
        try {
            JSONArray array = new JSONArray();
            for (String cell : cells) {
                array.put(cell);
            }
            obj.put("cells", array);
        } catch (JSONException e) {
            return;
        }
        new HttpConnection().post(url, obj, defaultListener);
    }

    public void startGetUsersByIds(Integer[] ids, RequestListener listener) {
        mListener = new WeakReference<RequestListener>(listener);
        String url = Config.SERVER_HOST + Config.URL_GET_USERS_BY_IDS.replace("{userId}", Utils.combineStrings(ids, ","));
        new HttpConnection().get(url, defaultListener);
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
        mListener = new WeakReference<RequestListener>(listener);
        new HttpConnection().put(url, defaultListener);
    }

    public void startAddFriendsConnect(Integer userId, Contact.FriendStatus status, RequestListener listener) {
        String op = null;
        switch (status) {
            case FRIENDS_STATUS_TO_BE_FRIEND:
                op = "add";
                break;
            case FRIENDS_STATUS_TO_ME_NOT_ACCEPT:
                op = "accept";
                break;
        }
        String url = Config.SERVER_HOST + Config.URL_ADD_FRIEND;
        JSONObject object = new JSONObject();
        try {
            object.put("toUserId", userId);
            object.put("op", op);
        } catch (JSONException e) {
            Utils.toast(mAppContext, Config.ERROR_APP);
        }
        mListener = new WeakReference<RequestListener>(listener);
        new HttpConnection().post(url, object, defaultListener);
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
        mListener = new WeakReference<RequestListener>(listener);
        new HttpConnection().get(url, defaultListener);
    }

    /**
     * 应用
     */
    public void startGetAppsByUserId(Integer userId, RequestListener listener) {
        String url = Config.SERVER_HOST + Config.URL_GET_APPS_BY_USER_ID.replace("{userId}", String.valueOf(userId));
        mListener = new WeakReference<RequestListener>(listener);
        new HttpConnection().get(url, defaultListener);
    }

    /**
     * 待办
     */
    public void requestTodoList(Integer userId, RequestListener listener) {
        String url = Config.SERVER_HOST + Config.URL_TODO_LIST.replace("{roleId}", userId.toString());
        mStrongListener = listener;
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (result.equals("fail")) {
                    if (!mIsSilent) {
                        Utils.toast(mAppContext, Config.ERROR_NETWORK);
                        mStrongListener.callbackError();
                        mStrongListener = null;
                    }
                    return;
                }
                if (result.equals(HttpConnection.ERROR_INTERFACE)) {
                    if (!mIsSilent) {
                        Utils.toast(mAppContext, Config.ERROR_INTERFACE);
                        mStrongListener.callbackError();
                        mStrongListener = null;
                    }
                    return;
                }
                try {
                    JSONObject object = new JSONObject(result);
                    String status = object.getString("result");
                    if (!status.equals("success")) {
                        if (!mIsSilent) {
                            Utils.toast(mAppContext, status);
                            mStrongListener.callbackError();
                            mStrongListener = null;
                        }
                        return;
                    }
                    mStrongListener.callback(result);
                } catch (JSONException e) {
                    Logger.e(TAG, e.getMessage());
                    if (!mIsSilent) {
                        Utils.toast(mAppContext, Config.ERROR_INTERFACE);
                        mStrongListener.callbackError();
                        mStrongListener = null;
                    }
                }
            }
        });
    }
}
