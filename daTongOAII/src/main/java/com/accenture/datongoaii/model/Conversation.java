package com.accenture.datongoaii.model;

import com.easemob.chat.EMConversation;

import java.io.Serializable;

/**
 * Created by leon on 10/3/15.
 * 会话model
 */
public class Conversation implements Serializable {
    public String head;
    public String username;
    public String imId;
    public String summary;
    public String create;
    public String unReadedCount;
}
