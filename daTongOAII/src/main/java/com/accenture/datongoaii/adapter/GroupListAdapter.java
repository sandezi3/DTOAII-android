package com.accenture.datongoaii.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Group;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class GroupListAdapter extends BaseAdapter {
    private Context context;
    private List<Group> groupList;
    private LayoutInflater inflater;
    private ImageLoader loader;

    public GroupListAdapter(Context context, List<Group> list) {
        this.context = context;
        groupList = list;
        inflater = LayoutInflater.from(context);
        loader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return groupList.size();
    }

    @Override
    public Object getItem(int position) {
        return groupList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_cell_group, parent,
                    false);
            holder = new ViewHolder();
            holder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            holder.tvMemberCount = (TextView) convertView
                    .findViewById(R.id.tvMemberCount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Group t = groupList.get(position);
        loader.displayImage(t.img, holder.ivIcon, Config.getDisplayOptions());
        holder.tvName.setText(t.name);
        holder.tvMemberCount.setText(String.valueOf(t.contactList.size()));

        return convertView;
    }

    public static class ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvMemberCount;
    }
}
