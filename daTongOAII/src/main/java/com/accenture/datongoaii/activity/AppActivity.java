package com.accenture.datongoaii.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.App;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;

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
        Utils.initWebViewSettings(webView);
        App app = (App) getIntent().getSerializableExtra(Constants.BUNDLE_TAG_APP);
        Logger.i(TAG, "App url = " + app.url);
        String url = Config.SERVER_HOST.replace("/api", "") + app.url.replace("{userId}", String.valueOf(Account.getInstance().getUserId()));

        ((TextView) findViewById(R.id.textTitle)).setText(app.appName);
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        webView.loadUrl(url, HttpConnection.getHeaderMap());
    }
}
