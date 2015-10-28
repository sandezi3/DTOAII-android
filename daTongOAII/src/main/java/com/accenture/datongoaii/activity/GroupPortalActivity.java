package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.adapter.AppGridAdapter;
import com.accenture.datongoaii.model.App;
import com.accenture.datongoaii.model.Group;
import com.accenture.datongoaii.widget.ExpandGridView;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by leon on 10/3/15.
 * 群聊详情
 */
public class GroupPortalActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_group_portal);

        ExpandGridView gvApps = (ExpandGridView) findViewById(R.id.gvApps);
        TextView textTitle = (TextView) findViewById(R.id.textTitle);
        ImageView ivHead = (ImageView) findViewById(R.id.ivHead);
        TextView tvName = (TextView) findViewById(R.id.tvName);

        Group mGroup = (Group) getIntent().getSerializableExtra(Constants.BUNDLE_TAG_GROUP);
        String type = getIntent().getStringExtra(Constants.BUNDLE_TAG_GROUP_TYPE);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(mGroup.img, ivHead, Config.getDisplayOptions());
        textTitle.setText(mGroup.name);
        tvName.setText(type);
        findViewById(R.id.btnBack).setOnClickListener(this);
        AppGridAdapter appAdapter = new AppGridAdapter(context, mGroup.apps);
        gvApps.setAdapter(appAdapter);
        gvApps.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        App app = (App) parent.getAdapter().getItem(position);
        Intent intent = new Intent(context, AppActivity.class);
        intent.putExtra(Constants.BUNDLE_TAG_APP, app);
        startActivity(intent);
    }
}
