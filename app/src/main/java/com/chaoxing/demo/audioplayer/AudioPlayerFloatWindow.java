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
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by huwei on 2017/6/18.
 */

public class AudioPlayerFloatWindow extends FrameLayout {

    protected View mContainer;

    private ImageButton mIbPrevious;
    private ImageButton mIbPlay;
    private ImageButton mIbNext;
    private ImageButton mIbClose;
    private ImageButton mIbZoom;
    private ImageButton mIbPlaylist;

    private TextView mTvTitle;

    private TextView mTvProgress;
    private TextView mTvLength;
    private SeekBar mSbProgress;

    private View mLoading;

    private boolean mDraggingSeekBar;

    private PlayCallbacks mPlayCallbacks;

    public AudioPlayerFloatWindow(@NonNull Context context) {
        super(context);
        init();
    }

    public AudioPlayerFloatWindow(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AudioPlayerFloatWindow(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        mContainer = LayoutInflater.from(getContext()).inflate(R.layout.audio_player_window, this, true);

        mIbPrevious = (ImageButton) mContainer.findViewById(R.id.ib_previous);
        mIbPrevious.setOnClickListener(mOnClickListener);
        mIbPlay = (ImageButton) mContainer.findViewById(R.id.ib_play);
        mIbPlay.setOnClickListener(mOnClickListener);
        mIbNext = (ImageButton) mContainer.findViewById(R.id.ib_next);
        mIbNext.setOnClickListener(mOnClickListener);
        mIbClose = (ImageButton) mContainer.findViewById(R.id.ib_close);
        mIbClose.setOnClickListener(mOnClickListener);
        mIbZoom = (ImageButton) mContainer.findViewById(R.id.ib_zoom);
        mIbZoom.setOnClickListener(mOnClickListener);
        mIbPlaylist = (ImageButton) mContainer.findViewById(R.id.ib_playlist);
        mIbPlaylist.setOnClickListener(mOnClickListener);

        mTvTitle = (TextView) mContainer.findViewById(R.id.tv_title);

        mTvProgress = (TextView) mContainer.findViewById(R.id.tv_progress);
        mTvLength = (TextView) mContainer.findViewById(R.id.tv_length);
        mSbProgress = (SeekBar) mContainer.findViewById(R.id.sb_progress);
        mSbProgress.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        updateProgress(0, 0);
        mLoading = mContainer.findViewById(R.id.loading);
        mLoading.setVisibility(View.GONE);
    }

    public void setup() {
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
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.ib_previous) {
                if (mPlayCallbacks != null) {
                    mPlayCallbacks.onPrevious();
                }
            } else if (id == R.id.ib_play) {
                if (mPlayCallbacks != null) {
                    mPlayCallbacks.onPlay();
                }
            } else if (id == R.id.ib_next) {
                if (mPlayCallbacks != null) {
                    mPlayCallbacks.onNext();
                }
            } else if (id == R.id.ib_close) {

            } else if (id == R.id.ib_zoom) {
                hide();
            } else if (id == R.id.ib_playlist) {
                if (mPlayCallbacks != null) {
                    mPlayCallbacks.onShowPlaylist();
                }
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mDraggingSeekBar = true;
                updateProgress(progress, mSbProgress.getMax());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mPlayCallbacks != null) {
                mPlayCallbacks.onProgressChanged(seekBar.getProgress());
            }
            mDraggingSeekBar = false;
        }
    };

    public void updateProgress(int progress, int length) {
        if (mDraggingSeekBar) {
            return;
        }
        if (length < 0) {
            length = 0;
        }
        if (progress > length) {
            progress = length;
        }
        if (length == 0) {
            mSbProgress.setEnabled(false);
        } else {
            mSbProgress.setEnabled(true);
        }
        mSbProgress.setMax(length);
        mSbProgress.setProgress(progress);
        String fProgress = AudioPlayerUtils.formatTime(progress);
        String fLength = AudioPlayerUtils.formatTime(length);
        String timeArr[] = AudioPlayerUtils.formatTimeLength(fProgress, fLength);
        mTvProgress.setText(timeArr[0]);
        mTvLength.setText(timeArr[1]);
    }


    public void setPlayCallbacks(PlayCallbacks playCallbacks) {
        this.mPlayCallbacks = playCallbacks;
    }

    public void hide() {
        mContainer.setVisibility(View.GONE);
    }

    public void show() {
        mContainer.setVisibility(View.VISIBLE);
    }

    public void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        mLoading.setVisibility(View.GONE);
    }

    public void switchOnPlay() {
        mIbPlay.setImageResource(R.drawable.ic_pause_circle_outline_gray_13dp);
    }

    public void switchOnPause() {
        mIbPlay.setImageResource(R.drawable.ic_play_circle_outline_gray_13dp);
    }

    public void setTitle(String title) {
        mTvTitle.setText(title);
    }

}
