package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.util.Config;
import com.accenture.datongoaii.util.Constants;
import com.accenture.datongoaii.util.Logger;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class SelectDeptActivity extends Activity implements AdapterView.OnItemClickListener {
    private ListView lvDept;
    private Dept dept;
    private List<Object> viewList;
    private List<Object> tmpList;
    private int prevFunction;

    private EditText etSearch;
    private ImageView ivSearch;
    private TextView tvSearch;
    private View lBack;
    private Button btnSubmit;

    private void clearData() {
        tmpList.clear();
        viewList.clear();
    }

    private void syncData() {
        if (dept != null) {
            viewList.addAll(dept.subDept);
        }
        tmpList.clear();
        tmpList.addAll(viewList);
        adapter.notifyDataSetChanged();
    }

    private final BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return dept.subDept.size();
        }

        @Override
        public Object getItem(int i) {
            return dept.subDept.get(i);
        }

        @Override
        public long getItemId(int i) {
            return dept.subDept.get(i).id;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Dept d = (Dept) getItem(i);
            if (view == null) {
                view = View.inflate(view.getContext(),
                        R.layout.list_cell_contact, null);
            }
            TextView tv = (TextView) view.findViewById(R.id.tvName);
            ImageView iv = (ImageView) view.findViewById(R.id.ivIcon);
            ImageView ivSelect = (ImageView) view.findViewById(R.id.ivSelect);
            Button btnAdd = (Button) view.findViewById(R.id.btnAdd);
            ImageLoader il = ImageLoader.getInstance();
            if (d.img.length() > 0) {
                il.displayImage(d.img, iv, Config.getDisplayOptions());
            } else {
                iv.setImageResource(R.drawable.ic_contact_c);
            }
            ivSelect.setVisibility(View.GONE);
            tv.setText(d.name);
            btnAdd.setVisibility(View.VISIBLE);
            view.setTag(d);
            return view;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_dept);

        etSearch = (EditText) findViewById(R.id.etSearch);
        ivSearch = (ImageView) findViewById(R.id.ivSearch);
        tvSearch = (TextView) findViewById(R.id.tvSearch);
        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Logger.i("afterTextChanged", s.toString());
                if (s.toString().length() > 0) {
                    ivSearch.setVisibility(View.GONE);
                    tvSearch.setVisibility(View.GONE);
                    viewList.clear();
                    viewList.addAll(getFiltedList(tmpList, s.toString()));
                    adapter.notifyDataSetChanged();
                } else {
                    ivSearch.setVisibility(View.VISIBLE);
                    tvSearch.setVisibility(View.VISIBLE);
                    viewList.clear();
                    viewList.addAll(tmpList);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        lBack = findViewById(R.id.lBack);

        lvDept = (ListView) findViewById(R.id.lvDept);
        viewList = new ArrayList<Object>();
        tmpList = new ArrayList<Object>();
        lvDept.setAdapter(adapter);
        getDept();

        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectDeptActivity.this.finish();
            }
        });

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setEnabled(false);
        btnSubmit.setBackgroundResource(R.drawable.button_disable);
        btnSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_TAG_SELECT_DEPT, dept);
                intent.putExtras(bundle);
                SelectDeptActivity.this.setResult(Activity.RESULT_OK, intent);
                SelectDeptActivity.this.finish();
            }
        });

    }

    private List<List<Object>> getFiltedList(List<?> list, String str) {
        List<List<Object>> tList = new ArrayList<List<Object>>();
        for (Object o : list) {
            List<Object> ll = new ArrayList<Object>();
            List<Object> oList = (List<Object>) o;
            for (Object oo : oList) {
                ll.add(oo);
            }
            tList.add(ll);
        }
        for (int i = tList.size() - 1; i >= 0; i--) {
            List<Object> l = tList.get(i);
            for (int j = l.size() - 1; j >= 0; j--) {
                Object o = l.get(j);
                if (o instanceof Dept) {
                    if (!((Dept) o).name.contains(str)) {
                        l.remove(o);
                    }
                }
            }
            if (l.size() == 0) {
                tList.remove(l);
            }
        }
        return tList;
    }

    private void getDept() {
        clearData();
//        dept = Account.getInstance().getDept();
        syncData();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        clearData();
        Dept d = dept.subDept.get(i);
        dept = d;
        syncData();
    }
}
