package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.fragment.FriendFragment;
import com.accenture.datongoaii.model.Account;

public class MyFriendActivity extends Activity {
    private Activity context;
    private FriendFragment friendFragment;

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_my_friends);

        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                context.finish();
            }
        });

        FragmentTransaction t = getFragmentManager().beginTransaction();
        if (friendFragment == null) {
            friendFragment = new FriendFragment();
            friendFragment.isSelectMode = getIntent().getBooleanExtra(Constants.BUNDLE_TAG_SELECT_PHONE_CONTACT, false);
            friendFragment.isMultiMode = false;
            t.add(R.id.flContent, friendFragment);
            t.commitAllowingStateLoss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_DELETE_FRIEND && resultCode == RESULT_OK) {
            friendFragment.getFriends(Account.getInstance().getUserId());
        }
    }
}
