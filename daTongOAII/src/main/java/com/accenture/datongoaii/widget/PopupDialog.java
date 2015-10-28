package com.accenture.datongoaii.widget;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

import com.accenture.datongoaii.R;

public class PopupDialog {
    public static Dialog showPushDialogFromBottom(Context context, View contentView) {
        Dialog dialog = new Dialog(context, R.style.myDialog);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        int[] wh = getDeviceWH(context);
        windowParams.x = 0;
        windowParams.y = wh[1];
        window.setAttributes(windowParams);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(contentView);
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        dialog.show();
        return dialog;
    }

    public static int[] getDeviceWH(Context context) {
        int[] wh = new int[2];
        int w, h;
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        w = dm.widthPixels;
        h = dm.heightPixels;
        wh[0] = w;
        wh[1] = h;
        return wh;
    }
}
