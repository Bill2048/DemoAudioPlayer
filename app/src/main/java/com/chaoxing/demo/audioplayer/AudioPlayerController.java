package com.chaoxing.demo.audioplayer;

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
    private PlaylistFloatWindow mPlaylistWindow;
    private MusicFloatSwitch mMusicSwitch;

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
            ((BaseApplication) mAudioPlayer.getApplication()).addAppForegroundBackgroundSwitchListener(mAppForegroundBackgroundSwitchListener);
            loadLocalAudio();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAudioServiceBound = false;
        }
    };

    BaseApplication.AppForegroundBackgroundSwitchListener mAppForegroundBackgroundSwitchListener = new BaseApplication.AppForegroundBackgroundSwitchListener() {
        @Override
        public void onForeground() {
            if (mPlayerWindow != null) {
                mPlayerWindow.show();
            }
        }

        @Override
        public void onBackground() {
            if (mPlaylistWindow != null) {
                mPlaylistWindow.hide();
            }
            if (mPlayerWindow != null) {
                mPlayerWindow.hide();
            }
        }
    };

    private void loadLocalAudio() {
        AudioPlayerUtils.scanLocalAudio(mAudioPlayer.getApplicationContext(), new AudioPlayerUtils.ScanLocalAudioCallbacks() {
            @Override
            public void onStart() {
                mPlayerWindow.showLoading();
            }

            @Override
            public void onCompletionInBackground(final List<Audio> audioList) {
                if (mPlayerWindow != null) {
                    mPlayerWindow.post(new Runnable() {
                        @Override
                        public void run() {
                            mPlayerWindow.hideLoading();
                            play(audioList, 0);
                        }
                    });
                }
            }
        });
    }

    private void play(int index) {
        if (index < 0) {
            index = 0;
        }

        if (index >= mAudioList.size()) {
            index = -1;
        }
        mActiveIndex = index;
        if (mActiveIndex >= 0) {
            mPlayStatus = STATUS_PLAY;
            Audio audio = mAudioList.get(index);
            AudioPlayerService.play(mAudioPlayer.getApplicationContext(), audio, 0);
            mPlayerWindow.switchOnPlay();
            mPlayerWindow.setTitle(audio.getTitle());
            mPlaylistWindow.notifyActiveIndex(mActiveIndex, audio);
        }
    }

    private PlayCallbacks mPlayCallbacks = new PlayCallbacks() {
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
        public void onPlay(int index) {
            play(index);
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

        @Override
        public void onShowPlaylist() {
            mPlaylistWindow.show();
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
            mPlayerWindow.setup();
            mPlayerWindow.setPlayCallbacks(mPlayCallbacks);

            mPlaylistWindow = new PlaylistFloatWindow(context.getApplicationContext());
            mPlaylistWindow.setup(false);
            mPlaylistWindow.setPlayCallbacks(mPlayCallbacks);

            mMusicSwitch = new MusicFloatSwitch(context.getApplicationContext());
            mMusicSwitch.setup(true);

        }
    }

    public void play(List<Audio> audioList, int index) {
        if (!mAudioServiceBound) {
            return;
        }

        mAudioList.clear();
        mAudioList.addAll(audioList);

        mAudioPlayer.setOnPositionChangedListener(mOnProgressChangedListener);

        mPlaylistWindow.notifyPlaylist(audioList);

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

    public boolean isPause() {
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
