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

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.fragment.SelectDeptFragment;
import com.accenture.datongoaii.fragment.SelectDeptRootFragment;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SelectDeptActivity extends Activity implements View.OnClickListener {
    public List<Dept> selectList;
    public Integer invalidDeptId;

    private Context context;
    private Dept mDept;
    private Boolean isMultiMode;
    private List<Fragment> fragList;
    private List<Dept> deptList;

    private LinearLayout layoutButtons;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_select_dept);

        layoutButtons = (LinearLayout) findViewById(R.id.layoutButtons);

        findViewById(R.id.btnBack).setOnClickListener(this);
        findViewById(R.id.btnSubmit).setOnClickListener(this);

        String orgName = getIntent().getStringExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_NAME);

        isMultiMode = getIntent().getBooleanExtra(Constants.BUNDLE_TAG_SELECT_DEPT_MULTI_MODE, true);
        invalidDeptId = getIntent().getIntExtra(Constants.BUNDLE_TAG_PARENT_DEPT_INVALID, Dept.DEPT_ID_INVALID);

        mDept = new Dept();
        mDept.id = Dept.DEPT_ID_ROOT_CONTACT;
        mDept.name = "组织架构";
        mDept.subDept = new ArrayList<Dept>();
        Dept org = new Dept();
        org.id = Account.getInstance().getCreatedOrg().orgId;
        org.name = orgName;
        mDept.subDept.add(org);

        selectList = new ArrayList<Dept>();
        if (getIntent().hasExtra(Constants.BUNDLE_TAG_MANAGE_DEPT_SELECT_PARENT)) {
            List<Dept> list = (List<Dept>) getIntent().getSerializableExtra(Constants.BUNDLE_TAG_MANAGE_DEPT_SELECT_PARENT);
            if (list != null && list.size() > 0) {
                selectList.addAll(list);
            }
        }

        fragList = new ArrayList<Fragment>();
        deptList = new ArrayList<Dept>();

        resolveButtons();
        refreshBottomBar();
        setFragmentDisplay(mDept);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                finish();
                break;
            case R.id.btnSubmit: {
                if (selectList.size() == 0) {
                    Utils.toast(context, Config.NOTE_SELECT_PARENT_DEPT);
                    return;
                }
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_TAG_SELECT_DEPT, (Serializable) selectList);
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
            break;
            default:
                if (view instanceof Button) {
                    Dept dept = (Dept) view.getTag();
                    Utils.removeButton(context, (Button) view, layoutButtons);
                    mDept = dept;
                    setFragmentDisplay(dept);
                    refreshBottomBar();
                }
                break;
        }
    }

    // 公有方法
    public void onFragmentItemClick(Dept dept) {
        dept.parent = mDept;
        mDept = dept;
        Utils.addButton(context, dept, layoutButtons);
        setFragmentDisplay(dept);
    }

    public void onDeptSelect(Dept dept) {
        if (isMultiMode) {
            if (Dept.contains(selectList, dept)) {
                Dept.removes(selectList, dept);
            } else {
                selectList.add(dept);
            }
        } else {
            if (selectList.size() > 0) {
                selectList.clear();
            }
            selectList.add(dept);
        }

        for (Fragment fragment : fragList) {
            if (fragment instanceof SelectDeptFragment) {
                ((SelectDeptFragment) fragment).refreshData();
            } else if (fragment instanceof SelectDeptRootFragment) {
                ((SelectDeptRootFragment) fragment).refreshData();
            }
        }
        refreshBottomBar();
    }

    // 私有方法
    private void resolveButtons() {
        Button btn = Utils.createButton(context, mDept);
        layoutButtons.addView(btn, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void refreshBottomBar() {
        LinearLayout llBottomBtns = (LinearLayout) findViewById(R.id.llBottomBtns);
        llBottomBtns.removeAllViews();
        for (Dept dept : selectList) {
            Button btn = Utils.createButton(context, dept);
            btn.setTextColor(getResources().getColor(R.color.tab_text_focused));
            btn.setTag(dept);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dept d = (Dept) view.getTag();
                    Dept.removes(selectList, d);
                    refreshBottomBar();
                    for (Fragment fragment : fragList) {
                        if (fragment instanceof SelectDeptFragment) {
                            ((SelectDeptFragment) fragment).refreshData();
                        } else if (fragment instanceof SelectDeptRootFragment) {
                            ((SelectDeptRootFragment) fragment).refreshData();
                        }
                    }
                }
            });
            llBottomBtns.addView(btn, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        Button btnSubmit = (Button) findViewById(R.id.btnSubmit);
        if (selectList.size() == 0) {
            btnSubmit.setEnabled(false);
            btnSubmit.setBackgroundResource(R.drawable.button_disable);
        } else {
            btnSubmit.setEnabled(true);
            btnSubmit.setBackgroundResource(R.drawable.button_normal);
        }
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
                    if (dept.id == Dept.DEPT_ID_ROOT_CONTACT) {
                        ((SelectDeptRootFragment) fragList.get(i)).setDisplayData(dept);
                    } else {
                        ((SelectDeptFragment) fragList.get(i)).setDisplayData(dept);
                    }
                    t.show(fragList.get(i));
                    break;
                }
            }
        } else {
            for (Fragment frag : fragList) {
                t.hide(frag);
            }
            deptList.add(dept);
            android.app.Fragment frag;
            t.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            if (dept.id == Dept.DEPT_ID_ROOT_CONTACT) {
                frag = new SelectDeptRootFragment();
                ((SelectDeptRootFragment) frag).setDisplayData(dept);
            } else {
                frag = new SelectDeptFragment();
                ((SelectDeptFragment) frag).setDisplayData(dept);
            }
            t.add(R.id.flContact, frag);
            t.show(frag);
            fragList.add(frag);
        }
        t.commit();
    }

}
