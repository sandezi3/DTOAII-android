package com.accenture.datongoaii.adapter;

import java.text.ParseException;
import java.util.List;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Todo;
import com.accenture.datongoaii.util.Config;
import com.accenture.datongoaii.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TodoListAdapter extends BaseAdapter {
	private List<Todo> todoList;
	private LayoutInflater inflater;

	public TodoListAdapter(Context context, List<Todo> list) {
		this.todoList = list;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return todoList.size();
	}

	@Override
	public Object getItem(int position) {
		return todoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_cell_todo, parent,
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

		Todo t = todoList.get(position);
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
