package com.accenture.datongoaii.model;

import java.io.Serializable;

import android.graphics.drawable.Drawable;

public class Noti implements Serializable {
	private static final long serialVersionUID = -4269456304688379751L;
	public int _id;
	public String nid;
	public String title;
	public String create;
	public String deadline;
	public String from;
	public String img;
	public Drawable image;
}
