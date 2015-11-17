package com.accenture.datongoaii.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.DTOARequest;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.AppActivity;
import com.accenture.datongoaii.adapter.TodoListAdapter;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.App;
import com.accenture.datongoaii.model.Todo;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TodoFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "TodoFragment";
    private PullToRefreshListView lvTodo;
    private ImageView ivSearch;
    private TextView tvSearch;

    private TodoListAdapter adapter;
    private List<Todo> todoList;
    private List<Todo> tmpList;
    private Context context;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        View layoutTodo = inflater.inflate(R.layout.frag_todo, container, false);
        lvTodo = (PullToRefreshListView) layoutTodo.findViewById(R.id.lvTodo);
        EditText etSearch = (EditText) layoutTodo.findViewById(R.id.etSearch);
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

        lvTodo.setMode(Mode.PULL_FROM_START);
//        lvTodo.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多...");
//        lvTodo.getLoadingLayoutProxy(false, true).setRefreshingLabel(
//                "正在加载更多...");
//        lvTodo.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载更多...");

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
        lvTodo.setOnItemClickListener(this);
        refreshTodoList();
        return layoutTodo;
    }

    public void refreshTodoList() {
        DTOARequest.getInstance(context).requestTodoList(Account.getInstance().getUserId(), new DTOARequest.RequestListener() {
            @Override
            public void callback(String result) {
                try {
                    todoList.clear();
                    todoList.addAll(Todo.listFromJSON(new JSONObject(result)));
                    tmpList.clear();
                    tmpList.addAll(todoList);
                    adapter.notifyDataSetChanged();
                    lvTodo.onRefreshComplete();
                } catch (JSONException e) {
                    Utils.toast(context, Config.ERROR_INTERFACE);
                }
            }

            @Override
            public void callbackError() {
                Logger.e(TAG, "error");
                Utils.toast(context, Config.ERROR_APP);
                lvTodo.onRefreshComplete();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Todo todo = (Todo) adapter.getItem(position - 1);
        App app = new App();
        app.appName = todo.title;
        app.url = todo.url;
        Utils.startActivityForResult((Activity) context, AppActivity.class, Constants.BUNDLE_TAG_APP, app, Constants.REQUEST_CODE_SCAN_QR_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            refreshTodoList();
        }
    }

    private void loadMoreTodoList() {
        DTOARequest.getInstance(context).requestTodoList(Account.getInstance().getUserId(), new DTOARequest.RequestListener() {
            @Override
            public void callback(String result) {
                try {
                    todoList.addAll(Todo.listFromJSON(new JSONObject(result)));
                    tmpList.clear();
                    tmpList.addAll(todoList);
                    adapter.notifyDataSetChanged();
                    lvTodo.onRefreshComplete();
                } catch (JSONException e) {
                    Utils.toast(context, Config.ERROR_INTERFACE);
                }
            }

            @Override
            public void callbackError() {
            }
        });
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
