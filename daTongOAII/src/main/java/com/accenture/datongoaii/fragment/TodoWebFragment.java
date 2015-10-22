package com.accenture.datongoaii.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;

public class TodoWebFragment extends Fragment {
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layoutTodo = inflater.inflate(R.layout.frag_todo_web, container, false);
        webView = (WebView) layoutTodo.findViewById(R.id.wvTodo);

        //启用支持javascript
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        refreshTodoList(Account.getInstance().getUserId());

        return layoutTodo;
    }

    public void refreshTodoList(Integer userId) {
        String url = Config.URL_GET_TODO_URL;//.replace("{userId}", String.valueOf(userId));
        webView.loadUrl(url);
    }

}
