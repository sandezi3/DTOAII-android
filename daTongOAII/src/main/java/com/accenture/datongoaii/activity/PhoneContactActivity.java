package com.accenture.datongoaii.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.fragment.PhoneContactFragment;

public class PhoneContactActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_contact);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        PhoneContactFragment fcFrag = new PhoneContactFragment();
        fcFrag.isSelectMode = getIntent().getBooleanExtra(Constants.BUNDLE_TAG_SELECT_PHONE_CONTACT, false);
        t.add(R.id.flContact, fcFrag);
        t.commitAllowingStateLoss();

        findViewById(R.id.btnBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
