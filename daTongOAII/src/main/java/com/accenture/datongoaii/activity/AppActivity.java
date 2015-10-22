package com.accenture.datongoaii.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.App;
import com.accenture.datongoaii.util.Logger;

/**
 * Created by leon on 10/21/15.
 * 应用Activity
 */
public class AppActivity extends Activity {
    private static final String TAG = "AppActivity";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        WebView webView = (WebView) findViewById(R.id.wvContent);
        App app = (App) getIntent().getSerializableExtra(Constants.BUNDLE_TAG_APP);
        Logger.i(TAG, "App url = " + app.url);

        ((TextView) findViewById(R.id.textTitle)).setText(app.appName);
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //启用支持javascript
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
        String url = Config.SERVER_HOST.replace("/api", "").replace("{userId}", String.valueOf(Account.getInstance().getUserId()));
        webView.loadUrl(url);
    }
}
