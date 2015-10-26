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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.fragment.DeptFragment;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class DeptActivity extends Activity implements View.OnClickListener {
    public static final int HANDLER_TAG_DISMISS_PROGRESS_DIALOG = 0;

    private Context context;
    private Dept mDept;
    private Boolean isManageMode;

    private View layoutDept;
    private LinearLayout layoutButtons;
    private List<Fragment> fragList;
    private List<Dept> deptList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_dept);

        layoutButtons = (LinearLayout) findViewById(R.id.layoutButtons);
        layoutDept = findViewById(R.id.flContact);

        findViewById(R.id.btnBack).setOnClickListener(this);

        isManageMode = getIntent().getBooleanExtra(Constants.BUNDLE_TAG_ORG_IS_MANAGE_MODE, false);

        mDept = new Dept();
        mDept.id = getIntent().getIntExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_ID, -1);
        mDept.name = getIntent().getStringExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_NAME);
        fragList = new ArrayList<Fragment>();
        deptList = new ArrayList<Dept>();
        resolveButtons();
        resolveBottomBar();
        setFragmentDisplay(mDept, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CREATE_DEPT && resultCode == RESULT_OK) {
            setFragmentDisplay(mDept, true);
            return;
        }
        if (requestCode == Constants.REQUEST_CODE_ADD_DEPT_USER && resultCode == RESULT_OK) {
            setFragmentDisplay(mDept, true);
            return;
        }
        if (requestCode == Constants.REQUEST_CODE_MANAGE_DEPT && resultCode == RESULT_OK) {
            if (data.hasExtra(Constants.BUNDLE_TAG_RENAME_DEPT_NEW_NAME)) {
                String newName = data.getStringExtra(Constants.BUNDLE_TAG_RENAME_DEPT_NEW_NAME);
                Button btn = Utils.findButtonInButtons(mDept, layoutButtons);
                if (btn != null) {
                    btn.setText(newName);
                }
            }
            setFragmentDisplay(mDept, true);
            return;
        }
        if (requestCode == Constants.REQUEST_CODE_MANAGE_DEPT && resultCode == RESULT_FIRST_USER) {
            //删除部门
            Button btn = Utils.findButtonInButtons(mDept.parent, layoutButtons);
            Utils.removeButton(context, btn, layoutButtons);
            mDept = mDept.parent;
            setFragmentDisplay(mDept, true);
            resolveBottomBar();
        }
        if (requestCode == Constants.REQUEST_CODE_MANAGE_USER && resultCode == RESULT_OK) {
            setFragmentDisplay(mDept, true);
        }
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
                    Utils.removeButton(context, (Button) view, layoutButtons);
                    mDept = dept;
                    setFragmentDisplay(dept, false);
                    resolveBottomBar();
                } else if (view.getTag() instanceof Contact) {
                    Contact contact = (Contact) view.getTag();
                    Intent intent = new Intent(context, ManageUserActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_TAG_MANAGE_USER, contact);
                    bundle.putSerializable(Constants.BUNDLE_TAG_MANAGE_USER_DEPT, mDept);
                    intent.putExtras(bundle);
                    ((Activity) context).startActivityForResult(intent, Constants.REQUEST_CODE_MANAGE_USER);
                }
                break;
        }
    }

    public void onFragmentItemClick(Object obj) {
        if (obj instanceof Dept) {
            Dept dept = (Dept) obj;
            dept.parent = mDept;
            mDept = dept;
            Utils.addButton(this, dept, layoutButtons);
            setFragmentDisplay(mDept, false);
            resolveBottomBar();
        } else if (obj instanceof Contact) {
            Logger.i("onSectionItemClicked", "个人信息");
            Contact c = (Contact) obj;
            Intent intent = new Intent(context, ContactProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_TAG_CONTACT_PROFILE, c);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

    // 私有方法
    private void setFragmentDisplay(Dept dept, boolean isUpdated) {
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
                    DeptFragment deptFragment = (DeptFragment) fragList.get(i);
                    t.show(deptFragment);
                    if (isUpdated) {
                        deptFragment.setDisplayData(dept);
                    }
                    break;
                }
            }
        } else {
            for (Fragment frag : fragList) {
                t.hide(frag);
            }
            deptList.add(dept);
            Fragment frag;
            t.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            frag = new DeptFragment();
            ((DeptFragment) frag).setDisplayData(dept);
            ((DeptFragment) frag).setIsManageMode(isManageMode);
            t.add(R.id.flContact, frag);
            t.show(frag);
            fragList.add(frag);
        }
        t.commit();
    }

    private void resolveBottomBar() {
        if (isManageMode) {
            findViewById(R.id.llBottom).setVisibility(View.VISIBLE);
            findViewById(R.id.btnAddDept).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, CreateDeptActivity.class);
                    intent.putExtra(Constants.BUNDLE_TAG_CREATE_DEPT_DEPT_ID, mDept.id);
                    intent.putExtra(Constants.BUNDLE_TAG_CREATE_DEPT_DEPT_NAME, mDept.name);
                    ((Activity) context).startActivityForResult(intent, Constants.REQUEST_CODE_CREATE_DEPT);
                }
            });
            findViewById(R.id.btnAddUser).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, AddDeptUserActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_TAG_ADD_DEPT_USER, mDept);
                    intent.putExtras(bundle);
                    ((Activity) context).startActivityForResult(intent, Constants.REQUEST_CODE_ADD_DEPT_USER);
                }
            });
            View btnManageDept = findViewById(R.id.btnManageDept);
            if (mDept.id == null || mDept.id.equals(Account.getInstance().getOrg().orgId)) {
                btnManageDept.setVisibility(View.GONE);
            } else {
                btnManageDept.setVisibility(View.VISIBLE);
                btnManageDept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ManageDeptActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.BUNDLE_TAG_MANAGE_DEPT, mDept);
                        intent.putExtras(bundle);
                        ((Activity) context).startActivityForResult(intent, Constants.REQUEST_CODE_MANAGE_DEPT);
                    }
                });
            }
        } else {
            findViewById(R.id.llBottom).setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutDept.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
        }
    }

    private void resolveButtons() {
        Button btn = Utils.createButton(this, mDept);
        layoutButtons.addView(btn, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }


}
