package com.hayvn.hayvnapp.Helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hayvn.hayvnapp.Activities.PhotoshopActivity;

import java.util.ArrayList;

public class SurfaceViewExt extends SurfaceView {

    private final String TAG = "SURFACE";
    private SurfaceHolder surfaceHolder;
    PhotoshopActivity mainActivity;
    Bitmap src_bmp;
    Bitmap out_bmp;
    Bitmap scaledBitmap;
    int offset_x = 0;
    int offset_y = 0;
    int original_width = 0;
    int screen_width = 0;
    ArrayList<Integer> xs = new ArrayList<>();
    ArrayList<Integer> ys = new ArrayList<>();
    float mScaleFactor = 1.1f;
    private ScaleGestureDetector mScaleDetector;

    public SurfaceViewExt(Context context) {
        super(context);
        init(context);
    }

    public SurfaceViewExt(Context context,
                          AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SurfaceViewExt(Context context,
                          AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public SurfaceViewExt(Context context,
                          Bitmap src_bmp,
                          Bitmap original_bmp) {
        super(context);
        init(context);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        this.out_bmp = src_bmp;
        this.src_bmp = original_bmp;
        original_width = out_bmp.getWidth();

    }

    private void init(Context c) {
        mainActivity = (PhotoshopActivity) c;
        surfaceHolder = getHolder();


        surfaceHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //
                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
                mScaleFactor = Math.min(900 / (float) out_bmp.getHeight(), 900 / (float) out_bmp.getWidth());
                scaledBitmap = Bitmap.createScaledBitmap(out_bmp, (int) (out_bmp.getWidth() * mScaleFactor),
                        (int) (out_bmp.getHeight() * mScaleFactor), false);
                draw_bitmap();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder,
                                       int format, int width, int height) {
                //
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                //
            }
        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager()
                                .getDefaultDisplay()
                                .getMetrics(displayMetrics);
        screen_width = displayMetrics.widthPixels;
    }

    public void draw_bitmap() {
        Canvas canvas;
        try {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);
            canvas.drawBitmap(scaledBitmap, offset_x, offset_y, null);
            surfaceHolder.unlockCanvasAndPost(canvas);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

    }

    public int start_x = 0;
    public int start_y = 0;

    public void start_move(int x, int y) {
        start_x = x;
        start_y = y;
    }

    public void continue_move(int x, int y) {
        offset_x += (x - start_x);
        offset_y += (y - start_y);
        draw_bitmap();
        start_x = x;
        start_y = y;
    }

    boolean was_scaled = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int touched_x = (int) event.getX();
        int touched_y = (int) event.getY();
        int pointCount = event.getPointerCount();
        int action = event.getAction();
        String parentAction = mainActivity.action;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                start_move(touched_x, touched_y);
                was_scaled = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointCount > 1) {
                    mScaleDetector.onTouchEvent(event);
                    was_scaled = true;
                    break;
                } else {
                    if (was_scaled) {
                        was_scaled = false;
                    } else {
                        continue_move(touched_x, touched_y);
                    }
                    break;
                }
            case MotionEvent.ACTION_UP:
                break;
            }

        return true; //processed
    }
    public Bitmap get_current_Bitmap() {
        return (out_bmp);
    }

    public float latest_scale = 1.0f;
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            latest_scale = mScaleFactor;
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.2f, Math.min(mScaleFactor, 10.0f));

            if(latest_scale > mScaleFactor){
                scaledBitmap = Bitmap.createScaledBitmap(scaledBitmap, (int) (scaledBitmap.getWidth() * mScaleFactor / latest_scale),
                        (int) (scaledBitmap.getHeight() * mScaleFactor / latest_scale), false);
            }else{
                scaledBitmap = Bitmap.createScaledBitmap(out_bmp, (int) (out_bmp.getWidth() * mScaleFactor),
                        (int) (out_bmp.getHeight() * mScaleFactor), false);
            }
            invalidate();
            draw_bitmap();
            return true;
        }
    }


}