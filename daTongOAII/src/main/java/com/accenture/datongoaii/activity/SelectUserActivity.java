package com.accenture.datongoaii.activity;

import android.app.Activity;
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
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.util.Utils;

public class SelectUserActivity extends FragmentActivity implements View.OnClickListener {
    private Fragment currentFrag;

    private Context context;
    private Dept mDept;
    private LinearLayout llNavBtns;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_SELECT_USER && resultCode == RESULT_OK) {
            if (data.hasExtra(Constants.BUNDLE_TAG_SELECT_USER_NAME) && data.hasExtra(Constants.BUNDLE_TAG_SELECT_USER_CELL)) {
                String cell = data.getStringExtra(Constants.BUNDLE_TAG_SELECT_USER_CELL);
                String name = data.getStringExtra(Constants.BUNDLE_TAG_SELECT_USER_NAME);
                Integer userId = data.getIntExtra(Constants.BUNDLE_TAG_SELECT_USER_ID, -1);
                Intent intent = new Intent();
                intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_CELL, cell);
                intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_NAME, name);
                intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_ID, userId);
                this.setResult(Activity.RESULT_OK, intent);
                this.finish();
            }
        }
    }

    // 公有方法
    public void onFragmentItemClick(Object obj) {
        if (obj instanceof Dept) {
            Dept dept = (Dept) obj;
            setDisplay(dept);
            if (!dept.id.equals(Dept.DEPT_ID_PHONE_CONTACT) && !dept.id.equals(Dept.DEPT_ID_MY_FRIENDS)) {
                Utils.addButton(context, (Dept) obj, llNavBtns);
            }
            return;
        }
        if (obj instanceof Contact) {
            Contact contact = (Contact) obj;
            Intent intent = new Intent();
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_CELL, contact.cell);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_NAME, contact.name);
            if (contact.id != null && contact.id > 0) {
                intent.putExtra(Constants.BUNDLE_TAG_SELECT_USER_ID, contact.id);
            }
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
        if (dept.id.equals(Dept.DEPT_ID_ROOT_CONTACT)) {
            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            if (currentFrag != null) {
                t.remove(currentFrag);
            }
            ContactRootFragment rootFrag = new ContactRootFragment();
            t.add(R.id.flContact, rootFrag);
            currentFrag = rootFrag;
            t.commitAllowingStateLoss();
        } else if (dept.id.equals(Dept.DEPT_ID_MY_FRIENDS)) {
            Intent intent = new Intent(context, MyFriendActivity.class);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_PHONE_CONTACT, true);
            startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_USER);
        } else if (dept.id.equals(Dept.DEPT_ID_PHONE_CONTACT)) {
            Intent intent = new Intent(context, SelectPhoneContactActivity.class);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_PHONE_CONTACT, true);
            startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_USER);
        } else {
            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            if (currentFrag != null) {
                t.remove(currentFrag);
            }
            DeptFragment deptFrag = new DeptFragment();
            deptFrag.setDisplayData(dept);
            t.add(R.id.flContact, deptFrag);
            currentFrag = deptFrag;
            t.commitAllowingStateLoss();
        }
    }
}
