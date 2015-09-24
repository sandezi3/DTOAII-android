package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.accenture.datongoaii.R;

public class ContactAddActivity extends Activity {
    private ListView lvMenu;
    private String[] menu = {"创建群组"};
    private MenuAdapter adapter;

    private class MenuAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return menu.length;
        }

        @Override
        public Object getItem(int position) {
            return menu[position];
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
            TextView tv = (TextView) view.findViewById(R.id.tvLabel);
            tv.setText((String) getItem(position));
            return view;
        }
    }

    ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_add);
        lvMenu = (ListView) findViewById(R.id.lvFunction);
        adapter = new MenuAdapter();
        lvMenu.setAdapter(adapter);
        lvMenu.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (menu[position].equals("创建群组")) {
                    Intent intent = new Intent(parent.getContext(), CreateGroupActivity.class);
                    parent.getContext().startActivity(intent);
                }
            }
        });
        View btnBack = this.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactAddActivity.this.finish();
            }
        });
    }
}
