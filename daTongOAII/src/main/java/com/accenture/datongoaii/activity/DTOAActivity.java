package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.os.Bundle;

import com.accenture.datongoaii.DTOARequest;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.fragment.FragTitleBar;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;

/**
 * Created by leon on 11/10/15.
 * Base Activity
 */
public class DTOAActivity extends Activity {
    protected FragTitleBar mFragTitleBar;

    protected void initTitleBar(int titleId) {
        mFragTitleBar = (FragTitleBar) getFragmentManager().findFragmentById(R.id.fragTitleBar);
        if (mFragTitleBar != null) {
            mFragTitleBar.init(titleId);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.gc();
    }

    @Override
    protected void onDestroy() {
        Logger.d("DTOAActivity", "onDestroy()");

        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left_1, R.anim.slide_out_right_1);
    }

    private long previousBackKeyPressTime;

    @Override
    public void onBackPressed() {
        if (this.isTaskRoot()) {
            long time = System.currentTimeMillis();
            if (previousBackKeyPressTime < time && previousBackKeyPressTime + 2000 > time) {
                DTOARequest.getInstance(this).startLogout();
                try {
                    Thread.sleep(400);
                } catch (Exception e) {
                    // do nothing
                }
                finish();
                System.exit(0);
            } else {
                previousBackKeyPressTime = time;
                Utils.toast(this, getString(R.string.label_press_again_to_quit));
            }
            return;
        }

        previousBackKeyPressTime = 0;
        super.onBackPressed();
    }

}
