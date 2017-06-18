package com.chaoxing.demo.audioplayer;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by huwei on 2017/6/18.
 */

public class AudioPlayerFloatWindow extends FrameLayout {

    protected View mContentView;

    private TextView mTvProgress;
    private TextView mTvLength;
    private AppCompatSeekBar mSbProgress;

    private ImageButton mIbPrevious;
    private ImageButton mIbPlay;
    private ImageButton mIbNext;

    private boolean mDraggingSeekBar;

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
        mContentView = LayoutInflater.from(getContext()).inflate(R.layout.audio_player_window, this, true);
        mTvProgress = (TextView) mContentView.findViewById(R.id.tv_progress);
        mTvLength = (TextView) mContentView.findViewById(R.id.tv_length);
        mSbProgress = (AppCompatSeekBar) mContentView.findViewById(R.id.sb_progress);

        mIbPrevious = (ImageButton) mContentView.findViewById(R.id.ib_previous);
        mIbPrevious.setOnClickListener(mOnClickListener);
        mIbPlay = (ImageButton) mContentView.findViewById(R.id.ib_play);
        mIbPlay.setOnClickListener(mOnClickListener);
        mIbNext = (ImageButton) mContentView.findViewById(R.id.ib_next);
        mIbNext.setOnClickListener(mOnClickListener);
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.ib_previous) {

            } else if (id == R.id.ib_play) {

            } else if (id == R.id.ib_next) {

            }
        }
    };

}
