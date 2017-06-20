package com.chaoxing.demo.audioplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HuWei on 2017/6/14.
 */

public class AudioPlayerController {

    private static AudioPlayerController sInstance = new AudioPlayerController();

    private boolean mAudioServiceBound;
    private AudioPlayerService mAudioPlayer;
    private AudioPlayerFloatWindow mPlayerWindow;

    private List<Audio> mAudioList = new ArrayList<>();
    private int mActiveIndex = 1;
    private int mActivePosition;

    private int mPlayStatus;
    public final static int STATUS_STOP = 0;
    public final static int STATUS_PLAY = 1;
    public final static int STATUS_PAUSE = 2;

    private AudioPlayerController() {
    }

    public static AudioPlayerController getInstance() {
        return sInstance;
    }

    public void bindMediaService(Context context) {
        if (!mAudioServiceBound) {
            Intent playerIntent = new Intent(context.getApplicationContext(), AudioPlayerService.class);
//        startService(playerIntent);
            context.getApplicationContext().bindService(playerIntent, mAudioServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unBindAudioService(Context context) {
        if (mAudioServiceBound && mAudioServiceConnection != null) {
            context.getApplicationContext().unbindService(mAudioServiceConnection);
            mAudioServiceBound = false;
            mAudioPlayer.stopSelf();
        }
    }

    private ServiceConnection mAudioServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.AudioPlayerBinder binder = (AudioPlayerService.AudioPlayerBinder) service;
            mAudioPlayer = binder.getService();
            mAudioServiceBound = true;
            launchFloatWindow(mAudioPlayer.getApplicationContext());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAudioServiceBound = false;
        }
    };


    private void play(int index) {
        if (index < 0) {
            index = 0;
        }

        if (index >= mAudioList.size()) {
            index = -1;
        }
        mActiveIndex = index;
        if (mActiveIndex >= 0) {
            AudioPlayerService.play(mAudioPlayer.getApplicationContext(), mAudioList.get(index), 0);
            mPlayerWindow.switchOnPlay();
            mPlayStatus = STATUS_PLAY;
        }
    }

    private AudioPlayerFloatWindow.OnOperationListener mOnOperationListener = new AudioPlayerFloatWindow.OnOperationListener() {
        @Override
        public void onPlay() {
            if (mPlayStatus == STATUS_PLAY) {
                parsePlay();
            } else if (mPlayStatus == STATUS_PAUSE) {
                if (mAudioPlayer.isPause()) {
                    resumePlay();
                } else {
                    play(mActiveIndex);
                }
            } else if (mPlayStatus == STATUS_STOP) {
                play(mActiveIndex);
            }
        }

        @Override
        public void onPrevious() {
            previousPlay();
        }

        @Override
        public void onNext() {
            nextPlay();
        }

        @Override
        public void onProgressChanged(int progress) {
            setPlayProgress(progress);
        }
    };

    private AudioPlayerService.OnPositionChangedListener mOnProgressChangedListener = new AudioPlayerService.OnPositionChangedListener() {
        @Override
        public void onPositionChanged(int position, int length) {
            mPlayerWindow.updateProgress(position, length);

        }
    };


    public void launchFloatWindow(Context context) {
        if (mPlayerWindow == null) {
            mPlayerWindow = new AudioPlayerFloatWindow(context.getApplicationContext());
            mPlayerWindow.setOnOperationListener(mOnOperationListener);
            mPlayerWindow.setup();
        }
    }

    public void play(ArrayList<Audio> audioList, int index) {
        if (!mAudioServiceBound) {
            return;
        }

        mAudioList.clear();
        mAudioList.addAll(audioList);

        mAudioPlayer.setOnPositionChangedListener(mOnProgressChangedListener);

        play(index);
    }

    public void resumePlay() {
        if (mAudioPlayer.isPause()) {
            mAudioPlayer.resumePlay();
            mPlayerWindow.switchOnPlay();
            mPlayStatus = STATUS_PLAY;
        }
    }

    public void parsePlay() {
        mAudioPlayer.pausePlay();
        mPlayerWindow.switchOnPause();
        mPlayStatus = STATUS_PAUSE;
    }

    public boolean isPause(Activity activity) {
        return mPlayStatus == STATUS_PAUSE || mPlayStatus == STATUS_STOP;
    }

    public void previousPlay() {
        if (mPlayStatus == STATUS_STOP) {
            play(mActiveIndex);
        } else {
            mActiveIndex--;
            play(mActiveIndex);
        }
    }

    public void nextPlay() {
        if (mPlayStatus == STATUS_STOP) {
            play(mActiveIndex);
        } else {
            mActiveIndex++;
            play(mActiveIndex);
        }
    }

    public void setPlayProgress(int progress) {
        mAudioPlayer.updatePlayPosition(progress);
    }

}
