package com.accenture.datongoaii.model;

import com.accenture.datongoaii.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 10/21/15.
 * 应用类
 */
public class App implements Serializable {
    public static final String TAG = "Model App";
    public Integer appId;
    public String appName;
    public String desc;
    public String logo;
    public String url;
    public Integer createUserId;
    public String createTime;

    public static App fromJSON(JSONObject json) {
        App app = new App();
        try {
            app.appId = json.getInt("appId");
            app.appName = json.getString("appName");
            app.desc = json.getString("desc");
            app.logo = json.getString("logo");
            app.url = json.getString("url");
            app.createUserId = json.getInt("createUserId");
            app.createTime = "";
        } catch (JSONException e) {
            Logger.e(TAG, "fromJSON " + e.getMessage());
            app = null;
        }
        return app;
    }

    public static List<App> listFromJSON(JSONObject json) {
        List<App> list = new ArrayList<App>();
        try {
            JSONArray array = json.getJSONArray("apps");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                App app = fromJSON(obj);
                list.add(app);
            }
        } catch (JSONException e) {
            Logger.e(TAG, "fromJSON " + e.getMessage());
            list = null;
        }
        return list;
    }
}
