package com.accenture.datongoaii.model;

import com.accenture.datongoaii.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 10/27/15.
 * App 分类
 */
public class AppCategory {
    private static final String TAG = "model.AppCategory";
    public String type;
    public List<App> apps;
    public List<Group> groups;

    public static List<AppCategory> listFromJSON(JSONObject json) {
        try {
            JSONArray array = json.getJSONArray("data");
            List<AppCategory> list = new ArrayList<AppCategory>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject j = array.getJSONObject(i);
                AppCategory category = new AppCategory();
                category.type = j.getString("type");
                if (j.has("apps")) {
                    category.apps = getAppsFromJSON(j.getJSONArray("apps"));
                } else if (j.has("groups")) {
                    category.groups = getGroupsFromJSON(j.getJSONArray("groups"));
                }
                list.add(category);
            }
            return list;
        } catch (JSONException e) {
            Logger.e(TAG, "listFromJSON " + e.getMessage());
        }
        return null;
    }

    private static List<App> getAppsFromJSON(JSONArray array) {
        List<App> list = new ArrayList<App>();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                App app = App.fromJSON(obj);
                list.add(app);
            }
        } catch (JSONException e) {
            Logger.e(TAG, "getAppsFromJSON " + e.getMessage());
            list = null;
        }
        return list;
    }

    private static List<Group> getGroupsFromJSON(JSONArray array) {
        List<Group> list = new ArrayList<Group>();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Group group = new Group();
                group.name = obj.getString("groupName");
                group.apps = getAppsFromJSON(obj.getJSONArray("apps"));
                list.add(group);
            }
        } catch (JSONException e) {
            Logger.e(TAG, "getAppsFromJSON " + e.getMessage());
            list = null;
        }
        return list;
    }
}
