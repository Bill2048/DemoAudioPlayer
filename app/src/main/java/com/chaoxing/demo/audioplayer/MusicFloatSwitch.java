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
    }

    public void setup(boolean front) {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        int width = WindowManager.LayoutParams.MATCH_PARENT;
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

        WindowManager.LayoutParams wmLayoutParams = new WindowManager.LayoutParams(width, height, type, flags, PixelFormat.RGBA_8888);
        wmLayoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        windowManager.addView(this, wmLayoutParams);

        if (!front) {
            mContainer.setVisibility(View.GONE);
        }
    }

    public void show() {
        mContainer.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mContainer.setVisibility(View.GONE);
    }

}
