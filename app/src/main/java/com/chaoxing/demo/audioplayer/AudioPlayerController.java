package com.chaoxing.demo.audioplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HuWei on 2017/6/14.
 */

public class AudioPlayerController {

    private static AudioPlayerController sInstance;

    private boolean mAudioServiceBound;
    private AudioPlayerService mAudioPlayer;
    private AudioPlayerFloatWindow mPlayerWindow;
    private PlaylistFloatWindow mPlaylistWindow;
    private AudioPlayerFloatSwitch mPlayerSwitch;

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
        if (sInstance == null) {
            synchronized (AudioPlayerController.class) {
                if (sInstance == null) {
                    sInstance = new AudioPlayerController();
                }
            }
        }
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
            if (mPlayerWindow != null) {
                mPlayerWindow.release();
            }
            if (mPlaylistWindow != null) {
                mPlaylistWindow.release();
            }
            if (mPlayerSwitch != null) {
                mPlayerSwitch.release();
            }
            sInstance = null;
        }
    }

    private ServiceConnection mAudioServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.AudioPlayerBinder binder = (AudioPlayerService.AudioPlayerBinder) service;
            mAudioPlayer = binder.getService();
            mAudioPlayer.setOnPlayStatusChangedListener(mOnPlayStatusChangedListener);
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

    private void launchFloatWindow(Context context) {
        mPlayerWindow = new AudioPlayerFloatWindow(context.getApplicationContext());
        mPlayerWindow.setup();
        mPlayerWindow.setPlayCallbacks(mPlayCallbacks);
        mPlayerWindow.setOperationCallbacks(mOperationCallbacks);

        mPlaylistWindow = new PlaylistFloatWindow(context.getApplicationContext());
        mPlaylistWindow.setup(false);
        mPlaylistWindow.setPlayCallbacks(mPlayCallbacks);

        mPlayerSwitch = new AudioPlayerFloatSwitch(context.getApplicationContext());
        mPlayerSwitch.setup(false);
        mPlayerSwitch.setOnSwitchListener(mOnSwitchListener);
    }

    BaseApplication.AppForegroundBackgroundSwitchListener mAppForegroundBackgroundSwitchListener = new BaseApplication.AppForegroundBackgroundSwitchListener() {
        @Override
        public void onForeground() {

        }

        @Override
        public void onBackground() {
            if (mPlayerSwitch != null) {
                mPlayerSwitch.show();
            }
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


    private PlayCallbacks mPlayCallbacks = new PlayCallbacks() {
        @Override
        public void onPlay() {
            if (mPlayStatus == STATUS_PLAY) {
                pausePlay();
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

    };

    private OperationCallbacks mOperationCallbacks = new OperationCallbacks() {
        @Override
        public void onShowPlaylist() {
            if (mPlaylistWindow != null) {
                mPlaylistWindow.show();
            }
        }

        @Override
        public void onHideWindow() {
            mPlayerWindow.hide();
            if (mPlayerSwitch != null) {
                mPlayerSwitch.show();
            }
        }

        @Override
        public void onRelease() {
            if (mAudioServiceBound) {
                unBindAudioService(mAudioPlayer.getApplicationContext());
            }
        }
    };

    private AudioPlayerFloatSwitch.OnSwitchListener mOnSwitchListener = new AudioPlayerFloatSwitch.OnSwitchListener() {
        @Override
        public void onSwitch() {
            mPlayerSwitch.hide();
            if (mPlayerWindow != null) {
                mPlayerWindow.show();
            }
        }
    };

    OnPlayStatusChangedListener mOnPlayStatusChangedListener = new OnPlayStatusChangedListener() {
        @Override
        public void onStart() {
            mPlayStatus = STATUS_PLAY;
            updatePlayerByStatus();
        }

        @Override
        public void onPause() {
            mPlayStatus = STATUS_PAUSE;
            updatePlayerByStatus();
        }

        @Override
        public void onStop() {
            mPlayStatus = STATUS_STOP;
            updatePlayerByStatus();
        }

        @Override
        public void onPlayPositionChanged(int position, int length) {
            mPlayerWindow.updateProgress(position, length);
        }

        @Override
        public void onCompleted() {
            nextPlay();
        }
    };

    private ErrorHandler errorHandler = new ErrorHandler() {
        @Override
        public boolean onErrorWithMediaPlayer(int what, int extra) {
            switch (what) {
                // 文件不存在或错误，或网络不可访问错误
                case MediaPlayer.MEDIA_ERROR_IO:
                    break;
                // 流不符合有关标准或文件的编码规范
                case MediaPlayer.MEDIA_ERROR_MALFORMED:
                    break;
                // 视频流及其容器不适用于连续播放视频的指标（例如：MOOV原子）不在文件的开始
                case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                    break;
                // 媒体服务器挂掉了
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    break;
                // 一些操作使用了过长的时间，也就是超时了，通常是超过了3-5秒
                case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                    break;
                // 未知错误
                case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                    break;
                // 比特流符合相关编码标准或文件的规格，但媒体框架不支持此功能
                case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                    break;
                default:break;
            }
            return false;
        }

        @Override
        public void onError(Exception e) {

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
            mPlayStatus = STATUS_PLAY;
            updatePlayerByStatus();
            Audio audio = mAudioList.get(mActiveIndex);
            AudioPlayerService.play(mAudioPlayer.getApplicationContext(), audio, 0);
        }
    }

    private void updatePlayerByStatus() {
        if (mPlayStatus == STATUS_PLAY) {
            mPlayerWindow.switchOnPlay();
            Audio audio = mAudioList.get(mActiveIndex);
            mPlayerWindow.setTitle(audio.getTitle());
            mPlaylistWindow.notifyActiveIndex(mActiveIndex, audio);
        } else if (mPlayStatus == STATUS_PAUSE) {
            mPlayerWindow.switchOnPause();
        } else if (mPlayStatus == STATUS_STOP) {
            mPlayerWindow.switchOnPause();
        }
    }

    public void play(List<Audio> audioList, int index) {
        if (!mAudioServiceBound) {
            return;
        }

        mAudioList.clear();
        mAudioList.addAll(audioList);

        mPlaylistWindow.notifyPlaylist(index, audioList);

        play(index);
    }

    public void resumePlay() {
        if (mAudioPlayer.isPause()) {
            mAudioPlayer.resumePlay();
        }
    }

    public void pausePlay() {
        mAudioPlayer.pausePlay();
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
