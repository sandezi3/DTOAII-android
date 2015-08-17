package com.accenture.datongoaii.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.adapter.QuestionListAdapter;
import com.accenture.datongoaii.model.Question;
import com.accenture.datongoaii.util.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SelectQuestionActivity extends Activity implements
		OnItemClickListener, OnClickListener {
	private ListView lVQuestion;
	private QuestionListAdapter adapter;
	private Button btnComplete;
	private List<Question> questionList;

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_question);

		// 取数据
		if (this.getIntent().hasExtra(Constants.BUNDLE_TAG_SELECT_QUESTION)) {
			questionList = (List<Question>) getIntent().getSerializableExtra(
					Constants.BUNDLE_TAG_SELECT_QUESTION);
		} else {
			questionList = new ArrayList<Question>();
		}

		lVQuestion = (ListView) findViewById(R.id.lVQuestion);
		adapter = new QuestionListAdapter(this, questionList);
		lVQuestion.setAdapter(adapter);
		lVQuestion.setOnItemClickListener(this);
		btnComplete = (Button) findViewById(R.id.btnComplete);

		findViewById(R.id.btnBack).setOnClickListener(this);
		btnComplete.setOnClickListener(this);
		btnComplete.setEnabled(false);
		btnComplete.setBackgroundResource(R.drawable.button_disable);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnBack:
			this.finish();
			break;
		case R.id.btnComplete:
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putSerializable(Constants.BUNDLE_TAG_SELECT_QUESTION,
					findSelectedQuestion());
			intent.putExtras(bundle);
			this.setResult(RESULT_OK, intent);
			this.finish();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		for (Question q : questionList) {
			q.selected = false;
		}
		questionList.get(position).selected = true;
		adapter.notifyDataSetChanged();
		
		btnComplete.setEnabled(findSelectedQuestion() != null);
		if (btnComplete.isEnabled()) {
			btnComplete.setBackgroundResource(R.drawable.button_normal);
		} else {
			btnComplete.setBackgroundResource(R.drawable.button_disable);
		}
	}

	public void show(CharSequence msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	private Serializable findSelectedQuestion() {
		for (Question q : questionList) {
			if (q.selected) {
				return q;
			}
		}
		return null;
	}
}
