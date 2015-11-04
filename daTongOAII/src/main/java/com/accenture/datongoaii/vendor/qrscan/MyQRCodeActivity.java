package com.accenture.datongoaii.vendor.qrscan;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.client.utils.URLEncodedUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.IntBuffer;
import java.util.Hashtable;

/**
 * Created by leon on 11/4/15.
 * Encoding QRCode
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class MyQRCodeActivity extends Activity {
    private static final String TAG = MyQRCodeActivity.class.getSimpleName();
    private ImageView ivQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_qrcode);

        ivQRCode = (ImageView) findViewById(R.id.ivQRCode);
        ImageView ivHead = (ImageView) findViewById(R.id.ivHead);
        TextView tvName = (TextView) findViewById(R.id.tvName);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(Account.getInstance().getHead(), ivHead, Config.getDisplayOptions());
        tvName.setText(Account.getInstance().getUsername());

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                drawQRCode();
            }
        }).start();
    }

    private void drawQRCode() {
        BitMatrix matrix;
        String name;
        try {
            name = Utils.encodeUTF8(Account.getInstance().getUsername());
        } catch (UnsupportedEncodingException e) {
            Logger.e(TAG, "encodeUTF8 " + e.getMessage());
            return;
        }
        String contents = Constants.BARCODE_PREFIX_TAG + name + "|" + Account.getInstance().getCell() + "|" + Account.getInstance().getHead();
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        try {
            matrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, 300, 300, hints);
        } catch (WriterException e) {
            Logger.e(TAG, "Encode" + e.getMessage());
            return;
        }
        String tempFilePath = Environment.getExternalStorageDirectory().getPath() + "/TEMP";
        String fileName = tempFilePath + "/temp" + System.currentTimeMillis() + ".jpg";
        File file = new File(tempFilePath);
        file.mkdirs();
        file = new File(fileName);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pie = new int[width * height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix.get(x, y)) {
                    pie[y * width + x] = 0xff000000;
                } else {
                    pie[y * width + x] = 0xffffffff;
                }
            }
        }
        if (file.exists()) {
            file.delete();
        }
        final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(IntBuffer.wrap(pie));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ivQRCode.setImageBitmap(bmp);
            }
        });

        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 100;
        OutputStream stream;
        try {
            stream = new FileOutputStream(fileName);
        } catch (FileNotFoundException e) {
            Logger.e(TAG, "FileOutputStream " + e.getMessage());
            return;
        }

        bmp.compress(format, quality, stream);
    }

}
