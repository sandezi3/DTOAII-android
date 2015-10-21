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
import com.accenture.datongoaii.model.App;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class AppGridAdapter extends BaseAdapter {
    private Context context;
    private List<App> appList;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;

    public AppGridAdapter(Context context, List<App> list) {
        this.context = context;
        this.appList = list;
        this.inflater = LayoutInflater.from(context);
        this.imageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public Object getItem(int position) {
        return appList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_cell_app, parent,
                    false);
            holder = new ViewHolder();
            holder.ivLogo = (ImageView) convertView.findViewById(R.id.lvLogo);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvTitle);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        App app = appList.get(position);
        holder.tvName.setText(app.appName);
        imageLoader.displayImage(app.logo, holder.ivLogo, Config.getDisplayOptions());
        return convertView;
    }

    public static class ViewHolder {
        ImageView ivLogo;
        TextView tvName;
    }
}
