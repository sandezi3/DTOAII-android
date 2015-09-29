package com.accenture.datongoaii;

import com.accenture.datongoaii.network.HttpConnection;

/**
 * Created by leon on 9/29/15.
 * 网络请求
 */
public class DTOARequest {
    public static void startGetUserByImId(String imId, HttpConnection.CallbackListener listener) {
        String url = Config.SERVER_HOST + Config.URL_GET_USER_BY_IMID.replace("{imId}", imId);
        new HttpConnection().get(url, listener);
    }
}
