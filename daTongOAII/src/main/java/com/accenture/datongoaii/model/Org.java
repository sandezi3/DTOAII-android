package com.accenture.datongoaii.model;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Org extends FirstPinYin implements Serializable {
    public static final String TAG = "Model.Org";
    public Integer orgId;
    public String orgName;
    public String logo;
    public Integer createUserId;
    public Integer createTime;

    public static List<Org> listFromJSON(JSONObject json) {
        try {
            List<Org> list = new ArrayList<Org>();
            JSONArray array = json.getJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                JSONObject j = array.getJSONObject(i);
                if (j.getInt("groupTypeId") == Config.GROUP_TYPE_COMPANY) {
                    Org o = fromJSON(j);
                    if (o != null) {
                        list.add(o);
                    }
                }
            }
            return list;
        } catch (Exception e) {
            if (Logger.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Org fromJSON(JSONObject json) {
        try {
            if (json.getInt("groupTypeId") == Config.GROUP_TYPE_COMPANY) {
                Org o = new Org();
                o.orgId = json.getInt("groupId");
                o.orgName = json.getString("groupName");
                o.logo = json.getString("logo");
                o.mFirstPinYin = "#";
                o.createUserId = json.getInt("createUserId");
                o.createTime = json.getInt("createTime");
                return o;
            }
        } catch (JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return null;
    }
}
