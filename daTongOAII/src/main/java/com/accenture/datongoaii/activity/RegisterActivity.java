package com.accenture.datongoaii.activity;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Jsons.JsonRegister;
import com.accenture.datongoaii.model.Question;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Config;
import com.accenture.datongoaii.util.Constants;
import com.accenture.datongoaii.util.Intepreter;
import com.accenture.datongoaii.util.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener {
	public static final int HANDLER_TAG_DISMISS_PROGRESS_DIALOG = 0;
	public static final int HANDLER_TAG_REFRESH_SECOND_COUNT = 1;
	public static final int HANDLER_TAG_RESTORE_BUTTON_BEHAVIER = 2;
	public static final int HANDLER_TAG_GET_CODE_SUCCESS = 3;
	public static final int HANDLER_TAG_START_SELECT_QUESTION = 4;

	public static final int GET_VERIFY_CODE_INVALID_SECONDS = 60;

	private EditText editCell;
	private EditText editPassword;
	private EditText editVerifyCode;
	private EditText editAnswer;
	private Button btnGetVerifyCode;
	private Button btnRegister;
	private View btnBack;
	private ProgressDialog progressDialog;
	private View btnSelectQuestion;
	private TextView tVQuestion;
	private List<Question> questionList;
	private Question selectedQuestion;
	private Timer mTimer;
	private Integer count;

	static class RegisterActivityHandler extends Handler {
		WeakReference<RegisterActivity> mActivity;

		RegisterActivityHandler(RegisterActivity activity) {
			mActivity = new WeakReference<RegisterActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			RegisterActivity theActivity = mActivity.get();
			switch (msg.what) {
			case HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
				if (theActivity.progressDialog != null) {
					theActivity.progressDialog.dismiss();
					theActivity.progressDialog = null;
				}
				break;
			case HANDLER_TAG_REFRESH_SECOND_COUNT:
				theActivity.btnGetVerifyCode.setText("再获取" + "("
						+ (theActivity.count + 1) + ")");
				break;
			case HANDLER_TAG_RESTORE_BUTTON_BEHAVIER:
				theActivity.btnGetVerifyCode.setEnabled(true);
				theActivity.btnGetVerifyCode
						.setBackgroundResource(R.drawable.button);
				theActivity.btnGetVerifyCode.setText("获取验证码");
				break;
			case HANDLER_TAG_GET_CODE_SUCCESS:
				Toast.makeText(theActivity, Config.SUCCESS_GET_VERIFY_CODE,
						Toast.LENGTH_LONG).show();
				break;
			case HANDLER_TAG_START_SELECT_QUESTION:
				theActivity
						.startSelectQuestionActivity(theActivity.questionList);
				break;
			}
		}
	}

	private Handler handler = new RegisterActivityHandler(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		editCell = (EditText) findViewById(R.id.editCell);
		editPassword = (EditText) findViewById(R.id.editPassword);
		editVerifyCode = (EditText) findViewById(R.id.editVerifyCode);
		btnGetVerifyCode = (Button) findViewById(R.id.btnGetVerfyCode);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		btnBack = findViewById(R.id.btnBack);

		btnGetVerifyCode.setOnClickListener(this);
		btnRegister.setOnClickListener(this);
		btnBack.setOnClickListener(this);

		btnSelectQuestion = findViewById(R.id.rLSelectQuestion);
		tVQuestion = (TextView) findViewById(R.id.tVQuestion);
		btnSelectQuestion.setOnClickListener(this);
		editAnswer = (EditText) findViewById(R.id.editAnswer);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnGetVerfyCode:
			if (isCellValid()) {
				getVerifyCode(editCell.getEditableText().toString().trim());
			}
			break;
		case R.id.btnRegister:
			if (isDataValid()) {
				startRegisterConnection(editCell.getEditableText().toString()
						.trim(), editPassword.getEditableText().toString()
						.trim(), editVerifyCode.getEditableText().toString()
						.trim(), editAnswer.getEditableText().toString().trim());
			}
			break;
		case R.id.btnBack:
			this.finish();
			break;
		case R.id.rLSelectQuestion:
			if (questionList == null) {
				startGetQuestionsConnection();
			} else {
				handler.sendEmptyMessage(HANDLER_TAG_START_SELECT_QUESTION);
			}
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.REQUEST_CODE_SELECT_QUESTION
				&& resultCode == RESULT_OK) {
			// 取数据
			if (data.hasExtra(Constants.BUNDLE_TAG_SELECT_QUESTION)) {
				selectedQuestion = (Question) data
						.getSerializableExtra(Constants.BUNDLE_TAG_SELECT_QUESTION);
				tVQuestion.setText(selectedQuestion.text);
				tVQuestion.setTextSize(14);
			} else {
				selectedQuestion = null;
			}
		}
	}

	protected void startSelectQuestionActivity(List<Question> qList) {
		Intent intent = new Intent(RegisterActivity.this,
				SelectQuestionActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable(Constants.BUNDLE_TAG_SELECT_QUESTION,
				(Serializable) qList);
		intent.putExtras(bundle);
		startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_QUESTION);
	}

	private void startGetQuestionsConnection() {
		progressDialog = ProgressDialog.show(this, null,
				Config.PROGRESS_Q_QUESTION);
		String url = Config.SERVER_HOST + "qQuestions.json";
		new HttpConnection().get(url, new HttpConnection.CallbackListener() {
			@Override
			public void callBack(String result) {
				if (result != "fail") {
					try {
						if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
							questionList = Question
									.getListFromJSON(new JSONObject(result));
							handler.sendEmptyMessage(HANDLER_TAG_START_SELECT_QUESTION);
						} else {
							show(Intepreter.getCommonStatusFromJson(result).statusMsg);
						}
					} catch (Exception e) {
						show(Config.ERROR_INTERFACE);
					}
				} else {
					show(Config.ERROR_NETWORK);
				}
				handler.sendEmptyMessage(HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
			}
		});
	}

	private void getCodeBtnInvaidForSeconds() {
		count = GET_VERIFY_CODE_INVALID_SECONDS;
		if (mTimer == null) {
			mTimer = new Timer();
			setTimerTask();
		}
	}

	private void setTimerTask() {
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (count > 0) {
					count--;
					handler.sendEmptyMessage(HANDLER_TAG_REFRESH_SECOND_COUNT);
				} else {
					mTimer.cancel();
					mTimer = null;
					handler.sendEmptyMessage(HANDLER_TAG_RESTORE_BUTTON_BEHAVIER);
				}
			}
		}, 0, 1000 * 1);
	}

	private void getVerifyCode(String number) {
		String url = Config.SERVER_HOST + "getVerifyCode.json";
		new HttpConnection().get(url, new HttpConnection.CallbackListener() {
			@Override
			public void callBack(String result) {
				if (result != "fail") {
					try {
						if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
							handler.sendEmptyMessage(HANDLER_TAG_GET_CODE_SUCCESS);
							btnGetVerifyCode.setEnabled(false);
							btnGetVerifyCode
									.setBackgroundResource(R.drawable.button_disable);
							getCodeBtnInvaidForSeconds();
						} else {
							show(Intepreter.getCommonStatusFromJson(result).statusMsg);
						}
					} catch (Exception e) {
						e.printStackTrace();
						show(Config.ERROR_INTERFACE);
					}
				} else {
					show(Config.ERROR_NETWORK);
				}
			}
		});
	}

	private void startRegisterConnection(String cell, String password,
			String verifyCode, String answer) {
		progressDialog = ProgressDialog.show(this, null,
				Config.PROGRESS_REGISTER);
		String url = Config.SERVER_HOST + "register.json";
		JSONObject obj = new JSONObject();
		try {
			obj.put(JsonRegister.userId, cell);
			obj.put(JsonRegister.password, Utils.md5(password));
			obj.put(JsonRegister.code, verifyCode);
			obj.put(JsonRegister.questionId, selectedQuestion.id);
			obj.put(JsonRegister.answer, answer);
		} catch (JSONException e) {
			show(Config.ERROR_APP);
			e.printStackTrace();
			return;
		}
		HttpConnection connection = new HttpConnection();
		connection.post(url, obj, new HttpConnection.CallbackListener() {
			@Override
			public void callBack(String result) {
				if (result != "fail") {
					try {
						if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
							show(Config.SUCCESS_REGISTER);
							RegisterActivity.this.finish();
						} else {
							show(Intepreter.getCommonStatusFromJson(result).statusMsg);
						}
					} catch (Exception e) {
						e.printStackTrace();
						show(Config.ERROR_INTERFACE);
					}
				} else {
					show(Config.ERROR_NETWORK);
				}
				RegisterActivity.this.handler
						.sendEmptyMessage(HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
			}
		});
	}

	private boolean isCellValid() {
		if (editCell.getEditableText().toString().trim().length() == 0) {
			show(Config.NOTE_CELL_EMPTY);
			editCell.requestFocus();
			return false;
		} else if (editCell.getEditableText().toString().trim().length() != 11) {
			show(Config.NOTE_CELL_NUMBER);
			editCell.requestFocus();
			return false;
		} else {
			try {
				Long.parseLong(editCell.getEditableText().toString().trim());
			} catch (Exception e) {
				e.printStackTrace();
				editCell.requestFocus();
				show(Config.NOTE_CELL_FORMAT);
				editCell.requestFocus();
				return false;
			}
		}
		return true;
	}

	private boolean isDataValid() {
		if (!isCellValid()) {
			return false;
		}

		if (editPassword.getEditableText().toString().trim().length() == 0) {
			editPassword.requestFocus();
			show(Config.NOTE_PASSWORD_EMPTY);
			return false;
		}

		if (editVerifyCode.getEditableText().toString().trim().length() == 0) {
			editVerifyCode.requestFocus();
			show(Config.NOTE_VERIFY_CODE_EMPTY);
			return false;
		}

		if (selectedQuestion == null) {
			show(Config.NOTE_SELECT_QUESTION);
			return false;
		}

		if (editAnswer.getEditableText().toString().trim().length() == 0) {
			show(Config.NOTE_ANSWER_EMPTY);
			editAnswer.requestFocus();
			return false;
		}

		return true;
	}

	public void show(CharSequence msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

}
