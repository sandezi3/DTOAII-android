package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.fragment.ContactRootFragment;
import com.accenture.datongoaii.fragment.DeptFragment;
import com.accenture.datongoaii.fragment.FriendFragment;
import com.accenture.datongoaii.fragment.PhoneContactFragment;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.model.Org;
import com.accenture.datongoaii.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SelectUserActivity extends Activity implements View.OnClickListener {
    private Context context;
    private Dept mDept;
    private LinearLayout llNavBtns;
    private boolean isMultiMode;
    private LinearLayout llBottom;
    private List<Contact> selectedUsers;
    private List<Fragment> fragList;
    private List<Dept> deptList;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_select_user);

        llNavBtns = (LinearLayout) findViewById(R.id.llNavBtns);

        findViewById(R.id.btnBack).setOnClickListener(this);

        mDept = new Dept();
        mDept.id = Dept.DEPT_ID_ROOT_CONTACT;
        mDept.name = getResources().getText(R.string.label_contact).toString();

        isMultiMode = getIntent().getBooleanExtra(Constants.BUNDLE_TAG_SELECT_USER_MULTI_MODE, false);
        RelativeLayout rlBottom = (RelativeLayout) findViewById(R.id.rlBottom);
        if (isMultiMode) {
            llBottom = (LinearLayout) findViewById(R.id.llBottom);
            findViewById(R.id.btnSubmit).setOnClickListener(this);
            rlBottom.setVisibility(View.VISIBLE);
            selectedUsers = (List<Contact>) getIntent().getSerializableExtra(Constants.BUNDLE_TAG_SELECT_USER_MULTI_MODE_RESULT);
            refreshBottomBar();
        } else {
            rlBottom.setVisibility(View.GONE);
        }

        initNavButton();
        fragList = new ArrayList<Fragment>();
        deptList = new ArrayList<Dept>();
        setFragmentDisplay(mDept);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                finish();
                break;
            case R.id.btnSubmit:
                if (isMultiMode) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_TAG_SELECT_USER_MULTI_MODE_RESULT, (Serializable) selectedUsers);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
            default:
                if (view.getTag() instanceof Dept) {
                    Dept dept = (Dept) view.getTag();
                    Utils.removeButton(context, (Button) view, llNavBtns);
                    setFragmentDisplay(dept);
                } else if (isMultiMode && view.getTag() instanceof Contact) {
                    Contact contact = (Contact) view.getTag();
                    if (!contact.id.equals(Account.getInstance().getUserId())) {
                        selectedUsers.remove(contact);
                        refreshBottomBar();
                    }
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
        if (obj instanceof Org) {
            Dept dept = new Dept();
            dept.id = ((Org)obj).orgId;
            dept.name = ((Org)obj).orgName;
            dept.mFirstPinYin = ((Org)obj).mFirstPinYin;
            dept.img = ((Org)obj).logo;
            setFragmentDisplay(dept);
            Utils.addButton(context, dept, llNavBtns);
            return;
        }
        if (obj instanceof Dept) {
            Dept dept = (Dept) obj;
            setFragmentDisplay(dept);
            Utils.addButton(context, (Dept) obj, llNavBtns);
            return;
        }
        if (obj instanceof Contact) {
            Contact contact = (Contact) obj;
            if (isMultiMode) {
                // 排除重选
                if (!Contact.contains(selectedUsers, contact)) {
                    selectedUsers.add(contact);
                    refreshBottomBar();
                } else {
                    Utils.toast(context, Config.NOTE_CHOSEN_CONTACT);
                }
            } else {
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
    }

    // 私有方法
    private void initNavButton() {
        Button btn = Utils.createButton(context, mDept);
        llNavBtns.addView(btn, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void setFragmentDisplay(Dept dept) {
        FragmentTransaction t = getFragmentManager().beginTransaction();
        if (deptList.contains(dept)) {
            t.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            for (int i = deptList.size() - 1; i >= 0; i--) {
                Dept d = deptList.get(i);
                Fragment frag = fragList.get(i);
                if (!dept.equals(d)) {
                    deptList.remove(i);
                    t.remove(frag);
                    fragList.remove(i);
                } else {
                    t.show(fragList.get(i));
                    break;
                }
            }
        } else {
            for (Fragment frag : fragList) {
                t.hide(frag);
            }
            deptList.add(dept);
            Fragment frag;
            if (dept.id.equals(Dept.DEPT_ID_ROOT_CONTACT)) {
                frag = new ContactRootFragment();
            } else if (dept.id.equals(Dept.DEPT_ID_MY_FRIENDS)) {
                frag = new FriendFragment();
                ((FriendFragment) frag).isSelectMode = false;
                ((FriendFragment) frag).isMultiMode = isMultiMode;
            } else if (dept.id.equals(Dept.DEPT_ID_PHONE_CONTACT)) {
                frag = new PhoneContactFragment();
                ((PhoneContactFragment) frag).isSelectMode = true;
                ((PhoneContactFragment) frag).isMultiMode = isMultiMode;
            } else {
                t.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                frag = new DeptFragment();
                ((DeptFragment) frag).setDisplayData(dept);
            }
            t.add(R.id.flContact, frag);
            t.show(frag);
            fragList.add(frag);
        }
        t.commit();
    }

    private void refreshBottomBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                llBottom.removeAllViews();
                for (Contact contact : selectedUsers) {
                    Utils.addButton(context, contact, llBottom);
                }
                ((HorizontalScrollView) findViewById(R.id.hsvBottom)).fullScroll(View.FOCUS_RIGHT);
                Button btnSubmit = (Button) findViewById(R.id.btnSubmit);
                if (!isSubmitButtonEnabled()) {
                    btnSubmit.setEnabled(false);
                    btnSubmit.setBackgroundResource(R.drawable.button_disable);
                } else {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setBackgroundResource(R.drawable.button_normal);
                }
            }
        });
    }

    private boolean isSubmitButtonEnabled() {
        return selectedUsers.size() != 0 && !(selectedUsers.size() == 1 && selectedUsers.get(0).id.equals(Account.getInstance().getUserId()));
    }
}
