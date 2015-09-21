package com.accenture.datongoaii.activity;

import android.app.Activity;
//import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.fragment.ContactRootFragment;
import com.accenture.datongoaii.fragment.DeptFragment;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Dept;

import com.accenture.datongoaii.util.Constants;
import com.accenture.datongoaii.util.Utils;


public class SelectUserActivity extends FragmentActivity implements View.OnClickListener {
//    public static final int HANDLER_TAG_DISMISS_PROGRESS_DIALOG = 0;

    private Fragment currentFrag;


    private Context context;
    private Dept mDept;
    private LinearLayout llNavBtns;
//    private ProgressDialog progressDialog;
//    private Handler handler = new ActivityHandler(this);
//
//    public static class ActivityHandler extends Handler {
//        WeakReference<SelectUserActivity> mActivity;
//
//        public ActivityHandler(SelectUserActivity activity) {
//            this.mActivity = new WeakReference<SelectUserActivity>(activity);
//        }
//
//        @Override
//        public void handleMessage(Message message) {
//            SelectUserActivity a = mActivity.get();
//            switch (message.what) {
//                case HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
//                    if (a.progressDialog != null) {
//                        a.progressDialog.dismiss();
//                        a.progressDialog = null;
//                    }
//                    break;
//            }
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_select_user);

        llNavBtns = (LinearLayout) findViewById(R.id.llNavBtns);

        findViewById(R.id.btnBack).setOnClickListener(this);

        mDept = new Dept();
        mDept.id = Dept.DEPT_ID_ROOT_CONTACT;
        mDept.name = "联系人";

        initNavButton();
        setDisplay(mDept);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                finish();
                break;
            default:
                if (view.getTag() instanceof Dept) {
                    Dept dept = (Dept) view.getTag();
                    Utils.removeButton(context, (Button) view, llNavBtns);
                    setDisplay(dept);
                }
                break;
        }
    }

    // 公有方法
    public void onFragmentItemClick(Object obj) {
        if (obj instanceof Dept) {
            setDisplay((Dept) obj);
            Utils.addButton(context, (Dept) obj, llNavBtns);
            return;
        }
        if (obj instanceof Contact) {
            Contact contact = (Contact) obj;
            Intent intent = new Intent();
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_CELL, contact.cell);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_NAME, contact.name);
            this.setResult(Activity.RESULT_OK, intent);
            this.finish();
        }
    }

    // 私有方法
    private void initNavButton() {
        Button btn = Utils.createButton(context, mDept);
        llNavBtns.addView(btn, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void setDisplay(Dept dept) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        if (currentFrag != null) {
            t.remove(currentFrag);
        }
        if (dept.id.equals(Dept.DEPT_ID_ROOT_CONTACT)) {
            ContactRootFragment rootFrag = new ContactRootFragment();
            t.add(R.id.flContact, rootFrag);
            currentFrag = rootFrag;
        } else {
            DeptFragment deptFrag = new DeptFragment();
            deptFrag.setDisplayData(dept);
            t.add(R.id.flContact, deptFrag);
            currentFrag = deptFrag;
        }
        t.commitAllowingStateLoss();
    }
}
