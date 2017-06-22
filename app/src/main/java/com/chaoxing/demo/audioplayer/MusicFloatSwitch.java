package com.chaoxing.demo.audioplayer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * Created by HuWei on 2017/6/22.
 */

public class MusicFloatSwitch extends FrameLayout {

    private View mContainer;

    public MusicFloatSwitch(@NonNull Context context) {
        super(context);
        init();
    }

    public MusicFloatSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MusicFloatSwitch(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mContainer = LayoutInflater.from(getContext()).inflate(R.layout.music_float_switch, this, true);
        mContainer.setOnTouchListener(mOnTouchListener);
    }

    private WindowManager windowManager;

    public void setup(boolean front) {
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        int width = WindowManager.LayoutParams.WRAP_CONTENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;

        int flags = 0;

        // FLAG_NOT_TOUCH_MODAL:不拦截后面的事件
        // FLAG_NOT_FOCUSABLE:弹出的View收不到Back键的事件

        flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

//        flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        int type = WindowManager.LayoutParams.TYPE_PHONE;

        if (Build.VERSION.SDK_INT < 19) {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        } else if (Build.VERSION.SDK_INT < 25) {
            type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(width, height, type, flags, PixelFormat.RGBA_8888);
        lp.gravity = Gravity.TOP | Gravity.LEFT;
        lp.x = 0;
        lp.y = 0;
        windowManager.addView(this, lp);

        if (!front) {
            mContainer.setVisibility(View.GONE);
        }
    }

    private int originalX;
    private int originalY;
    private int offsetX;
    private int offsetY;

    private OnTouchListener mOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    int[] location = new int[2];
                    mContainer.getLocationOnScreen(location);
                    originalX = location[0];
                    originalY = location[1];
                    offsetX = (int) motionEvent.getRawX();
                    offsetY = (int) motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) motionEvent.getRawX() - offsetX;
                    int dy = (int) motionEvent.getRawY() - offsetY;
                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) mContainer.getLayoutParams();
                    originalX = originalX + dx;
                    originalY = originalY + dy;
                    params.x = originalX;
                    params.y = originalY;
                    windowManager.updateViewLayout(mContainer, params);
                    offsetX = (int) motionEvent.getRawX();
                    offsetY = (int) motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    public void show() {
        mContainer.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mContainer.setVisibility(View.GONE);
    }

}
