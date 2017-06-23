package com.chaoxing.demo.audioplayer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HuWei on 2017/6/22.
 */

public class PlaylistFloatWindow extends FrameLayout {

    private WindowManager mWindowManager;

    private View mContainer;

    private TextView mTvTitle;
    private Button mBtnClose;
    private ListView mLvPlaylist;
    private List<Audio> mAudioList = new ArrayList<>();
    private PlaylistAdapter mAdapter;

    private int mActiveIndex = -1;

    private PlayCallbacks mPlayCallbacks;

    public PlaylistFloatWindow(@NonNull Context context) {
        super(context);
        init();
    }

    public PlaylistFloatWindow(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlaylistFloatWindow(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mContainer = LayoutInflater.from(getContext()).inflate(R.layout.playlist_window, this, true);
        mContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
        mTvTitle = (TextView) mContainer.findViewById(R.id.tv_title);
        mBtnClose = (Button) mContainer.findViewById(R.id.btn_close);
        mBtnClose.setOnClickListener(mOnClickListener);
        mLvPlaylist = (ListView) mContainer.findViewById(R.id.lv_playlist);
        mAdapter = new PlaylistAdapter(getContext(), mAudioList);
        mAdapter.setPlaylistCallbacks(mPlaylistCallbacks);
        mLvPlaylist.setAdapter(mAdapter);
        mLvPlaylist.setOnItemClickListener(mOnItemClickListener);
    }

    public void setup(boolean front) {
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.MATCH_PARENT;

        int flags = 0;

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
        mWindowManager.addView(this, wmLayoutParams);
        if (!front) {
            mContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mContainer.getVisibility() == View.VISIBLE) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                mContainer.setVisibility(View.GONE);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.btn_close) {
                mContainer.setVisibility(View.GONE);
            }
        }
    };

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mPlayCallbacks != null) {
                mPlayCallbacks.onPlay(position);
            }
        }
    };

    private PlaylistAdapter.PlaylistCallbacks mPlaylistCallbacks = new PlaylistAdapter.PlaylistCallbacks() {
        @Override
        public int getActiveIndex() {
            return mActiveIndex;
        }

        @Override
        public Audio getActiveAudio() {
            return null;
        }
    };

    public void setPlayCallbacks(PlayCallbacks playCallbacks) {
        this.mPlayCallbacks = playCallbacks;
    }


    public void show() {
        mContainer.setVisibility(View.VISIBLE);
        mLvPlaylist.setSelection(mActiveIndex);
    }

    public void hide() {
        mContainer.setVisibility(View.GONE);
    }

    public void notifyPlaylist(int index, List<Audio> audioList) {
        mActiveIndex = index;
        mAudioList.clear();
        mAudioList.addAll(audioList);
        mAdapter.notifyDataSetChanged();
    }

    public void notifyActiveIndex(int index, Audio audio) {
        mActiveIndex = index;
        mAdapter.notifyDataSetChanged();
    }

    public void release() {
        mWindowManager.removeView(this);
    }
}
