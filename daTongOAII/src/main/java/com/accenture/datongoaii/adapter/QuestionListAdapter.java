package com.accenture.datongoaii.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Question;

import java.util.List;

public class QuestionListAdapter extends ArrayAdapter<Question> {
    private LayoutInflater inflater;

    public QuestionListAdapter(Context context, List<Question> objects) {
        super(context, R.layout.list_cell_select_question, objects);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_cell_select_question, null);
            holder = new ViewHolder();
            holder.tVName = (TextView) convertView.findViewById(R.id.tVName);
            holder.iVCheck = (ImageView) convertView.findViewById(R.id.iVCheck);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Question q = getItem(position);
        holder.tVName.setText(q.text);
        if (q.selected) {
            holder.iVCheck.setVisibility(View.VISIBLE);
        } else {
            holder.iVCheck.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    public static class ViewHolder {
        TextView tVName;
        ImageView iVCheck;
    }
}
