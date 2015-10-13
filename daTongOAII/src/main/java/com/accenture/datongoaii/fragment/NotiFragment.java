package com.accenture.datongoaii.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.accenture.datongoaii.DTOARequest;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.adapter.ConversationListAdapter;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Conversation;
import com.accenture.datongoaii.model.Group;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Utils;
import com.accenture.datongoaii.vendor.HX.ChatActivity;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VoiceMessageBody;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

public class NotiFragment extends Fragment implements AdapterView.OnItemClickListener {
    private Context context;
    //    private PullToRefreshListView lvNoti;
//    private EditText etSearch;
//    private ImageView ivSearch;
//    private TextView tvSearch;

    //    private NotiListAdapter adapter;
//    private List<Noti> notiList;
//    private List<Noti> tmpList;
    private List<Conversation> conversationList;
    private ConversationListAdapter adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = this.getActivity();
        View layoutNoti = inflater.inflate(R.layout.frag_noti, container, false);
//        lvNoti = (PullToRefreshListView) layoutNoti.findViewById(R.id.lvNoti);
        ListView lvConversation = (ListView) layoutNoti.findViewById(R.id.lvConversation);
//        etSearch = (EditText) layoutNoti.findViewById(R.id.etSearch);
//        ivSearch = (ImageView) layoutNoti.findViewById(R.id.ivSearch);
//        tvSearch = (TextView) layoutNoti.findViewById(R.id.tvSearch);

//        etSearch.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count,
//                                          int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before,
//                                      int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                Logger.i("afterTextChanged", s.toString());
//                if (s.toString().length() > 0) {
//                    ivSearch.setVisibility(View.GONE);
//                    tvSearch.setVisibility(View.GONE);
//                    notiList.clear();
//                    notiList.addAll(getFiltedList(tmpList, s.toString()));
//                    adapter.notifyDataSetChanged();
//                } else {
//                    ivSearch.setVisibility(View.VISIBLE);
//                    tvSearch.setVisibility(View.VISIBLE);
//                    notiList.clear();
//                    notiList.addAll(tmpList);
//                    adapter.notifyDataSetChanged();
//                }
//            }
//        });

//        lvNoti.setMode(Mode.BOTH);
//        lvNoti.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多...");
//        lvNoti.getLoadingLayoutProxy(false, true).setRefreshingLabel(
//                "正在加载更多...");
//        lvNoti.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载更多...");
//
////        notiList = new ArrayList<Noti>();
//        tmpList = new ArrayList<Noti>();
//        adapter = new NotiListAdapter(getActivity(), notiList);
//        lvNoti.setAdapter(adapter);
//        lvNoti.setOnRefreshListener(new OnRefreshListener<ListView>() {
//            @Override
//            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
//                String label = DateUtils.formatDateTime(
//                        NotiFragment.this.getActivity(),
//                        System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
//                                | DateUtils.FORMAT_SHOW_DATE
//                                | DateUtils.FORMAT_ABBREV_ALL);
//                // Update the LastUpdatedLabel
//                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
//
//                if (lvNoti.isHeaderShown()) {
//                    refreshNotiList();
//                } else if (lvNoti.isFooterShown()) {
//                    loadMoreNotiList();
//                }
//            }
//        });
//        refreshNotiList();
        conversationList = new ArrayList<Conversation>();
        adapter = new ConversationListAdapter(context, conversationList);
        lvConversation.setAdapter(adapter);
        lvConversation.setOnItemClickListener(this);
        syncConversationList();
        return layoutNoti;
    }

    public void refresh() {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void syncConversationList() {
        conversationList.clear();
        conversationList.addAll(loadConversationsWithRecentChat());
        // 判断会话是否有更新
        if (isConversationUpdated(conversationList)) {
            DTOARequest.startGetUsersByImIds(getImIds(conversationList), new HttpConnection.CallbackListener() {
                @Override
                public void callBack(String result) {
                    if (!result.equals("result")) {
                        try {
                            CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                            if (cr.statusCode == 0) {
                                JSONArray array = new JSONObject(result).getJSONArray("data");
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject obj = array.getJSONObject(i);
                                    Contact c = Contact.fromJSON(obj);
                                    if (c != null) {
                                        Conversation conversation = Conversation.getItemByImId(conversationList, c.imId);
                                        if (conversation != null) {
                                            conversation.head = c.head;
                                            conversation.username = c.name;
                                        }
                                    }
                                }
                                refresh();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            DTOARequest.startGetGroupsByIds(getGroupIds(conversationList), new HttpConnection.CallbackListener() {
                @Override
                public void callBack(String result) {
                    if (!result.equals("result")) {
                        try {
                            CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                            if (cr.statusCode == 0) {
                                JSONArray array = new JSONObject(result).getJSONArray("groupList");
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject obj = array.getJSONObject(i);
                                    Group group = Group.fromJSON(obj);
                                    if (group != null) {
                                        Conversation conversation = Conversation.getItemByImId(conversationList, group.imId);
                                        if (conversation != null) {
                                            conversation.head = group.img;
                                            conversation.username = group.name;
                                        }
                                    }
                                }
                                refresh();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private boolean isConversationUpdated(List<Conversation> list) {
        for (Conversation conversation : list) {
            if (conversation.username == null) {
                return true;
            }
        }
        return false;
    }

    private List<String> getImIds(List<Conversation> list) {
        List<String> imIds = new ArrayList<String>();
        for (Conversation conversation : list) {
            if (!conversation.isGroup) {
                imIds.add(conversation.imId);
            }
        }
        return imIds;
    }

    private List<String> getGroupIds(List<Conversation> list) {
        List<String> ids = new ArrayList<String>();
        for (Conversation conversation : list) {
            if (conversation.isGroup) {
                ids.add(conversation.imId);
            }
        }
        return ids;
    }
//    public void refreshNotiList() {
//        EMChatManager.getInstance().getAllConversations();
//        String url = Config.SERVER_HOST + "noti.json";
//        Logger.i("NotiFragment.refreshNotiList", "URL = " + url);
//        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
//            @Override
//            public void callBack(String result) {
//                if (result != "fail") {
//                    try {
//                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
//                            notiList.clear();
//                            notiList.addAll(Intepreter
//                                    .getNotiListFromJson(result));
//                            tmpList.clear();
//                            tmpList.addAll(notiList);
//                            adapter.notifyDataSetChanged();
//                            lvNoti.onRefreshComplete();
//                        } else {
//                            show(Intepreter.getCommonStatusFromJson(result).statusMsg);
//                        }
//                    } catch (JSONException e) {
//                        show(Config.ERROR_INTERFACE);
//                    }
//                } else {
//                    show(Config.ERROR_NETWORK);
//                }
//            }
//        });
//    }

//    private void loadMoreNotiList() {
//        String url = Config.SERVER_HOST + "noti.json";
//        Logger.i("NotiFragment.loadMoreNotiList", "URL = " + url);
//        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
//            @Override
//            public void callBack(String result) {
//                if (result != "fail") {
//                    try {
//                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
//                            notiList.addAll(Intepreter
//                                    .getNotiListFromJson(result));
//                            tmpList.clear();
//                            tmpList.addAll(notiList);
//                            adapter.notifyDataSetChanged();
//                            lvNoti.onRefreshComplete();
//                        } else {
//                            show(Intepreter.getCommonStatusFromJson(result).statusMsg);
//                        }
//                    } catch (JSONException e) {
//                        show(Config.ERROR_INTERFACE);
//                    }
//                } else {
//                    show(Config.ERROR_NETWORK);
//                }
//            }
//        });
//    }

    //    private void show(String msg) {
//        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
//    }
//
//    private List<Noti> getFiltedList(List<Noti> list,
//                                     String s) {
//        List<Noti> tmpList = new ArrayList<Noti>();
//        for (Noti t : list) {
//            if (t.title.contains(s)) {
//                tmpList.add(t);
//            }
//        }
//        return tmpList;
//    }
    private List<Conversation> loadConversationsWithRecentChat() {
        // 获取所有会话，包括陌生人
        Hashtable<String, EMConversation> conversations = EMChatManager.getInstance().getAllConversations();
        // 过滤掉messages size为0的conversation
        /**
         * 如果在排序过程中有新消息收到，lastMsgTime会发生变化
         * 影响排序过程，Collection.sort会产生异常
         * 保证Conversation在Sort过程中最后一条消息的时间不变
         * 避免并发问题
         */
        List<Pair<Long, EMConversation>> sortList = new ArrayList<Pair<Long, EMConversation>>();
        synchronized (conversations) {
            for (EMConversation conversation : conversations.values()) {
                if (conversation.getAllMessages().size() != 0) {
                    //if(conversation.getType() != EMConversationType.ChatRoom){
                    sortList.add(new Pair<Long, EMConversation>(conversation.getLastMessage().getMsgTime(), conversation));
                    //}
                }
            }
        }
        try {
            // Internal is TimSort algorithm, has bug
            sortConversationByLastChatTime(sortList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Conversation> list = new ArrayList<Conversation>();
        for (Pair<Long, EMConversation> sortItem : sortList) {
            Conversation conversation = new Conversation();
            EMConversation emConversation = sortItem.second;
            conversation.username = null;
            conversation.isGroup = emConversation.getIsGroup();
            conversation.imId = emConversation.getUserName();
            if (emConversation.getLastMessage().getBody() instanceof TextMessageBody) {
                conversation.summary = ((TextMessageBody) emConversation.getLastMessage().getBody()).getMessage();
            } else if (emConversation.getLastMessage().getBody() instanceof ImageMessageBody) {
                conversation.summary = "[图片]";
            } else if (emConversation.getLastMessage().getBody() instanceof VoiceMessageBody) {
                conversation.summary = "[语音]";
            }
            try {
                conversation.create = Utils.getPeroid(emConversation.getLastMessage().getMsgTime(), System.currentTimeMillis());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            conversation.unReadedCount = emConversation.getUnreadMsgCount() + "";
            list.add(conversation);
        }
        return list;
    }

    /**
     * 根据最后一条消息的时间排序
     *
     * @param conversationList
     * 会话列表
     */
    private void sortConversationByLastChatTime(List<Pair<Long, EMConversation>> conversationList) {
        Collections.sort(conversationList, new Comparator<Pair<Long, EMConversation>>() {
            @Override
            public int compare(final Pair<Long, EMConversation> con1, final Pair<Long, EMConversation> con2) {
                if (con1.first.equals(con2.first)) {
                    return 0;
                } else if (con2.first > con1.first) {
                    return 1;
                } else {
                    return -1;
                }
            }

        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Conversation c = conversationList.get(position);
        Intent intent = new Intent(view.getContext(), ChatActivity.class);
        if (c.isGroup) {
            intent.putExtra("groupId", c.imId);
            intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
        } else {
            intent.putExtra("userId", c.imId);
            intent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
        }
        startActivity(intent);
    }
}
