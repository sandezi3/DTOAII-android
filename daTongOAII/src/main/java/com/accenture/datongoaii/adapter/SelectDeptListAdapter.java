package com.accenture.datongoaii.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.SelectDeptActivity;
import com.accenture.datongoaii.model.Dept;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class SelectDeptListAdapter extends BaseAdapter {
    private static final int HANDLER_TAG_REFRESH_DATA = 0;
    private SelectDeptActivity activity;
    private List<Dept> deptList;
    private LayoutInflater inflater;
    private ImageLoader loader;

    public SelectDeptListAdapter(Context context, List<Dept> list) {
        this.activity = (SelectDeptActivity) context;
        this.deptList = list;
        this.inflater = LayoutInflater.from(context);
        loader = ImageLoader.getInstance();
    }

    private Handler handler = new Handler() {
        public void refreshWithSelected() {
            notifyDataSetChanged();
        }

        @Override
        public void handleMessage(android.os.Message message) {
            switch (message.what) {
                case HANDLER_TAG_REFRESH_DATA:
                    refreshWithSelected();
                    break;
            }
        }
    };

    public void refreshWithSelected() {
        Message msg = handler.obtainMessage(HANDLER_TAG_REFRESH_DATA);
        handler.sendMessage(msg);
    }

    @Override
    public int getCount() {
        return deptList.size();
    }

    @Override
    public Object getItem(int position) {
        return deptList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_cell_org, parent,
                    false);
            holder = new ViewHolder();
            holder.ivSelect = (ImageView) convertView.findViewById(R.id.ivSelect);
            holder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            holder.ivArrow = (ImageView) convertView.findViewById(R.id.ivArrow);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Object o = deptList.get(position);
        holder.ivSelect.setTag(o);
        Dept dept = (Dept) o;
        if (Dept.contains(activity.selectList, dept)) {
            holder.ivSelect.setImageResource(R.drawable.ic_selected);
        } else {
            holder.ivSelect.setImageResource(R.drawable.ic_unselected);
        }
        holder.ivSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onDeptSelect((Dept) v.getTag());
            }
        });
        holder.ivSelect.setTag(dept);
        if (dept.id.equals(activity.invalidDeptId)) {
            holder.ivSelect.setOnClickListener(null);
            convertView.setOnClickListener(null);
            convertView.setBackgroundColor(activity.getResources().getColor(R.color.gray_5));
        }
        if (dept.img != null && dept.img.length() > 0) {
            loader.displayImage(dept.img, holder.ivIcon, Config.getDisplayOptions());
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_contact_c);
        }
        holder.tvName.setText(dept.name);
        holder.ivArrow.setVisibility(View.VISIBLE);

        convertView.setTag(holder);
        return convertView;
    }

    public static class ViewHolder {
        ImageView ivSelect;
        ImageView ivIcon;
        TextView tvName;
        ImageView ivArrow;
    }
}
