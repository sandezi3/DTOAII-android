/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.accenture.datongoaii.vendor.qrscan;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.vendor.qrscan.camera.CameraManager;
import com.google.zxing.ResultPoint;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {
    private static final long ANIMATION_DELAY = 80L;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int POINT_SIZE = 6;

    private CameraManager cameraManager;
    private final Paint paint;
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    private final int frameColor;
    private final int cornerColor;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every
        // time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.qrscan_viewfinder_mask);
        resultColor = resources.getColor(R.color.qrscan_result_view);
        frameColor = resources.getColor(R.color.qrscan_viewfinder_frame);
        cornerColor = resources.getColor(R.color.qrscan_viewfinder_corner);
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }
        int width = getWidth();
        int height = getHeight();
        Rect frame = cameraManager.getFramingRect(width, height);
        if (frame == null) {
            return;
        }

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
        canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
        canvas.drawRect(0, frame.bottom, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            paint.setColor(frameColor);
            int frameLineWidth = 2;
            canvas.drawRect(frame.left, frame.top, frame.right - frameLineWidth, frame.top + frameLineWidth, paint);
            canvas.drawRect(frame.left, frame.top + frameLineWidth, frame.left + frameLineWidth, frame.bottom
                    - frameLineWidth, paint);
            canvas.drawRect(frame.left, frame.bottom - frameLineWidth, frame.right - frameLineWidth, frame.bottom,
                    paint);
            canvas.drawRect(frame.right - frameLineWidth, frame.top, frame.right, frame.bottom, paint);

            // int time = (int) (System.currentTimeMillis() % 500);
            paint.setColor(cornerColor);
            // paint.setAlpha(Math.abs(time - 250));
            int cornerLength = frame.width() / 8;
            int cornerLineWidth = 3;
            // top left
            canvas.drawRect(frame.left, frame.top, frame.left + cornerLength, frame.top + cornerLineWidth, paint);
            canvas.drawRect(frame.left, frame.top, frame.left + cornerLineWidth, frame.top + cornerLength, paint);
            // top right
            canvas.drawRect(frame.right - cornerLength, frame.top, frame.right, frame.top + cornerLineWidth, paint);
            canvas.drawRect(frame.right - cornerLineWidth, frame.top, frame.right, frame.top + cornerLength, paint);
            // bottom left
            canvas.drawRect(frame.left, frame.bottom - cornerLength, frame.left + cornerLineWidth, frame.bottom, paint);
            canvas.drawRect(frame.left, frame.bottom - cornerLineWidth, frame.left + cornerLength, frame.bottom, paint);
            // bottom right
            canvas.drawRect(frame.right - cornerLineWidth, frame.bottom - cornerLength, frame.right, frame.bottom,
                    paint);
            canvas.drawRect(frame.right - cornerLength, frame.bottom - cornerLineWidth, frame.right, frame.bottom,
                    paint);

            // Request another update at the animation interval, but only
            // repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(ANIMATION_DELAY, frame.left - POINT_SIZE, frame.top - POINT_SIZE, frame.right
                    + POINT_SIZE, frame.bottom + POINT_SIZE);
        }
    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live
     * scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
    }

}
