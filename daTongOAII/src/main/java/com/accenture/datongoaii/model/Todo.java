package com.accenture.datongoaii.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Todo implements Serializable {
    private static final long serialVersionUID = -5411409149570392820L;
    public int _id;
    public String tid;
    public String title;
    public String create;
    public String deadline;
    public String from;
    public String img;
    public Drawable image;
}
