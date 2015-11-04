package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.DTOAIIApplication;
import com.accenture.datongoaii.DTOARequest;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;
import com.accenture.datongoaii.vendor.HX.HXController;
import com.accenture.datongoaii.vendor.HX.Utils.CommonUtils;
import com.accenture.datongoaii.vendor.qrscan.MyQRCodeActivity;
import com.accenture.datongoaii.widget.PopupDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyInfoActivity extends Activity implements OnItemClickListener, OnClickListener {
    private static final String TAG = "MyInfoActivity";

    private Context context;
    private Dialog selectPicDialog;
    private ProgressDialog progressDialog;
    private EditText etName;
    private View dialogView;
    private AlertDialog.Builder builder;

    private SparseArray<String[]> menu;
    private File cameraFile;
    private MenuAdapter adapter;
    private String mSex;
    private String mBirth;

    private Handler handler = new ActivityHandler(this);

    public static class ActivityHandler extends Handler {
        WeakReference<MyInfoActivity> mActivity;

        ActivityHandler(MyInfoActivity activity) {
            this.mActivity = new WeakReference<MyInfoActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG:
                    MyInfoActivity a = mActivity.get();
                    if (a.progressDialog.isShowing()) {
                        a.progressDialog.dismiss();
                        a.progressDialog = null;
                    }
            }
        }
    }

    private class MenuAdapter extends BaseAdapter {
        private ImageLoader imageLoader = ImageLoader.getInstance();

        @Override
        public int getCount() {
            return menu.size();
        }

        @Override
        public Object getItem(int position) {
            return menu.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = View.inflate(parent.getContext(),
                        R.layout.list_cell_menu, null);
            }
            TextView tvLabel = (TextView) view.findViewById(R.id.tvLabel);
            TextView tvValue = (TextView) view.findViewById(R.id.tvValue);
            ImageView ivValue = (ImageView) view.findViewById(R.id.ivValue);
            String[] array = (String[]) getItem(position);
            tvLabel.setText(array[0]);
            try {
                ivValue.setVisibility(View.VISIBLE);
                tvValue.setVisibility(View.GONE);
                int res = Integer.parseInt(array[1]);
                ivValue.setImageResource(res);
            } catch (NumberFormatException e) {
                if (array[1].contains("http")) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ivValue.getLayoutParams();
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    params.width = (int) (80 * dm.density);
                    params.height = (int) (80 * dm.density);
                    ivValue.setLayoutParams(params);
                    imageLoader.displayImage(array[1], ivValue, Config.getDisplayOptions());
                } else {
                    ivValue.setVisibility(View.GONE);
                    tvValue.setVisibility(View.VISIBLE);
                    tvValue.setText(array[1]);
                }
            }

            return view;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_my_info);

        getUserInfo();

        ListView lvMenu = (ListView) findViewById(R.id.lvContent);
        adapter = new MenuAdapter();
        lvMenu.setAdapter(adapter);
        lvMenu.setOnItemClickListener(this);
        View btnBack = this.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) v.getContext()).finish();
            }
        });

        View btnLogout = this.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle(null)
                        .setMessage(Config.ALERT_SWITCH_ACCOUNT)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startLogoutConnection();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_CAMERA) {
            // 发送照片
            if (cameraFile != null && cameraFile.exists())
                uploadPicture(cameraFile.getAbsolutePath());
        } else if (requestCode == Constants.REQUEST_CODE_LOCAL) { // 发送本地图片
            if (data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    sendPicByUri(selectedImage);
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                showSelectPicDialog();
                break;
            case 1:
                showRenameDialog();
                break;
            case 2:
                startQRCodeActivity();
                break;
            case 3:
                showModifySexDialog();
                break;
            case 4:
                showModifyBirthdayDialog();
                break;
            case 5:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCapture: {
                dismissPicDialog();
                selectPicFromCamera();
            }
            break;
            case R.id.btnSelect: {
                dismissPicDialog();
                selectPicFromLocal();
            }
            break;
            case R.id.rlMale:
            case R.id.btnMale: {
                mSex = "男";
                ((RadioButton) dialogView.findViewById(R.id.btnFemale)).setChecked(false);
            }
            break;
            case R.id.rlFemale:
            case R.id.btnFemale: {
                mSex = "女";
                ((RadioButton) dialogView.findViewById(R.id.btnMale)).setChecked(false);
            }
            break;
        }
    }

    private void showSelectPicDialog() {
        View view = View.inflate(context, R.layout.dialog_select_pic, null);
        view.findViewById(R.id.btnCapture).setOnClickListener(this);
        view.findViewById(R.id.btnSelect).setOnClickListener(this);
        selectPicDialog = PopupDialog.showPushDialogFromBottom(context, view);
    }

    private void dismissPicDialog() {
        if (selectPicDialog.isShowing()) {
            selectPicDialog.dismiss();
        }
    }

    private void showRenameDialog() {
        etName = new EditText(context);
        etName.setBackgroundColor(getResources().getColor(R.color.white));
        etName.setText(Account.getInstance().getUsername());
        if (builder == null) {
            builder = new AlertDialog.Builder(this);
            builder.setView(etName)
                    .setTitle("更改姓名")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            builder = null;
                        }
                    })
                    .setNeutralButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startRenameConnect(getNewName());
                            builder = null;
                        }
                    })
                    .show();
            builder.create();
        }
    }

    private void showModifySexDialog() {
        dialogView = View.inflate(context, R.layout.dialog_modify_user_sex, null);
        dialogView.findViewById(R.id.btnMale).setOnClickListener(this);
        dialogView.findViewById(R.id.btnFemale).setOnClickListener(this);
        dialogView.findViewById(R.id.rlMale).setOnClickListener(this);
        dialogView.findViewById(R.id.rlFemale).setOnClickListener(this);
        mSex = Account.getInstance().getSex();
        if (mSex.equals("男")) {
            ((RadioButton) dialogView.findViewById(R.id.btnMale)).setChecked(true);
            ((RadioButton) dialogView.findViewById(R.id.btnFemale)).setChecked(false);
        } else {
            ((RadioButton) dialogView.findViewById(R.id.btnMale)).setChecked(false);
            ((RadioButton) dialogView.findViewById(R.id.btnFemale)).setChecked(true);
        }
        if (builder == null) {
            builder = new AlertDialog.Builder(this);
            builder.setTitle("更改性别")
                    .setView(dialogView)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            builder = null;
                        }
                    })
                    .setNeutralButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startModifySexConnect(mSex);
                            builder = null;
                        }
                    }).show();
            builder.create();
        }
    }

    private void showModifyBirthdayDialog() {
        final DatePicker datePicker = new DatePicker(context);
        datePicker.setBackgroundColor(getResources().getColor(R.color.white));
        datePicker.setSpinnersShown(true);
        datePicker.setCalendarViewShown(false);
        int[] array = getBirthArray();
        datePicker.init(array[0], array[1] - 1, array[2], null);
        if (builder == null) {
            builder = new AlertDialog.Builder(this);
            builder.setTitle("更改生日")
                    .setView(datePicker)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            builder = null;
                        }
                    })
                    .setNeutralButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                            String birth = Utils.getDateString(calendar);
                            startModifyBirthdayConnect(birth);
                            builder = null;
                        }
                    }).show();
            builder.create();
        }
    }

    private void startQRCodeActivity() {
        Intent intent = new Intent(context, MyQRCodeActivity.class);
        startActivity(intent);
    }

    private int[] getBirthArray() {
        mBirth = Account.getInstance().getBirth();
        List<String> data = Utils.splitStrings(mBirth, "-");
        int[] array = new int[data.size()];
        for (int i = 0; i < data.size(); i++) {
            array[i] = Integer.parseInt(data.get(i));
        }
        return array;
    }

    private String getNewName() {
        return etName.getEditableText().toString().trim().replace(" ", "");
    }

    /**
     * 照相获取图片
     */
    private void selectPicFromCamera() {
        if (!CommonUtils.isExitsSdcard()) {
            Utils.toast(context, Config.NOTE_NO_STORAGE_CARD);
            return;
        }

        String fileName = Environment.getExternalStorageDirectory().getPath() + "/TEMP" + "/temp" + System.currentTimeMillis() + ".jpg";
        cameraFile = new File(fileName);
        cameraFile.getParentFile().mkdirs();
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
                Constants.REQUEST_CODE_CAMERA);
    }

    /**
     * 从图库获取图片
     */
    public void selectPicFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, Constants.REQUEST_CODE_LOCAL);
    }

    /**
     * 根据图库图片uri发送图片
     *
     * @param selectedImage 图库图片Uri
     */
    private void sendPicByUri(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        String st8 = Config.NOTE_NO_LOCAL_PIC;
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            if (picturePath == null || picturePath.equals("null")) {
                Utils.toast(context, st8);
                return;
            }
            uploadPicture(picturePath);
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                Utils.toast(context, st8);
                return;
            }
            uploadPicture(file.getAbsolutePath());
        }
    }

    private void getUserInfo() {
        if (menu == null) {
            menu = new SparseArray<String[]>();
        }
        menu.put(0, new String[]{"头像", Account.getInstance().getHead()});
        menu.put(1, new String[]{"用户名", Account.getInstance().getUsername()});
        menu.put(2, new String[]{"二维码名片", String.valueOf(R.drawable.ic_qrcode)});
        menu.put(3, new String[]{"性别", Account.getInstance().getSex()});
        menu.put(4, new String[]{"生日", Account.getInstance().getBirth()});
    }

    private void refresh() {
        getUserInfo();
        adapter.notifyDataSetChanged();
    }

    /**
     * 网络请求
     */
    private void uploadPicture(String path) {
        progressDialog = Utils.showProgressDialog(this, progressDialog, null, Config.PROGRESS_SUBMIT);
        DTOARequest.getInstance(context).uploadImage(path, new DTOARequest.RequestListener() {
            @Override
            public void callback(String result) {
                handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                Utils.toast(context, Config.SUCCESS_UPDATE);
                try {
                    String url = new JSONObject(result).getJSONObject("data").getString("url");
                    Account.getInstance().setHead(url);
                    refresh();
                } catch (JSONException e) {
                    Logger.e(TAG, "uploadPicture " + e.getMessage());
                }
            }
        });
    }

    private void startRenameConnect(final String name) {
        try {
            progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_SUBMIT);
            DTOARequest.getInstance(context).modifyUsername(Account.getInstance().getUserId(), name, new DTOARequest.RequestListener() {
                @Override
                public void callback(String result) {
                    handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                    Account.getInstance().setUsername(name);
                    refresh();
                }
            });
        } catch (JSONException e) {
            Logger.e(TAG, "startRenameConnect: " + e.getMessage());
            handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
        }
    }

    private void startModifySexConnect(final String sex) {
        try {
            progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_SUBMIT);
            DTOARequest.getInstance(context).modifyUserSex(Account.getInstance().getUserId(), sex, new DTOARequest.RequestListener() {
                @Override
                public void callback(String result) {
                    handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                    Account.getInstance().setSex(sex);
                    refresh();
                }
            });
        } catch (JSONException e) {
            Logger.e(TAG, "startModifySexConnect: " + e.getMessage());
            handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
        }
    }

    private void startModifyBirthdayConnect(final String birthday) {
        try {
            progressDialog = Utils.showProgressDialog(context, progressDialog, null, Config.PROGRESS_SUBMIT);
            DTOARequest.getInstance(context).modifyUserBirthday(Account.getInstance().getUserId(), birthday, new DTOARequest.RequestListener() {
                @Override
                public void callback(String result) {
                    handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                    mBirth = birthday;
                    Account.getInstance().setBirth(birthday);
                    refresh();
                }
            });
        } catch (JSONException e) {
            Logger.e(TAG, "startModifySexConnect: " + e.getMessage());
            handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
        }
    }

    private void startLogoutConnection() {
        String url = Config.SERVER_HOST + Config.URL_LOGOUT;
        progressDialog = Utils.showProgressDialog(this, progressDialog, null, Config.PROGRESS_LOGOUT);
        new HttpConnection().get(url, new HttpConnection.CallbackListener() {
                    @Override
                    public void callBack(String result) {
                        handler.sendEmptyMessage(Constants.HANDLER_TAG_DISMISS_PROGRESS_DIALOG);
                        if (!result.equals("fail")) {
                            try {
                                CommonResponse cr = Intepreter.getCommonStatusFromJson(result);
                                if (cr.statusCode == 0) {
                                    try {
                                        HXController.getInstance().startLogout();
                                    } catch (Exception e) {
                                        Logger.e(TAG, e.getMessage());
                                        Utils.toast(context, Config.ERROR_IM);
                                    }
                                    Utils.clearUserInfo(context);
                                    ((DTOAIIApplication) getApplicationContext()).restartApplication();
                                } else {
                                    Utils.toast(context, cr.statusMsg);
                                }
                            } catch (JSONException e) {
                                Utils.toast(context, Config.ERROR_INTERFACE);
                            }
                        } else {
                            Utils.toast(context, Config.ERROR_NETWORK);
                        }
                    }
                }

        );
    }
}
