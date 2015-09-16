package com.accenture.datongoaii.model;

import com.accenture.datongoaii.util.Logger;

import org.json.JSONObject;

import java.io.Serializable;

public class Org extends FirstPinYin implements Serializable {
    public Integer orgId;
    public String orgName;
    public String logo;
    public Integer createUserId;
    public String createTime;

    public static Org fromJSON(JSONObject json) {
        try {
            json = json.getJSONObject("data");
            Org o = new Org();
            o.orgId = json.getInt("orgId");
            o.orgName = json.getString("orgName");
            o.logo = json.getString("logo");
            o.mFirstPinYin = "#";
            o.createUserId = json.getInt("createUserId");
            o.createTime = json.getString("createTime");
            return o;
        } catch (Exception e) {
            if (Logger.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
