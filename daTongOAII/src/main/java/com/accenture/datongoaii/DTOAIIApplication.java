package com.accenture.datongoaii;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.vendor.HX.HXController;
import com.easemob.chat.EMChat;

import java.util.Iterator;
import java.util.List;

public class DTOAIIApplication extends Application {
    private static final String TAG = "DTOAIIApplication";

    public static Context applicationContext;
    public static HXController hxController;

    @Override
    public void onCreate() {
        super.onCreate();

        applicationContext = this;

        hxController = new HXController();
        hxController.onInit(applicationContext);
    }

    public void restartApplication() {
        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
