package com.accenture.datongoaii.widget;

import com.accenture.datongoaii.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class SectionListView extends RelativeLayout implements
		OnItemClickListener, OnClickListener, OnScrollListener {

	private SectionListAdapter mAdapter;
	private ListView mListView;
	private OnSectionItemClickedListener mOnSectionItemClickedListener;
	
	public interface OnSectionItemClickedListener {
		void onSectionItemClicked(SectionListView listView, View view,
				int section, int position);
	}

	public static abstract class SectionListAdapter extends BaseAdapter {
		public int getSectionCount() {
			return 0;
		}

		public int getSectionItemCount(int section) {
			return 0;
		}

		public View getSectionHeaderView(int section, View convertView,
				ViewGroup parent) {
			return null;
		}

		public View getSectionItemView(int section, int position,
				View convertView, ViewGroup parent) {
			return null;
		}

		public Object getItem(int section, int position) {
			return null;
		}

		public String getSectionLabel(int section) {
			return Integer.toString(section);
		}

		@Override
		public final boolean isEnabled(int position) {
			SectionPosition sectionPosition = getSectionPosition(position);
			return sectionPosition.mPosition >= 0;
		}

		private SectionPosition getSectionPosition(int position) {
			SectionPosition sectionPosition = new SectionPosition();
			int sectionCount = getSectionCount();
			int index = 0;
			for (int section = 0; section < sectionCount; section++) {
				if (index == position) {
					sectionPosition.mSection = section;
					sectionPosition.mPosition = -1;
					break;
				}
				index++;
				int itemCount = getSectionItemCount(section);
				if (position < index + itemCount) {
					sectionPosition.mSection = section;
					sectionPosition.mPosition = position - index;
					break;
				}
				index += itemCount;
			}
			return sectionPosition;
		}

		@Override
		public final int getCount() {
			int sectionCount = getSectionCount();
			int count = sectionCount;
			for (int section = 0; section < sectionCount; section++) {
				count += getSectionItemCount(section);
			}
			return count;
		}

		@Override
		public final Object getItem(int position) {
			return null;
		}

		@Override
		public final long getItemId(int position) {
			return position;
		}

		@Override
		public final View getView(int position, View convertView,
				ViewGroup parent) {
			SectionPosition sectionPosition = getSectionPosition(position);
			if (sectionPosition.mPosition < 0) {
				return getSectionHeaderView(sectionPosition.mSection, null,
						parent);
			}
			return getSectionItemView(sectionPosition.mSection,
					sectionPosition.mPosition, null, parent);
		}
	}

	private static class SectionPosition {
		public int mSection;
		public int mPosition;
	}

	public SectionListView(Context context) {
		super(context);
	}

	public SectionListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SectionListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onFinishInflate() {
		if (!isInEditMode()) {
			mListView = (ListView) findViewById(R.id.listView);
			mListView.setOnItemClickListener(this);
			mListView.setOnScrollListener(this);
		}
		super.onFinishInflate();
	}

	private final DataSetObserver mDataSetObserver = new DataSetObserver() {
		public void onChanged() {
			mCurrentSelectedSection = -1;
			updateButtonGroup();
			updateSelectedSection(mListView.getFirstVisiblePosition());
		}
	};

	public void setAdapter(SectionListAdapter adapter) {
		mCurrentSelectedSection = -1;
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataSetObserver);
		mListView.setAdapter(adapter);
	}

	public void setOnSectionItemClickedListener(
			OnSectionItemClickedListener listener) {
		this.mOnSectionItemClickedListener = listener;
	}

	private void updateButtonGroup() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.layoutBtnGroup);
		layout.removeAllViews();
		Context context = getContext();
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		int[] colors = new int[] { Color.GREEN, Color.GREEN, Color.DKGRAY };
		int[][] states = new int[3][];
		states[0] = new int[] { android.R.attr.state_pressed };
		states[1] = new int[] { android.R.attr.state_selected };
		states[2] = new int[] {};
		ColorStateList colorList = new ColorStateList(states, colors);
		int leftPading = layout.getWidth() / 3;
		int sectionCount = mAdapter.getSectionCount();
		for (int section = 0; section < sectionCount; section++) {
			Button btn = new Button(context);
			btn.setText(mAdapter.getSectionLabel(section));
			btn.setTextColor(Color.DKGRAY);
			btn.setPadding(leftPading, 0, 0, 0);
			btn.setLayoutParams(layoutParams);
			btn.setBackgroundResource(R.color.transparent);
			btn.setTextColor(colorList);
			btn.setTag(section);
			btn.setOnClickListener(this);
			layout.addView(btn);
		}
	}

	private void scrollToSection(int section) {
		int index = 0;
		for (int i = 0; i < section; i++) {
			index++;
			index += mAdapter.getSectionItemCount(i);
		}
		mListView.setSelection(index);
		updateSelectedSection(mListView.getFirstVisiblePosition());
	}

	@Override
	public void onClick(View v) {
		LinearLayout layout = (LinearLayout) findViewById(R.id.layoutBtnGroup);
		for (int i = 0; i < layout.getChildCount(); i++) {
			View view = layout.getChildAt(i);
			if (view instanceof Button) {
				((Button) view).setTextColor(Color.DKGRAY);
			}
		}
		if (v instanceof Button) {
			Integer integer = (Integer) v.getTag();
			scrollToSection(integer);
			((Button) v).setTextColor(Color.GREEN);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mOnSectionItemClickedListener != null
				&& position < mAdapter.getCount()) {
			SectionPosition sectionPosition = mAdapter
					.getSectionPosition(position);
			mOnSectionItemClickedListener.onSectionItemClicked(this, view,
					sectionPosition.mSection, sectionPosition.mPosition);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		updateSelectedSection(firstVisibleItem);
	}

	private int mCurrentSelectedSection = -1;

	private void updateSelectedSection(int position) {
		if (mAdapter == null) {
			return;
		}

		SectionPosition sectionPosition = mAdapter.getSectionPosition(position);
		if (mCurrentSelectedSection != sectionPosition.mSection) {
			mCurrentSelectedSection = sectionPosition.mSection;
			LinearLayout layout = (LinearLayout) findViewById(R.id.layoutBtnGroup);
			int sectionCount = mAdapter.getSectionCount();
			for (int section = 0; section < sectionCount; section++) {
				View view = layout.getChildAt(section);
				view.setSelected(section == mCurrentSelectedSection);
			}
		}
	}
}
