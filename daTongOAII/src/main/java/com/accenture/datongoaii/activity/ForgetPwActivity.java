package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.Question;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class ForgetPwActivity extends Activity implements OnClickListener {
    public static final int HANDLER_TAG_DISMISS_PROGRESS_DIALOG = 0;
    public static final int HANDLER_TAG_REFRESH_QUESTION = 1;
    public static final int GET_VERIFY_CODE_INVALID_SECONDS = 60;

    private LinearLayout layoutVerify;
    private EditText editCell;
    private Button btnNext;
    private View btnBack;
    private ProgressDialog progressDialog;
    private Question question;
    private String cell;
    private TextView tvQuestion;
    private EditText editAnswer;
    private Button btnSubmit;

    static class ActivityHandler extends Handler {
        WeakReference<ForgetPwActivity> mActivity;

        ActivityHandler(ForgetPwActivity activity) {
            mActivity = new WeakReference<ForgetPwActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ForgetPwActivity theActivity = mActivity.get();
            switch (msg.what) {
                case HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    if (theActivity.progressDialog != null) {
                        theActivity.progressDialog.dismiss();
                        theActivity.progressDialog = null;
                    }
                    break;
                case HANDLER_TAG_REFRESH_QUESTION:
                    View content = View.inflate(theActivity,
                            R.layout.activity_forget_pswd_step_2, null);
                    LayoutParams lp = (LayoutParams) theActivity.layoutVerify
                            .getLayoutParams();
                    theActivity.layoutVerify.removeAllViews();
                    theActivity.tvQuestion = (TextView) content.findViewById(R.id.tvQuestion);
                    theActivity.tvQuestion.setText(theActivity.question.text);
                    theActivity.editAnswer = (EditText) content
                            .findViewById(R.id.editAnswer);
                    theActivity.btnSubmit = (Button) content
                            .findViewById(R.id.btnSubmit);
                    theActivity.btnSubmit.setOnClickListener(theActivity);
                    theActivity.layoutVerify.addView(content, lp);
                    break;
            }
        }
    }

    private Handler handler = new ActivityHandler(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pswd_step_1);

        layoutVerify = (LinearLayout) findViewById(R.id.layoutVerify);
        editCell = (EditText) findViewById(R.id.editCell);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        btnNext.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                this.finish();
                break;
            case R.id.btnNext:
                if (isCellValid()) {
                    cell = editCell.getEditableText().toString().trim();
                    startGetQuestionConnect(cell);
                }
                break;
            case R.id.btnSubmit:
                if (isAnswerValid()) {
                    startLoginWithQuestionConnect(editAnswer.getEditableText()
                            .toString().trim());
                }
                break;
        }
    }

    private void startGetQuestionConnect(String cell) {
        progressDialog = ProgressDialog.show(this, null,
                Config.PROGRESS_Q_QUESTION);
        String url = Config.SERVER_HOST + "qPrevQuestion.json?cell=" + cell;
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (result != "fail") {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            question = Question
                                    .fromJSON(new JSONObject(result));
                            handler.sendEmptyMessage(HANDLER_TAG_REFRESH_QUESTION);
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

    private void startLoginWithQuestionConnect(String answer) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("cell", cell);
            obj.put("questionId", question.id);
            obj.put("answer", answer);
        } catch (JSONException e) {
            Logger.e("startLoginWithQuestionConnect", e.getMessage());
            return;
        }
        String url = Config.SERVER_HOST + "answer.json";
        new HttpConnection().post(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (result != "fail") {
                    try {
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            Account.getInstance().fromJson(new JSONObject(result));
                            finishAndReturn();
                        } else {
                            Logger.i("Login", "Failed!");
                            show(Intepreter.getCommonStatusFromJson(result).statusMsg);
                        }
                    } catch (JSONException e) {
                        Logger.e("Login", "Exception!");
                        show(Config.ERROR_INTERFACE);
                    }
                } else {
                    Logger.i("Login", "Network Error!");
                    show(Config.ERROR_NETWORK);
                }
                handler.sendEmptyMessage(HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
            }
        });
    }

    private boolean isCellValid() {
        if (editCell.getEditableText().toString().trim().length() == 0) {
            show(Config.NOTE_CELL_EMPTY);
            editCell.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isAnswerValid() {
        if (editAnswer.getEditableText().toString().trim().length() == 0) {
            show(Config.NOTE_ANSWER_EMPTY);
            editAnswer.requestFocus();
            return false;
        }
        return true;
    }

    protected void finishAndReturn() {
        setResult(RESULT_OK);
        this.finish();
    }

    public void show(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
