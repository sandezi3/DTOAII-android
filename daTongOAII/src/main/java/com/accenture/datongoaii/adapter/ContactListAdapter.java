package com.accenture.datongoaii.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.model.Org;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class ContactListAdapter extends BaseAdapter {
    private Context context;
    private List<Object> dataList;
    private LayoutInflater inflater;
    private ImageLoader loader;
    public boolean isManageMode;

    public ContactListAdapter(Context context, List<Object> list) {
        this.context = context;
        this.dataList = list;
        this.inflater = LayoutInflater.from(context);
        this.isManageMode = false;
        loader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_cell_contact_1, parent,
                    false);
            holder = new ViewHolder();
            holder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            holder.ivArrow = (ImageView) convertView.findViewById(R.id.ivArrow);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Object o = dataList.get(position);
        if (o instanceof Org) {
            Org org = (Org) o;
            if (org.logo != null && org.logo.length() > 0) {
                loader.displayImage(org.logo, holder.ivIcon, Config.getDisplayOptions());
            } else {
                holder.ivIcon.setImageResource(R.drawable.ic_contact_c);
            }
            holder.tvName.setText(org.orgName);
            holder.ivArrow.setVisibility(View.VISIBLE);
        } else if (o instanceof Dept) {
            Dept dept = (Dept) o;
            if (dept.img != null && dept.img.length() > 0) {
                loader.displayImage(dept.img, holder.ivIcon, Config.getDisplayOptions());
            } else {
                holder.ivIcon.setImageResource(R.drawable.ic_contact_c);
            }
            holder.tvName.setText(dept.name);
            holder.ivArrow.setVisibility(View.VISIBLE);
        } else if (o instanceof Contact) {
            Contact contact = (Contact) o;
            Button btnEdit = (Button) convertView.findViewById(R.id.btnEdit);
            if (isManageMode) {
                btnEdit.setVisibility(View.VISIBLE);
                btnEdit.setTag(contact);
                btnEdit.setOnClickListener((View.OnClickListener) context);
            } else {
                btnEdit.setVisibility(View.INVISIBLE);
            }
            if (contact.head != null && contact.head.length() > 0) {
                loader.displayImage(contact.head, holder.ivIcon, Config.getDisplayOptions());
            } else {
                holder.ivIcon.setImageResource(R.drawable.ic_contact_p);
            }
            holder.tvName.setText(contact.name);
            holder.ivArrow.setVisibility(View.INVISIBLE);
        }

        convertView.setTag(convertView.getId(), o);
        return convertView;
    }

    public static class ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        ImageView ivArrow;
    }
}
