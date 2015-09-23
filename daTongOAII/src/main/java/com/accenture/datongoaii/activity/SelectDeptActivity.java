package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.model.FirstPinYin;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Config;
import com.accenture.datongoaii.util.Constants;
import com.accenture.datongoaii.util.Intepreter;

import com.accenture.datongoaii.util.Utils;
import com.accenture.datongoaii.widget.SectionListView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SelectDeptActivity extends Activity implements View.OnClickListener, SectionListView.OnSectionItemClickedListener {
    public static final int HANDLER_TAG_DISMISS_PROGRESS_DIALOG = 0;

    private Context context;
    private Dept mDept;
    private List<Object> viewList;
    private List<Dept> selectList;
    private Boolean isMultiMode;
    private Integer invalidDeptId;

    private LinearLayout layoutButtons;
    private ProgressDialog progressDialog;
    private Handler handler = new ActivityHandler(this);

    static class ActivityHandler extends Handler {
        WeakReference<SelectDeptActivity> mActivity;

        public ActivityHandler(SelectDeptActivity activity) {
            this.mActivity = new WeakReference<SelectDeptActivity>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            SelectDeptActivity a = mActivity.get();
            switch (message.what) {
                case HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    if (a.progressDialog != null) {
                        a.progressDialog.dismiss();
                        a.progressDialog = null;
                    }
                    break;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_select_dept);

        viewList = new ArrayList<Object>();
        layoutButtons = (LinearLayout) findViewById(R.id.layoutButtons);
        SectionListView slvDept = (SectionListView) findViewById(R.id.slvDept);
        slvDept.setAdapter(adapter);
        slvDept.setOnSectionItemClickedListener(this);

        findViewById(R.id.btnBack).setOnClickListener(this);
        findViewById(R.id.btnSubmit).setOnClickListener(this);

        Integer deptId = getIntent().getIntExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_ID, -1);
        String orgName = getIntent().getStringExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_NAME);
        getDept(deptId);

        isMultiMode = getIntent().getBooleanExtra(Constants.BUNDLE_TAG_SELECT_DEPT_MULTI_MODE, true);
        invalidDeptId = getIntent().getIntExtra(Constants.BUNDLE_TAG_PARENT_DEPT_INVALID, Dept.DEPT_ID_INVALID);

        mDept = new Dept();
        mDept.id = Account.getInstance().getOrg().orgId;
        mDept.name = orgName;
        mDept.mFirstPinYin = "#";

        selectList = new ArrayList<Dept>();
        if (getIntent().hasExtra(Constants.BUNDLE_TAG_MANAGE_DEPT_SELECT_PARENT)) {
            List<Dept> list = (List<Dept>) getIntent().getSerializableExtra(Constants.BUNDLE_TAG_MANAGE_DEPT_SELECT_PARENT);
            if (list != null && list.size() > 0) {
                selectList.addAll(list);
            }
        }

        resolveButtons();
        refreshBottomBar();
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
                    clearData();
                    adapter.notifyDataSetChanged();
                    removeButton((Button) view);
                    Dept dept = (Dept) view.getTag();
                    getDept(dept.id);
                }
                break;
        }
    }

    // 私有方法
    private void clearData() {
        viewList.clear();
    }

    private void refreshData() {
        viewList.addAll(mDept.subDept);
        List<Object> tmpList = new ArrayList<Object>();
        tmpList.addAll(FirstPinYin.createPinYinGroupedList(viewList));
        viewList.clear();
        viewList.addAll(tmpList);
        if (viewList.size() == 0) {
            Utils.toast(context, Config.NOTE_DEPT_EMPTY);
        }
        adapter.notifyDataSetChanged();
    }

    private void resolveButtons() {
        Button btn = createButton(mDept);
        layoutButtons.addView(btn, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private Button createButton(Dept dept) {
        Button btn = new Button(context);
        btn.setBackgroundColor(getResources().getColor(R.color.transparent));
        btn.setText(dept.name);
        btn.setPadding(8, 12, 8, 12);
        btn.setTextColor(getResources().getColor(R.color.gray_2));
        btn.setTag(dept);
        btn.setTextSize((float) 18);
        btn.setOnClickListener(this);
        return btn;
    }

    private void addButton(Dept dept) {
        TextView sep = new TextView(context);
        sep.setBackgroundColor(getResources().getColor(R.color.transparent));
        sep.setText(">");
        sep.setTextColor(getResources().getColor(R.color.gray_2));
        layoutButtons.addView(sep, 15, layoutButtons.getHeight());
        for (int i = 0; i < layoutButtons.getChildCount(); i++) {
            View view = layoutButtons.getChildAt(i);
            if (view instanceof Button) {
                ((Button) view).setTextColor(getResources().getColor(R.color.tab_text_focused));
            }
        }
        Button btn = createButton(dept);
        layoutButtons.addView(btn, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void removeButton(Button btn) {
        Integer index = layoutButtons.indexOfChild(btn);
        for (int i = layoutButtons.getChildCount() - 1; i > index; i--) {
            layoutButtons.removeViewAt(i);
        }
        btn.setTextColor(getResources().getColor(R.color.gray_2));
    }

    private void refreshBottomBar() {
        LinearLayout llBottomBtns = (LinearLayout) findViewById(R.id.llBottomBtns);
        llBottomBtns.removeAllViews();
        for (Dept dept : selectList) {
            Button btn = createButton(dept);
            btn.setTextColor(getResources().getColor(R.color.tab_text_focused));
            btn.setTag(dept);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dept d = (Dept) view.getTag();
                    Dept.removes(selectList, d);
                    refreshBottomBar();
                    adapter.notifyDataSetChanged();
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

    // 网络数据
    private void getDept(Integer deptId) {
//        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_GET);
        String url = Config.SERVER_HOST + Config.URL_DEPT.replace("{groupId}", deptId + "");
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                handler.sendEmptyMessage(HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                if (!result.equals("fail")) {
                    try {
                        CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                        if (cr == null) {
                            Utils.toast(context, Config.ERROR_INTERFACE);
                            return;
                        }
                        if (cr.statusCode == 0) {
                            clearData();
                            mDept = Dept.fromJSON(new JSONObject(result).getJSONObject("data"));
                            refreshData();
                        } else {
                            Utils.toast(context, cr.statusMsg);
                        }
                    } catch (JSONException e) {
                        Utils.toast(context, Config.ERROR_INTERFACE);
                    }
                } else {
                    Utils.toast(context, Config.ERROR_NETWORK);
                }
            }
        });
    }

    // adapter
    private final SectionListView.SectionListAdapter adapter = new SectionListView.SectionListAdapter() {
        @Override
        public int getSectionCount() {
            if (viewList == null) {
                return 0;
            } else {
                return viewList.size();
            }
        }

        @Override
        public int getSectionItemCount(int section) {
            Object object = viewList.get(section);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) object;
            return list.size();
        }

        @Override
        public View getSectionHeaderView(int section, View convertView,
                                         ViewGroup parent) {
            Object o = getItem(section, 0);
            View view = convertView;
            if (view == null) {
                view = View.inflate(parent.getContext(),
                        R.layout.section_list_header, null);
            }
            TextView tv = (TextView) view.findViewById(R.id.txtLabel);
            if (o instanceof Dept) {
                tv.setText("#");
            } else if (o instanceof Contact) {
                tv.setText(((Contact) o).mFirstPinYin);
            }
            return view;
        }

        @Override
        public View getSectionItemView(int section, int position,
                                       View convertView, ViewGroup parent) {
            Object o = getItem(section, position);
            View view = convertView;
            if (view == null) {
                view = View.inflate(parent.getContext(),
                        R.layout.list_cell_org, null);
            }
            TextView tv = (TextView) view.findViewById(R.id.tvName);
            ImageView iv = (ImageView) view.findViewById(R.id.ivIcon);
            ImageView ivSelect = (ImageView) view.findViewById(R.id.ivSelect);
            ivSelect.setTag(o);
            if (Dept.contains(selectList, (Dept) o)) {
                ivSelect.setImageResource(R.drawable.ic_selected);
            } else {
                ivSelect.setImageResource(R.drawable.ic_unselected);
            }
            ivSelect.setVisibility(View.VISIBLE);
            ivSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dept dept = (Dept) view.getTag();
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
                    adapter.notifyDataSetChanged();
                    refreshBottomBar();
                }
            });
            ImageLoader il = ImageLoader.getInstance();
            TextView tvValue = (TextView) view.findViewById(R.id.tvValue);
            ImageView ivArrow = (ImageView) view.findViewById(R.id.ivArrow);
            tvValue.setVisibility(View.VISIBLE);
            ivArrow.setVisibility(View.VISIBLE);
            Dept d = (Dept) o;
            if (d.img != null && d.img.length() > 0) {
                il.displayImage(d.img, iv, Config.getDisplayOptions());
            } else {
                iv.setImageDrawable(view.getContext().getResources().getDrawable(R.drawable.ic_contact_c));
            }
            tv.setText(d.name);
            tvValue.setText(d.userCount + "人");
            if (d.id.equals(invalidDeptId)) {
                ivSelect.setOnClickListener(null);
                view.setOnClickListener(null);
                view.setBackgroundColor(getResources().getColor(R.color.gray_5));
            }
            view.setTag(o);
            return view;
        }

        @Override
        public Object getItem(int section, int position) {
            Object object = viewList.get(section);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) object;
            return list.get(position);
        }

        @Override
        public String getSectionLabel(int section) {
            FirstPinYin o = (FirstPinYin) getItem(section, 0);
            return o.mFirstPinYin;
        }
    };


    @Override
    public void onSectionItemClicked(SectionListView listView, View view, int section, int position) {
        if (view.getTag() instanceof Dept) {
            clearData();
            adapter.notifyDataSetChanged();
            Dept dept = (Dept) view.getTag();
            mDept = dept;
            addButton(dept);
            getDept(dept.id);
        }
    }
}
