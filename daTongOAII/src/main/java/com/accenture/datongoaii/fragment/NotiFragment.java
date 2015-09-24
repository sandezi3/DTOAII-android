package com.accenture.datongoaii.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.adapter.NotiListAdapter;
import com.accenture.datongoaii.model.Noti;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.common.Config;
import com.accenture.datongoaii.common.Intepreter;
import com.accenture.datongoaii.util.Logger;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class NotiFragment extends Fragment {
    private View layoutNoti;
    private PullToRefreshListView lvNoti;
    private EditText etSearch;
    private ImageView ivSearch;
    private TextView tvSearch;

    private NotiListAdapter adapter;
    private List<Noti> notiList;
    private List<Noti> tmpList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layoutNoti = inflater.inflate(R.layout.frag_noti, container, false);
        lvNoti = (PullToRefreshListView) layoutNoti.findViewById(R.id.lvNoti);
        etSearch = (EditText) layoutNoti.findViewById(R.id.etSearch);
        ivSearch = (ImageView) layoutNoti.findViewById(R.id.ivSearch);
        tvSearch = (TextView) layoutNoti.findViewById(R.id.tvSearch);

        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Logger.i("afterTextChanged", s.toString());
                if (s.toString().length() > 0) {
                    ivSearch.setVisibility(View.GONE);
                    tvSearch.setVisibility(View.GONE);
                    notiList.clear();
                    notiList.addAll(getFiltedList(tmpList, s.toString()));
                    adapter.notifyDataSetChanged();
                } else {
                    ivSearch.setVisibility(View.VISIBLE);
                    tvSearch.setVisibility(View.VISIBLE);
                    notiList.clear();
                    notiList.addAll(tmpList);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        lvNoti.setMode(Mode.BOTH);
        lvNoti.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多...");
        lvNoti.getLoadingLayoutProxy(false, true).setRefreshingLabel(
                "正在加载更多...");
        lvNoti.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载更多...");

        notiList = new ArrayList<Noti>();
        tmpList = new ArrayList<Noti>();
        adapter = new NotiListAdapter(getActivity(), notiList);
        lvNoti.setAdapter(adapter);
        lvNoti.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(
                        NotiFragment.this.getActivity(),
                        System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);
                // Update the LastUpdatedLabel
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                if (lvNoti.isHeaderShown()) {
                    refreshNotiList();
                } else if (lvNoti.isFooterShown()) {
                    loadMoreNotiList();
                }
            }
        });
        refreshNotiList();
        return layoutNoti;
    }

    public void refreshNotiList() {
        String url = Config.SERVER_HOST + "noti.json";
        Logger.i("NotiFragment.refreshNotiList", "URL = " + url);
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (result != "fail") {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            notiList.clear();
                            notiList.addAll(Intepreter
                                    .getNotiListFromJson(result));
                            tmpList.clear();
                            tmpList.addAll(notiList);
                            adapter.notifyDataSetChanged();
                            lvNoti.onRefreshComplete();
                        } else {
                            show(Intepreter.getCommonStatusFromJson(result).statusMsg);
                        }
                    } catch (JSONException e) {
                        show(Config.ERROR_INTERFACE);
                    }
                } else {
                    show(Config.ERROR_NETWORK);
                }
            }
        });
    }

    private void loadMoreNotiList() {
        String url = Config.SERVER_HOST + "noti.json";
        Logger.i("NotiFragment.loadMoreNotiList", "URL = " + url);
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (result != "fail") {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            notiList.addAll(Intepreter
                                    .getNotiListFromJson(result));
                            tmpList.clear();
                            tmpList.addAll(notiList);
                            adapter.notifyDataSetChanged();
                            lvNoti.onRefreshComplete();
                        } else {
                            show(Intepreter.getCommonStatusFromJson(result).statusMsg);
                        }
                    } catch (JSONException e) {
                        show(Config.ERROR_INTERFACE);
                    }
                } else {
                    show(Config.ERROR_NETWORK);
                }
            }
        });
    }

    private void show(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private List<Noti> getFiltedList(List<Noti> list,
                                     String s) {
        List<Noti> tmpList = new ArrayList<Noti>();
        for (Noti t : list) {
            if (t.title.contains(s)) {
                tmpList.add(t);
            }
        }
        return tmpList;
    }

}
