package com.chaoxing.demo.audioplayer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by HuWei on 2017/6/22.
 */

public class AudioPlayerFloatSwitch extends FrameLayout {

    private WindowManager mWindowManager;

    private View mContainer;
    private ImageView mIvSwitch;

    private GestureDetector mGestureDetector;

    private OnSwitchListener mOnSwitchListener;

    public AudioPlayerFloatSwitch(@NonNull Context context) {
        super(context);
        init();
    }

    public AudioPlayerFloatSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AudioPlayerFloatSwitch(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mContainer = LayoutInflater.from(getContext()).inflate(R.layout.music_float_switch, this, true);
        mContainer.setOnTouchListener(mOnTouchListener);
        mIvSwitch = (ImageView) mContainer.findViewById(R.id.iv_switch);
        mGestureDetector = new GestureDetector(getContext(), mOnGestureListener);
    }


    public void setup(boolean front) {
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        int width = WindowManager.LayoutParams.WRAP_CONTENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;

        int flags = 0;

        flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

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

        int windowWidth = 0;
        int windowHeight = 0;
        Point p = new Point();
        mWindowManager.getDefaultDisplay().getSize(p);
        windowWidth = p.x;
        windowHeight = p.y;

        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        int containerWidth = 0;
        int containerHeight = 0;
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        mContainer.measure(w, h);
        containerWidth = mContainer.getMeasuredWidth();
        containerHeight = mContainer.getMeasuredHeight();

        lp.x = windowWidth - containerWidth;
        lp.y = windowHeight - statusBarHeight - containerHeight - convertDpToPixels(getContext(), 60);
        mWindowManager.addView(this, lp);

        if (!front) {
            mContainer.setVisibility(View.GONE);
        }
    }

    public static int convertDpToPixels(Context context, float dp) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return px;
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = R.id.iv_switch;
            if (id == mContainer.getId()) {
                if (mOnSwitchListener != null) {
                    mOnSwitchListener.onSwitch();
                }
            }
        }
    };

    private OnTouchListener mOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return mGestureDetector.onTouchEvent(motionEvent);
        }
    };

    private GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        private int mOriginalX;
        private int mOriginalY;

        @Override
        public boolean onDown(MotionEvent e) {
            int[] location = new int[2];
            mContainer.getLocationOnScreen(location);
            mOriginalX = location[0];
            mOriginalY = location[1];
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mOnSwitchListener != null) {
                mOnSwitchListener.onSwitch();
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float deltaX = e2.getRawX() - e1.getRawX();
            float deltaY = e2.getRawY() - e1.getRawY();
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) mContainer.getLayoutParams();
            params.x = (int) (mOriginalX + deltaX);
            params.y = (int) (mOriginalY + deltaY);
            mWindowManager.updateViewLayout(mContainer, params);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    };

    public void show() {
        mContainer.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mContainer.setVisibility(View.GONE);
    }

    public boolean isVisibility() {
        return mContainer.getVisibility() == View.VISIBLE;
    }

    public interface OnSwitchListener {
        void onSwitch();
    }

    public void setOnSwitchListener(OnSwitchListener onSwitchListener) {
        this.mOnSwitchListener = onSwitchListener;
    }

    public void release() {
        mWindowManager.removeView(this);
    }

}
