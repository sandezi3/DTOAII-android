package com.accenture.datongoaii.vendor.HX.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.db.ContactDao;
import com.accenture.datongoaii.db.GroupDao;

/**
 * Created by leon on 10/20/15.
 * 透传命令Receiver
 */
public class NewCMDMessageBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra(Constants.BROADCAST_CMD);
        if (action.equals(Constants.BROADCAST_CMD_DELETE_FRIEND) || action.equals(Constants.BROADCAST_CMD_ADD_FRIEND) || action.equals(Constants.BROADCAST_CMD_ACCEPT_FRIEND)) {
            // 清数据库
            new ContactDao(context).saveFriends(null);
        }
        if (action.equals(Constants.BROADCAST_CMD_GROUP_RENAME) ||
                action.equals(Constants.BROADCAST_CMD_GROUP_ADD_MEMBER) ||
                action.equals(Constants.BROADCAST_CMD_GROUP_MEMBER_REMOVED) ||
                action.equals(Constants.BROADCAST_CMD_GROUP_DISMISS)) {
            // 清数据库
            new GroupDao(context).saveMyGroups(null);
        }
    }
}
