package com.accenture.datongoaii.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.AppActivity;
import com.accenture.datongoaii.activity.DTOAActivity;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.App;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Dept;
import com.easemob.util.TimeInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String combineStrings(List<String> list, String sep) {
        String result = "";
        if (list == null || list.size() == 0) {
            return result;
        }
        for (String str : list) {
            result += str + sep;
        }
        return result.substring(0, result.length() - 1);
    }

    public static String combineStrings(Object[] array, String sep) {
        String result = "";
        if (array == null || 0 == array.length) {
            return result;
        }
        for (Object obj : array) {
            result += obj.toString() + sep;
        }
        return result.substring(0, result.length() - 1);
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

    public static String getPeroid(long time, long now) throws ParseException {
        long year = (now - time) / 1000 / 60 / 60 / 24 / 30 / 12;
        if (year > 0) {
            return year + "年前";
        }
        long month = (now - time) / 1000 / 60 / 60 / 24 / 30;
        if (month > 0) {
            return month + "个月前";
        }
        long day = (now - time) / 1000 / 60 / 60 / 24;
        if (day > 0) {
            return day + "天前";
        }
        long hour = (now - time) / 1000 / 60 / 60;
        if (hour > 0) {
            return hour + "小时前";
        }
        long minute = (now - time) / 1000 / 60;
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

    public static Button createButton(Context context, Contact contact) {
        Button btn = new Button(context);
        btn.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        btn.setText(contact.name);
        btn.setPadding(8, 12, 8, 12);
        btn.setTextColor(context.getResources().getColor(R.color.tab_text_focused));
        btn.setTag(contact);
        btn.setTextSize((float) 18);
        btn.setOnClickListener((View.OnClickListener) context);
        return btn;
    }

    public static void addButton(Context context, Contact contact, LinearLayout parent) {
        Button btn = createButton(context, contact);
        parent.addView(btn, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public static void removeButton(Context context, Button btn, LinearLayout parent) {
        Integer index = parent.indexOfChild(btn);
        for (int i = parent.getChildCount() - 1; i > index; i--) {
            parent.removeViewAt(i);
        }
        btn.setTextColor(context.getResources().getColor(R.color.gray_2));
    }

    public static Button findButtonInButtons(Dept dept, LinearLayout parent) {
        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            Object obj = parent.getChildAt(i).getTag();
            if (obj != null && obj instanceof Dept && obj.equals(dept)) {
                return (Button) parent.getChildAt(i);
            }
        }
        return null;
    }

    public static void saveUserInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("token", Account.getInstance().getToken());
        editor.putString("userId", String.valueOf(Account.getInstance().getUserId()));
        editor.putString("head", Account.getInstance().getHead());
        editor.putString("cell", Account.getInstance().getCell());
        editor.apply();
    }

    public static void clearUserInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    public static HashMap<String, String> getUserInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("token", sp.getString("token", ""));
        map.put("userId", sp.getString("userId", ""));
        map.put("head", sp.getString("head", ""));
        map.put("cell", sp.getString("cell", ""));
        return map;
    }

    public static String getTimestampString(Date messageDate) {
        Boolean isChinese = true;
        String format;

        long messageTime = messageDate.getTime();
        if (isSameDay(messageTime)) {
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(messageDate);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);

            format = "HH:mm";

            if (hour > 17) {
                if (isChinese) {
                    format = "晚上 hh:mm";
                }

            } else if (hour >= 0 && hour <= 6) {
                if (isChinese) {
                    format = "凌晨 hh:mm";
                }
            } else if (hour > 11 && hour <= 17) {
                if (isChinese) {
                    format = "下午 hh:mm";
                }

            } else {
                if (isChinese) {
                    format = "上午 hh:mm";
                }
            }
        } else if (isYesterday(messageTime)) {
            if (isChinese) {
                format = "昨天 HH:mm";
            } else {
                format = "MM-dd HH:mm";
            }

        } else {
            if (isChinese) {
                format = "M月d日 HH:mm";
            } else {
                format = "MM-dd HH:mm";
            }
        }

        if (isChinese) {
            return new SimpleDateFormat(format, Locale.CHINA).format(messageDate);
        } else {
            return new SimpleDateFormat(format, Locale.US).format(messageDate);
        }
    }

    private static boolean isSameDay(long inputTime) {

        TimeInfo tStartAndEndTime = getTodayStartAndEndTime();
        if (inputTime > tStartAndEndTime.getStartTime() && inputTime < tStartAndEndTime.getEndTime())
            return true;
        return false;
    }

    private static boolean isYesterday(long inputTime) {
        TimeInfo yStartAndEndTime = getYesterdayStartAndEndTime();
        if (inputTime > yStartAndEndTime.getStartTime() && inputTime < yStartAndEndTime.getEndTime())
            return true;
        return false;
    }

    public static TimeInfo getTodayStartAndEndTime() {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.HOUR_OF_DAY, 0);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar1.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar1.getTime();
        long startTime = startDate.getTime();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss S");

        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.HOUR_OF_DAY, 23);
        calendar2.set(Calendar.MINUTE, 59);
        calendar2.set(Calendar.SECOND, 59);
        calendar2.set(Calendar.MILLISECOND, 999);
        Date endDate = calendar2.getTime();
        long endTime = endDate.getTime();
        TimeInfo info = new TimeInfo();
        info.setStartTime(startTime);
        info.setEndTime(endTime);
        return info;
    }

    public static TimeInfo getYesterdayStartAndEndTime() {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.add(Calendar.DATE, -1);
        calendar1.set(Calendar.HOUR_OF_DAY, 0);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar1.set(Calendar.MILLISECOND, 0);

        Date startDate = calendar1.getTime();
        long startTime = startDate.getTime();

        Calendar calendar2 = Calendar.getInstance();
        calendar2.add(Calendar.DATE, -1);
        calendar2.set(Calendar.HOUR_OF_DAY, 23);
        calendar2.set(Calendar.MINUTE, 59);
        calendar2.set(Calendar.SECOND, 59);
        calendar2.set(Calendar.MILLISECOND, 999);
        Date endDate = calendar2.getTime();
        long endTime = endDate.getTime();
        TimeInfo info = new TimeInfo();
        info.setStartTime(startTime);
        info.setEndTime(endTime);
        return info;
    }

    private static final long INTERVAL_IN_MILLISECONDS = 30 * 1000;

    public static boolean isCloseEnough(long time1, long time2) {
        // long time1 = date1.getTime();
        // long time2 = date2.getTime();
        long delta = time1 - time2;
        if (delta < 0) {
            delta = -delta;
        }
        return delta < INTERVAL_IN_MILLISECONDS;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void initWebViewSettings(WebView webView) {
        //启用支持javascript
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
//        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSaveFormData(true);
        settings.setDomStorageEnabled(true);
//        settings.setAppCacheMaxSize(1024 * 1024 * 8);
//        String appCachePath = webView.getContext().getApplicationContext().getCacheDir().getAbsolutePath();
//        settings.setAppCachePath(appCachePath);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     *
     * @param angle  旋转角度
     * @param bitmap 图片
     * @return Bitmap
     */
    public static Bitmap rotateImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDateString(Calendar calendar) {
        Date date = new Date(calendar.getTimeInMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    public static String encodeUTF8(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, "UTF-8");
    }

    public static String decodeUTF8(String str) throws UnsupportedEncodingException {
        return URLDecoder.decode(str, "UTF-8");
    }

    public static void startActivity(Activity activity, Class<?> cls) {
        startActivity(activity, cls, null, null);
    }

    public static void startActivity(Activity activity, Class<?> cls, String key, Object value) {
        Context appContext = activity.getApplicationContext();
        Intent intent = new Intent(appContext, cls);
        initIntent(intent, key, value);
        appContext.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right_1, R.anim.slide_out_left_1);
    }

    public static void startActivityForResult(Activity activity, Class<?> cls, int requestCode) {
        startActivityForResult(activity, cls, null, null, requestCode);
    }

    public static void startActivityForResult(Activity activity, Class<?> cls, String key, Object value, int requestCode) {
        Intent intent = new Intent(activity, cls);
        initIntent(intent, key, value);
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(R.anim.slide_in_right_1, R.anim.slide_out_left_1);
    }

    private static void initIntent(Intent intent, String key, Object value) {
        if (value != null && key != null) {
            if (value instanceof String) {
                intent.putExtra(key, (String) value);
            } else if (value instanceof Integer) {
                intent.putExtra(key, ((Integer) value).intValue());
            } else if (value instanceof Boolean) {
                intent.putExtra(key, ((Boolean) value).booleanValue());
            } else if (value instanceof App) {
                intent.putExtra(key, (Serializable) value);
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }


}
