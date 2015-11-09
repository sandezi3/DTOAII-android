package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.DTOARequest;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.db.ContactDao;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Utils;
import com.accenture.datongoaii.vendor.HX.activity.ChatActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.ref.WeakReference;

/**
 * Created by leon on 10/3/15.
 * 用户详情
 */
public class ContactProfileActivity extends Activity {
    private ContactProfileActivity context;
    private Contact mContact;
    public ProgressDialog progressDialog;
    private Handler handler = new ActivityHandler(this);

    public static class ActivityHandler extends Handler {
        WeakReference<ContactProfileActivity> mActivity;

        public ActivityHandler(ContactProfileActivity activity) {
            mActivity = new WeakReference<ContactProfileActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ContactProfileActivity a = mActivity.get();
            switch (msg.what) {
                case Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG: {
                    if (a.progressDialog != null) {
                        a.progressDialog.dismiss();
                        a.progressDialog = null;
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_profile);
        context = this;

        ImageView ivHead = (ImageView) findViewById(R.id.ivHead);
        TextView tvName = (TextView) findViewById(R.id.tvUsername);
        TextView tvCell = (TextView) findViewById(R.id.tvCell);

        mContact = (Contact) getIntent().getSerializableExtra(Constants.BUNDLE_TAG_CONTACT_PROFILE);
        boolean isFromMyFriends = getIntent().getBooleanExtra(Constants.BUNDLE_TAG_CONTACT_PROFILE_IS_FROM_MY_FRIENDS, false);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(mContact.head, ivHead, Config.getDisplayOptions());
        tvName.setText(mContact.name);
        tvCell.setText(mContact.cell);

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (mContact.imId != null && mContact.imId.length() > 0) {
            findViewById(R.id.btnSendMessage).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("userId", mContact.imId);
                    intent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
                    startActivity(intent);
                    finish();
                }
            });
        } else {
            findViewById(R.id.btnSendMessage).setVisibility(View.INVISIBLE);
        }
        if (isFromMyFriends) {
            findViewById(R.id.btnDelete).setVisibility(View.VISIBLE);
            findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    AlertDialog ad = builder.setMessage(Config.ALERT_DELETE_FRIEND)
                            .setCancelable(false)
                            .setPositiveButton(getResources().getText(R.string.btn_delete), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    context.progressDialog = Utils.showProgressDialog(context, context.progressDialog, null, Config.PROGRESS_SUBMIT);
                                    DTOARequest.startDeleteFriend(mContact.id, new HttpConnection.CallbackListener() {
                                        @Override
                                        public void callBack(String result) {
                                            handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                                            ContactDao dao = new ContactDao(context);
                                            // 清数据库好友表
                                            dao.saveFriends(null);
                                            setResult(RESULT_OK);
                                            finish();
                                        }
                                    });
                                }
                            })
                            .setNegativeButton(getResources().getText(R.string.btn_cancel), null)
                            .create();
                    ad.show();
                }
            });
        } else {
            findViewById(R.id.btnDelete).setVisibility(View.GONE);
        }
    }
}
