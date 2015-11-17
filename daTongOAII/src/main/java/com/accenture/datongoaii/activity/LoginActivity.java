package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.db.DBHelper;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;
import com.accenture.datongoaii.vendor.HX.HXController;
import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class LoginActivity extends Activity implements OnClickListener {
    private final static String TAG = "LoginActivity";
    private Activity activity;
    private EditText editPhoneNumber;
    private EditText editPassword;
    private RoundedImageView ivHead;
    private Animation anim;

    private ProgressDialog progressDialog;

    private Handler handler = new ClearProgressDialogHandler(this);

    static class ClearProgressDialogHandler extends Handler {
        WeakReference<LoginActivity> mActivity;

        ClearProgressDialogHandler(LoginActivity activity) {
            mActivity = new WeakReference<LoginActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 0:
                    if (theActivity.progressDialog != null) {
                        theActivity.progressDialog.dismiss();
                        theActivity.progressDialog = null;
                    }
                    break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_login);

        // 初始化ImageLoader
        File cacheDir = StorageUtils.getOwnCacheDirectory(getApplicationContext(), "imageloader/Cache");
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .diskCacheSize(50 * 1024 * 1024)
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheFileCount(100)
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .imageDownloader(
                        new BaseImageDownloader(this, 5 * 1000, 30 * 1000)) // connectTimeout
                        // (5
                        // s),
                        // readTimeout
                        // (30
                        // s)超时时间
                .build();
        ImageLoader.getInstance().init(config);

        editPhoneNumber = (EditText) findViewById(R.id.editCell);
        editPassword = (EditText) findViewById(R.id.editPassword);
        TextView tVRegister = (TextView) findViewById(R.id.tVRegister);
        TextView tVForgetPswd = (TextView) findViewById(R.id.tVForgetPswd);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        ivHead = (RoundedImageView) findViewById(R.id.ivRoundHead);

        tVRegister.setOnClickListener(this);
        tVForgetPswd.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

        ImageLoader imageLoader = ImageLoader.getInstance();
        HashMap<String, String> map = Utils.getUserInfo(this);
        if (map != null && map.size() > 0 && map.get("head") != null && map.get("head").length() > 0) {
            ivHead.setCornerRadius(R.dimen.radius);
            ivHead.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageLoader.displayImage(map.get("head"), ivHead, Config.getDisplayOptions());
            editPhoneNumber.setText(map.get("cell"));
        } else {
            ivHead.setCornerRadius(0.0f);
            ivHead.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageLoader.displayImage("", ivHead, Config.getDisplayOptions());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Config.DEBUG_AUTO_LOGIN) {
            Utils.closeSoftKeyboard(this, null);
            String token = Utils.getUserInfo(this).get("token");
            if (token != null && token.length() > 0) {
                hideContent();
                startLoginConnect(token);
            }
        }
    }

    @Override
    public void onClick(View v) {
        Utils.closeSoftKeyboard(this, v);
        switch (v.getId()) {
            case R.id.btnBack:
                this.finish();
                break;
            case R.id.tVRegister: {
                Intent intent = new Intent(activity, CellIdentifyActivity.class);
                intent.putExtra(Constants.BUNDLE_TAG_FUNCTION, Constants.FUNCTION_TAG_REGISTER);
                this.startActivityForResult(intent, Constants.REQUEST_CODE_FORGET_PW_LOGIN);
            }
            break;
            case R.id.btnLogin:
                if (isDataValid()) {
                    Utils.closeSoftKeyboard(this, v);
                    hideContent();
                    startLoginConnect(editPhoneNumber.getEditableText().toString()
                            .trim(), editPassword.getEditableText().toString()
                            .trim());
                }
                break;
            case R.id.tVForgetPswd: {
                Intent intent = new Intent(activity, CellIdentifyActivity.class);
                intent.putExtra(Constants.BUNDLE_TAG_FUNCTION, Constants.FUNCTION_TAG_FORGET_PASSWORD);
                this.startActivityForResult(intent, Constants.REQUEST_CODE_FORGET_PW_LOGIN);
            }
            break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_FORGET_PW_LOGIN
                && resultCode == RESULT_OK) {
            finishAndReturn();
        }
    }

    private void hideContent() {
        findViewById(R.id.rrContent).setVisibility(View.INVISIBLE);
        findViewById(R.id.btnLogin).setVisibility(View.INVISIBLE);
        findViewById(R.id.tVForgetPswd).setVisibility(View.INVISIBLE);
        findViewById(R.id.tVRegister).setVisibility(View.INVISIBLE);

        anim = new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, -1.0f);
        anim.setDuration(3000);
        anim.setFillAfter(true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ivHead.startAnimation(anim);
            }
        });
    }

    private void showContent() {
        findViewById(R.id.rrContent).setVisibility(View.VISIBLE);
        findViewById(R.id.btnLogin).setVisibility(View.VISIBLE);
        findViewById(R.id.tVForgetPswd).setVisibility(View.VISIBLE);
        findViewById(R.id.tVRegister).setVisibility(View.VISIBLE);

        anim.cancel();
    }

    private boolean isDataValid() {
        if (editPhoneNumber.getEditableText().toString().trim().length() == 0) {
            Utils.toast(activity, Config.NOTE_CELL_EMPTY);
            return false;
        }
        if (editPassword.getEditableText().toString().trim().length() == 0) {
            editPassword.requestFocus();
            Utils.toast(activity, Config.NOTE_PASSWORD_EMPTY);
            return false;
        }
        return true;
    }

    private void resolveLoginSuccess(String result) throws JSONException {
        Logger.w(TAG, "resolveLoginSuccess" + Thread.currentThread().getName());
        Account.getInstance().fromJson(new JSONObject(result));
        String imId = Account.getInstance().getImId();
        if (imId != null && imId.length() > 0) {
            HXController.getInstance().startLogin(imId);
        }
        // 不同用户登录，清数据库
        try {
            if (!Utils.getUserInfo(activity).get("userId").equals(Account.getInstance().getUserId().toString())) {
                clearDB();
            }
        } catch (Exception e) {
            Logger.e(TAG, "resolveLoginSuccess" + e.getMessage());
        }

        Utils.saveUserInfo(activity);
        finishAndReturn();
    }

    private void clearDB() {
        DBHelper.getInstance(getApplicationContext()).clearDataBase();
    }

    private void startLoginConnect(String token) {
        progressDialog = Utils.showProgressDialog(this, progressDialog, null, Config.PROGRESS_LOGIN);
        String url = Config.SERVER_HOST + Config.URL_AUTO_LOGIN.replace("{token}", token);
        HttpConnection connection = new HttpConnection();
        connection.get(url, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(final String result) {
                handler.sendEmptyMessage(0);

                if (!result.equals("fail")) {
                    try {
                        Logger.w(TAG, "startLoginConnect res:" + Thread.currentThread().getName());
                        if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                            resolveLoginSuccess(result);
                            return;
                        } else {
                            Logger.i(TAG, "Failed!");
                            Utils.toast(activity, Intepreter.getCommonStatusFromJson(result).statusMsg);
                        }
                    } catch (JSONException e) {
                        Logger.e(TAG, "Exception!");
                        Utils.toast(activity, Config.ERROR_INTERFACE);
                    }
                } else {
                    Logger.i(TAG, "Network Error!");
                    Utils.toast(activity, Config.ERROR_NETWORK);
                }
                showContent();
            }

        });
    }

    private void startLoginConnect(final String userId, final String password) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, null, Config.PROGRESS_LOGIN);
        }
        String url = Config.SERVER_HOST + Config.URL_LOGIN;
        Logger.i("startLoginConnect", url);
        JSONObject obj = new JSONObject();
        try {
            obj.put("cell", userId);
            obj.put("password", Utils.md5(password));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Logger.i("startLoginConnect", obj.toString());
        HttpConnection connection = new HttpConnection();
        connection.post(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(final String result) {
                handler.sendEmptyMessage(0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!result.equals("fail")) {
                            try {
                                if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
                                    resolveLoginSuccess(result);
                                    return;
                                } else {
                                    Logger.i(TAG, "Failed!");
                                    Utils.toast(activity, Intepreter.getCommonStatusFromJson(result).statusMsg);
                                }
                            } catch (JSONException e) {
                                Logger.e(TAG, "Exception!");
                                Utils.toast(activity, Config.ERROR_INTERFACE);
                            }
                        } else {
                            Logger.i(TAG, "Network Error!");
                            Utils.toast(activity, Config.ERROR_NETWORK);
                        }
                        showContent();
                    }
                }).start();
            }
        });
    }

    protected void finishAndReturn() {
        Logger.w(TAG, "finishAndReturn" + Thread.currentThread().getName());
        setResult(RESULT_OK);
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        this.finish();
    }
}
