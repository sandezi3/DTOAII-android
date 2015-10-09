package com.accenture.datongoaii.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Contact;

import java.util.List;

public class UserGridAdapter extends BaseAdapter {
    private Context context;
    private List<Contact> userList;
    private LayoutInflater inflater;

    public UserGridAdapter(Context context, List<Contact> list) {
        this.context = context;
        this.userList = list;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_cell_user, parent,
                    false);
            holder = new ViewHolder();
            holder.tvName = (TextView) convertView.findViewById(R.id.tvTitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contact c = userList.get(position);
        holder.tvName.setText(c.name);
        if (position == 0) {
            holder.tvName.setTextColor(context.getResources().getColor(R.color.gray_2));
        } else {
            holder.tvName.setTextColor(context.getResources().getColor(R.color.tab_text_focused));
        }

        return convertView;
    }

    public static class ViewHolder {
        TextView tvName;
    }
}
