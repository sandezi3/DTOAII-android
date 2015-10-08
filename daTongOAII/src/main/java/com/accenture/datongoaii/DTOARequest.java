package com.accenture.datongoaii;

import com.accenture.datongoaii.model.Conversation;
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
}
