package com.accenture.datongoaii.activity;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.util.Config;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MyInfoActivity extends Activity {
    private ListView lvMenu;
    private SparseArray<String[]> menu;
    private MenuAdapter adapter;

    private class MenuAdapter extends BaseAdapter {
        private ImageLoader imageLoader = ImageLoader.getInstance();

        @Override
        public int getCount() {
            return menu.size();
        }

        @Override
        public Object getItem(int position) {
            return menu.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = View.inflate(parent.getContext(),
                        R.layout.list_cell_menu, null);
            }
            TextView tvLabel = (TextView) view.findViewById(R.id.tvLabel);
            TextView tvValue = (TextView) view.findViewById(R.id.tvValue);
            ImageView ivValue = (ImageView) view.findViewById(R.id.ivValue);
            String[] array = (String[]) getItem(position);
            tvLabel.setText(array[0]);
            try {
                ivValue.setVisibility(View.VISIBLE);
                tvValue.setVisibility(View.GONE);
                int res = Integer.parseInt(array[1]);
                ivValue.setImageResource(res);
            } catch (NumberFormatException e) {
                if (array[1].contains("http")) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ivValue.getLayoutParams();
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    params.width = (int) (80 * dm.density);
                    params.height = (int) (80 * dm.density);
                    ivValue.setLayoutParams(params);
                    imageLoader.displayImage(array[1], ivValue, Config.getDisplayOptions());
                } else {
                    ivValue.setVisibility(View.GONE);
                    tvValue.setVisibility(View.VISIBLE);
                    tvValue.setText(array[1]);
                }
            }

            return view;
        }
    }

    ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);

        getUserInfo();

        lvMenu = (ListView) findViewById(R.id.lvContent);
        adapter = new MenuAdapter();
        lvMenu.setAdapter(adapter);
        lvMenu.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO
            }
        });
        View btnBack = this.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) v.getContext()).finish();
            }
        });
    }

    private void getUserInfo() {
        if (menu == null) {
            menu = new SparseArray<String[]>();
        }
        menu.put(0, new String[]{"头像", Account.getInstance().getHead()});
        menu.put(1, new String[]{"用户名", Account.getInstance().getUsername()});
        menu.put(
                2,
                new String[]{"二维码名片", String.valueOf(R.drawable.ic_qrcode)});
        menu.put(3, new String[]{"性别", Account.getInstance().getSex()});
        menu.put(4, new String[]{"生日", Account.getInstance().getBirth()});
    }
}
