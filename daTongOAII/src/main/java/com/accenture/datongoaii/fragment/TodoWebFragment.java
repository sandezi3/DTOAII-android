package com.accenture.datongoaii.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.network.HttpConnection;

import java.util.HashMap;
import java.util.Map;

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
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSaveFormData(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getActivity().getApplicationContext().getCacheDir().getAbsolutePath();
        settings.setAppCachePath(appCachePath);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient());

        refreshTodoList(Account.getInstance().getUserId());

        return layoutTodo;
    }

    public void refreshTodoList(Integer userId) {
        String url = Config.SERVER_HOST.replace("/api", "") + Config.URL_GET_TODO_URL.replace("{userId}", String.valueOf(userId));
        webView.loadUrl(url, HttpConnection.getHeaderMap());
    }
}
