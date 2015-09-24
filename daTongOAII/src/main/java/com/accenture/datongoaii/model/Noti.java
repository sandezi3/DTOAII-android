package com.accenture.datongoaii.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

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
