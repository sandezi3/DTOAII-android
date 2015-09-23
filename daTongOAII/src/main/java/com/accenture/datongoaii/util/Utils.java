package com.accenture.datongoaii.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Dept;

public class Utils {

    public static List<String> splitStrings(String str, String seperater) {
        List<String> list = new ArrayList<String>();
        if (str.length() > 0) {
            while (str.contains(seperater)) {
                int index = str.indexOf(seperater);
                String s = str.substring(0, index);
                list.add(s);
                str = str.substring(index + 1);
            }
            list.add(str);
        }
        return list;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getPeroid(String time, long now) throws ParseException {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = sdf.parse(time);
        c.setTime(d);
        long then = c.getTimeInMillis();
        long year = (now - then) / 1000 / 60 / 60 / 24 / 30 / 12;
        if (year > 0) {
            return year + "年前";
        }
        long month = (now - then) / 1000 / 60 / 60 / 24 / 30;
        if (month > 0) {
            return month + "个月前";
        }
        long day = (now - then) / 1000 / 60 / 60 / 24;
        if (day > 0) {
            return day + "天前";
        }
        long hour = (now - then) / 1000 / 60 / 60;
        if (hour > 0) {
            return hour + "小时前";
        }
        long minute = (now - then) / 1000 / 60;
        if (minute > 0) {
            return minute + "分钟前";
        }
        return "刚才";
    }

    @SuppressLint("SimpleDateFormat")
    public static String getCurrentTime() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return sDateFormat.format(new Date(System.currentTimeMillis()));
    }

    public static void closeSoftKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static String getDecodedString(String str) throws UnsupportedEncodingException {
        return URLDecoder.decode(str, "UTF-8");
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public static String base64(String string) {
        return new String(Base64.encode(string.getBytes(), Base64.DEFAULT));
    }

    public static boolean isValidCellNumber(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$|(17[0-9])");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    public static void toast(Context context, String string) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }

    public static ProgressDialog showProgressDialog(Context context, ProgressDialog dialog,
                                                    CharSequence title, CharSequence message) {
        if (dialog != null) {
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.show();
        } else {
            dialog = ProgressDialog.show(context, title, message);
        }
        return dialog;
    }

    public static int getScreenWidthDP(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
        return (int) (metric.widthPixels / metric.density + 0.5f); // 屏幕宽度（像素）
    }

    public static float getScreenDensity(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.density;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static Button createButton(Context context, Dept dept) {
        Button btn = new Button(context);
        btn.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        btn.setText(dept.name);
        btn.setPadding(8, 12, 8, 12);
        btn.setTextColor(context.getResources().getColor(R.color.gray_2));
        btn.setTag(dept);
        btn.setTextSize((float) 18);
        btn.setOnClickListener((View.OnClickListener) context);
        return btn;
    }

    public static void addButton(Context context, Dept dept, LinearLayout parent) {
        TextView sep = new TextView(context);
        sep.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        sep.setText(">");
        sep.setTextColor(context.getResources().getColor(R.color.gray_2));
        parent.addView(sep, 15, parent.getHeight());
        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (view instanceof Button) {
                ((Button) view).setTextColor(context.getResources().getColor(R.color.tab_text_focused));
            }
        }
        Button btn = createButton(context, dept);
        parent.addView(btn, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public static void removeButton(Context context, Button btn, LinearLayout parent) {
        Integer index = parent.indexOfChild(btn);
        for (int i = parent.getChildCount() - 1; i > index; i--) {
            parent.removeViewAt(i);
        }
        btn.setTextColor(context.getResources().getColor(R.color.gray_2));
    }

    public static void saveUserInfo(Context context, String token) {
        SharedPreferences sp = context.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("token", token);
        editor.commit();
    }

    public static String getUserInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        return sp.getString("token", "");
    }
}
