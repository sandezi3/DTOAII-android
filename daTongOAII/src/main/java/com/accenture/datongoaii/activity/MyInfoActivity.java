package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.DTOAIIApplication;
import com.accenture.datongoaii.Intepreter;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;
import com.accenture.datongoaii.vendor.HX.HXController;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;

import java.lang.ref.WeakReference;

public class MyInfoActivity extends Activity {
    private static final String TAG = "MyInfoActivity";

    private Context context;
    private SparseArray<String[]> menu;
    private ProgressDialog progressDialog;
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
        MenuAdapter adapter = new MenuAdapter();
        lvMenu.setAdapter(adapter);
        lvMenu.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO
            }
        });
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

    private void getUserInfo() {
        if (menu == null) {
            menu = new SparseArray<String[]>();
        }
        menu.put(0, new String[]{"头像", Account.getInstance().getHead()});
        menu.put(1, new String[]{"用户名", Account.getInstance().getUsername()});
        menu.put(
                2,
                new String[]{"二维码名片", String.valueOf(R.drawable.ic_qrcode)});
        menu.put(3, new String[]{"性别", Account.getInstance().getSex()});
        menu.put(4, new String[]{"生日", Account.getInstance().getBirth()});
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
                                    Utils.saveUserInfo(context, "");
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
