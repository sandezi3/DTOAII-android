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
import com.accenture.datongoaii.adapter.TodoListAdapter;
import com.accenture.datongoaii.model.Todo;
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

public class TodoFragment extends Fragment {
    private View layoutTodo;
    private PullToRefreshListView lvTodo;
    private EditText etSearch;
    private ImageView ivSearch;
    private TextView tvSearch;

    private TodoListAdapter adapter;
    private List<Todo> todoList;
    private List<Todo> tmpList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layoutTodo = inflater.inflate(R.layout.frag_todo, container, false);
        lvTodo = (PullToRefreshListView) layoutTodo.findViewById(R.id.lvTodo);
        etSearch = (EditText) layoutTodo.findViewById(R.id.etSearch);
        ivSearch = (ImageView) layoutTodo.findViewById(R.id.ivSearch);
        tvSearch = (TextView) layoutTodo.findViewById(R.id.tvSearch);

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
                    todoList.clear();
                    todoList.addAll(getFiltedList(tmpList, s.toString()));
                    adapter.notifyDataSetChanged();
                } else {
                    ivSearch.setVisibility(View.VISIBLE);
                    tvSearch.setVisibility(View.VISIBLE);
                    todoList.clear();
                    todoList.addAll(tmpList);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        lvTodo.setMode(Mode.BOTH);
        lvTodo.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多...");
        lvTodo.getLoadingLayoutProxy(false, true).setRefreshingLabel(
                "正在加载更多...");
        lvTodo.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载更多...");

        todoList = new ArrayList<Todo>();
        tmpList = new ArrayList<Todo>();
        adapter = new TodoListAdapter(getActivity(), todoList);
        lvTodo.setAdapter(adapter);
        lvTodo.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(
                        TodoFragment.this.getActivity(),
                        System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);
                // Update the LastUpdatedLabel
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                if (lvTodo.isHeaderShown()) {
                    refreshTodoList();
                } else if (lvTodo.isFooterShown()) {
                    loadMoreTodoList();
                }
            }
        });
        refreshTodoList();
        return layoutTodo;
    }

    public void refreshTodoList() {
        String url = Config.SERVER_HOST + "todo.json";
        Logger.i("TodoFragment.refreshTodoList", "URL = " + url);
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (result != "fail") {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            todoList.clear();
                            todoList.addAll(Intepreter
                                    .getTodoListFromJson(result));
                            tmpList.clear();
                            tmpList.addAll(todoList);
                            adapter.notifyDataSetChanged();
                            lvTodo.onRefreshComplete();
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

    private void loadMoreTodoList() {
        String url = Config.SERVER_HOST + "todo.json";
        Logger.i("TodoFragment.loadMoreTodoList", "URL = " + url);
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (result != "fail") {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            todoList.addAll(Intepreter
                                    .getTodoListFromJson(result));
                            tmpList.clear();
                            tmpList.addAll(todoList);
                            adapter.notifyDataSetChanged();
                            lvTodo.onRefreshComplete();
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

    private List<Todo> getFiltedList(List<Todo> list,
                                     String s) {
        List<Todo> tmpList = new ArrayList<Todo>();
        for (Todo t : list) {
            if (t.title.contains(s)) {
                tmpList.add(t);
            }
        }
        return tmpList;
    }

}
