package com.accenture.datongoaii.model;

import java.io.Serializable;
import java.util.List;

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
    public boolean isGroup;

    private static Conversation getItemByImId(List<Conversation> list, String imId) {
        for (Conversation conversation : list) {
            if (conversation.imId.equals(imId)) {
                return conversation;
            }
        }
        return null;
    }

    public static void updateContactInList(List<Conversation> list, Contact contact) {
        Conversation conversation = Conversation.getItemByImId(list, contact.imId);
        if (conversation != null) {
            conversation.head = contact.head;
            conversation.username = contact.name;
        }
    }

    public static void updateGroupInList(List<Conversation> list, Group group) {
        Conversation conversation = Conversation.getItemByImId(list, group.imId);
        if (conversation != null) {
            conversation.head = group.img;
            conversation.username = group.name;
        }
    }
}
