package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.accenture.datongoaii.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

public class SplashActivity extends Activity {
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivty_splash);

        dialog = ProgressDialog.show(this, null, "程序正在启动，请稍候...", true, false);

        // 初始化ImageLoader
        File cacheDir = StorageUtils.getOwnCacheDirectory(
                getApplicationContext(), "imageloader/Cache");

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

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent loginIntent = new Intent(SplashActivity.this,
                        LoginActivity.class);
                SplashActivity.this.startActivity(loginIntent);
                dialog.dismiss();
                SplashActivity.this.finish();
            }

        }, 3000);
    }
}
