package com.accenture.datongoaii.model;

import com.accenture.datongoaii.util.Config;
import com.accenture.datongoaii.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;

public class Org extends FirstPinYin implements Serializable {
    public Integer orgId;
    public String orgName;
    public String logo;
    public Integer createUserId;
    public Integer createTime;

    public static Org fromJSON(JSONObject json) {
        try {
            JSONArray array = json.getJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                json = array.getJSONObject(i);
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
            }
        } catch (Exception e) {
            if (Logger.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
