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
import com.accenture.datongoaii.model.Noti;
import com.accenture.datongoaii.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.util.List;

public class NotiListAdapter extends BaseAdapter {
    private List<Noti> notiList;
    private LayoutInflater inflater;

    public NotiListAdapter(Context context, List<Noti> list) {
        this.notiList = list;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return notiList.size();
    }

    @Override
    public Object getItem(int position) {
        return notiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_cell_noti, parent,
                    false);
            holder = new ViewHolder();
            holder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            holder.tvDeadline = (TextView) convertView
                    .findViewById(R.id.tvDeadline);
            holder.tvCreate = (TextView) convertView
                    .findViewById(R.id.tvCreate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Noti t = notiList.get(position);
        ImageLoader loader = ImageLoader.getInstance();
        loader.displayImage(t.img, holder.ivIcon, Config.getDisplayOptions());
        holder.tvTitle.setText(t.title);
        try {
            holder.tvCreate.setText(Utils.getPeroid(t.create,
                    System.currentTimeMillis()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvDeadline.setText(t.deadline);

        return convertView;
    }

    public static class ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvDeadline;
        TextView tvCreate;
    }
}
