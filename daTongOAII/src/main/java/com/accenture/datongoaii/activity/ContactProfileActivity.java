package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.vendor.HX.activity.ChatActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by leon on 10/3/15.
 * 用户详情
 */
public class ContactProfileActivity extends Activity {
    private Context context;
    private Contact mContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_profile);
        context = this;

        ImageView ivHead = (ImageView) findViewById(R.id.ivHead);
        TextView tvName = (TextView) findViewById(R.id.tvUsername);
        TextView tvCell = (TextView) findViewById(R.id.tvCell);

        mContact = (Contact) getIntent().getSerializableExtra(Constants.BUNDLE_TAG_CONTACT_PROFILE);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(mContact.head, ivHead, Config.getDisplayOptions());
        tvName.setText(mContact.name);
        tvCell.setText(mContact.cell);

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (mContact.imId != null && mContact.imId.length() > 0) {
            findViewById(R.id.btnSendMessage).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("userId", mContact.imId);
                    intent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
                    startActivity(intent);
                    finish();
                }
            });
        } else {
            findViewById(R.id.btnSendMessage).setVisibility(View.INVISIBLE);
        }
    }
}
