package com.accenture.datongoaii.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Account {
	private String userId = null;
	private String username = null;
	private String head = null;
	private String token = null;
	private String sex = null;
	private String birth = null;

	private Account() {
		setUserId("");
		setUsername("");
		setHead("");
		setToken("");
		setSex("");
		setBirth("");
	}

	// Initialization on Demand Holder
	private static class HolderClass {
		private final static Account instance = new Account();
	}

	public static Account getInstance() {
		return HolderClass.instance;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
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

	public void fromJson(JSONObject json) {
		try {
			this.setUserId(json.getString("userId"));
			this.setUsername(json.getString("username"));
			this.setToken(json.getString("token"));
			this.setHead(json.getString("head"));
			this.setSex(json.getString("sex"));
			this.setBirth(json.getString("birth"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
