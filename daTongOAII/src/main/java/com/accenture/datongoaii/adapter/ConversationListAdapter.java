package com.accenture.datongoaii.adapter;

import android.content.Context;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Conversation;
import com.accenture.datongoaii.vendor.HX.Utils.SmileUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class ConversationListAdapter extends BaseAdapter {
    public List<Conversation> conversationList;
    private Context context;
    private LayoutInflater inflater;
    private ImageLoader loader;


    public ConversationListAdapter(Context context, List<Conversation> list) {
        this.context = context;
        this.conversationList = list;
        this.inflater = LayoutInflater.from(context);
        this.loader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return conversationList.size();
    }

    @Override
    public Object getItem(int position) {
        return conversationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_cell_conversation, parent,
                    false);
            holder = new ViewHolder();
            holder.ivHead = (ImageView) convertView.findViewById(R.id.ivHead);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            holder.tvCreate = (TextView) convertView.findViewById(R.id.tvCreate);
            holder.tvSummary = (TextView) convertView
                    .findViewById(R.id.tvSummary);
            holder.tvCount = (TextView) convertView
                    .findViewById(R.id.tvCount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Conversation c = conversationList.get(position);
        loader.displayImage(c.username, holder.ivHead, Config.getDisplayOptions());
        holder.tvName.setText(c.username);
        holder.tvCreate.setText(c.create);
        Spannable span = SmileUtils.getSmiledText(context, c.summary);
        holder.tvSummary.setText(span, TextView.BufferType.SPANNABLE);
        holder.tvCount.setText(c.unReadedCount);
        if (c.unReadedCount.equals("0")) {
            holder.tvCount.setVisibility(View.INVISIBLE);
        } else {
            holder.tvCount.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    public static class ViewHolder {
        ImageView ivHead;
        TextView tvName;
        TextView tvCount;
        TextView tvCreate;
        TextView tvSummary;
    }
}
