package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DeptActivity extends Activity implements View.OnClickListener, SectionListView.OnSectionItemClickedListener {
    public static final int HANDLER_TAG_DISMISS_PROGRESS_DIALOG = 0;

    private Context context;
    private Integer mOrgId;
    private Dept mDept;
    private List<Object> viewList;

    private LinearLayout layoutButtons;
    private ProgressDialog progressDialog;
    private Handler handler = new ActivityHandler(this);

    static class ActivityHandler extends Handler {
        WeakReference<DeptActivity> mActivity;

        public ActivityHandler(DeptActivity activity) {
            this.mActivity = new WeakReference<DeptActivity>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            DeptActivity a = mActivity.get();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_dept);

        viewList = new ArrayList<Object>();
        layoutButtons = (LinearLayout) findViewById(R.id.layoutButtons);
        SectionListView slvDept = (SectionListView) findViewById(R.id.slvDept);
        slvDept.setAdapter(adapter);
        slvDept.setOnSectionItemClickedListener(this);

        findViewById(R.id.btnBack).setOnClickListener(this);

        mOrgId = getIntent().getIntExtra(Constants.BUNDLE_TAG_GET_DEPT_ORG_ID, -1);
        Integer deptId = getIntent().getIntExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_ID, -1);
        String orgName = getIntent().getStringExtra(Constants.BUNDLE_TAG_GET_DEPT_DEPT_NAME);
        getDept(mOrgId, deptId);

        mDept = new Dept();
        mDept.id = -1;
        mDept.name = orgName;
        resolveButtons();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                finish();
                break;
            default:
                if (view instanceof Button) {
                    clearData();
                    adapter.notifyDataSetChanged();
                    removeButton((Button) view);
                    Dept dept = (Dept) view.getTag();
                    getDept(mOrgId, dept.id);
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
        viewList.addAll(mDept.contactList);
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

    // 网络数据
    private void getDept(Integer orgId, Integer deptId) {
//        progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_GET);
        String url = Config.SERVER_HOST + Config.URL_DEPT.replace("{orgId}", orgId + "").replace("{deptId}", deptId + "");
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
            ImageLoader il = ImageLoader.getInstance();
            TextView tvValue = (TextView) view.findViewById(R.id.tvValue);
            ImageView ivArrow = (ImageView) view.findViewById(R.id.ivArrow);
            if (o instanceof Dept) {
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
            } else if (o instanceof Contact) {
                tvValue.setVisibility(View.GONE);
                ivArrow.setVisibility(View.GONE);
                Contact c = (Contact) o;
                if (c.head.length() > 0) {
                    il.displayImage(c.head, iv);
                } else {
                    iv.setImageResource(R.drawable.ic_contact_p);
                }
                tv.setText(c.name);
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
            getDept(mOrgId, dept.id);
        }
    }
}
