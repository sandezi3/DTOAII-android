package com.accenture.datongoaii.model;

import com.accenture.datongoaii.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Account {
    private Integer userId = null;
    private String username = null;
    private String head = null;
    private String token = null;
    private String sex = null;
    private String birth = null;
    private String cell = null;


    private String imId = null;

    private List<Org> orgList = null;

    private Account() {
        setUserId(-1);
        setUsername("");
        setHead("");
        setToken("");
        setSex("");
        setBirth("");
        setCell("");
        setImId("");
    }

    // Initialization on Demand Holder
    private static class HolderClass {
        private final static Account instance = new Account();
    }

    public static Account getInstance() {
        return HolderClass.instance;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public List<Org> getOrgList() {
        return orgList;
    }

    public void setOrgList(List<Org> orgList) {
        this.orgList = orgList;
    }

    public String getImId() {
        return imId;
    }

    public void setImId(String imId) {
        this.imId = imId;
    }

    public void fromJson(JSONObject json) {
        try {
            json = json.getJSONObject("data");
            this.setUserId(json.getInt("userId"));
            this.setUsername(json.getString("username"));
            this.setToken(json.getString("token"));
            this.setHead(json.getString("photo"));
            this.setSex(json.getString("sex"));
            this.setBirth(json.getString("birth"));
            this.setCell(json.getString("cell"));
            if (json.has("imId") && json.getString("imId") != null) {
                this.setImId(json.getString("imId"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Org getCreatedOrg() {
        if (orgList == null || orgList.size() == 0) {
            return null;
        }
        for (Org org : orgList) {
            if (userId.equals(org.createUserId)) {
                return org;
            }
        }
        return null;
    }
}
