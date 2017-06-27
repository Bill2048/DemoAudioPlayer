package com.chaoxing.demo.audioplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HuWei on 2017/6/14.
 */

public class AudioPlayerController {

    private static AudioPlayerController sInstance;

    private boolean mAudioServiceBound;
    private AudioPlayerServiceBindCallbacks mAudioPlayerServiceBindCallbacks;
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

    private long mPlaylistId;
    private AudioContentRequester mContentRequester;

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

    public void bindMediaService(Context context, AudioPlayerServiceBindCallbacks callbacks) {
        mAudioPlayerServiceBindCallbacks = callbacks;
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
            mAudioPlayer.setErrorHandler(mErrorHandler);
            mAudioServiceBound = true;
            launchFloatWindow(mAudioPlayer.getApplicationContext());
            ((BaseApplication) mAudioPlayer.getApplication()).addAppForegroundBackgroundSwitchListener(mAppForegroundBackgroundSwitchListener);
            if (mAudioPlayerServiceBindCallbacks != null) {
                mAudioPlayerServiceBindCallbacks.onBind();
            }
//            loadLocalAudio();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAudioServiceBound = false;
            if (mAudioPlayerServiceBindCallbacks != null) {
                mAudioPlayerServiceBindCallbacks.onUnbind();
            }
        }
    };

    public void setAudioPlayerServiceBindCallbacks(AudioPlayerServiceBindCallbacks audioPlayerServiceBindCallbacks) {
        this.mAudioPlayerServiceBindCallbacks = audioPlayerServiceBindCallbacks;
    }

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
                            Audio audio = new Audio();
                            audio.setData("http://s1.ananas.chaoxing.com/audio/a9/7ca3f4cb058055ccf5e4933d8c30766e/audio.mp3");
                            audio.setTitle("单田芳 - 水浒外传 - 第022回.MP3");
                            audioList.add(0, audio);
                            play(System.currentTimeMillis(), audioList, 0);
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
            mPlayerWindow.notifyProgressChanged(position, length);
        }

        @Override
        public void onCompleted() {
            nextPlay();
        }
    };

    private ErrorHandler mErrorHandler = new ErrorHandler() {
        @Override
        public boolean onErrorWithMediaPlayer(int what, int extra) {
            switch (what) {
                // 视频流式传输，其容器对于逐行播放无效，即视频的索引（例如moov atom）不在文件的开头。
                case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                    // 媒体服务器已经死机。在这种情况下，应用程序必须释放MediaPlayer对象并实例化一个新对象
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    // 未指定的媒体播放器错误
                case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                default:
                    break;
            }
            pausePlay();
            return false;
        }

        @Override
        public void onError(Exception e) {
            pausePlay();
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
            if (audio.getData() == null) {
                if (mContentRequester != null) {
                    mContentRequester.request(mPlaylistId, mActiveIndex, new AudioContentRequestCallbacks() {
                        @Override
                        public void onStart(long playlistId, int index) {
                            if (!mAudioServiceBound) {
                                return;
                            }
                            if (playlistId != mPlaylistId) {
                                return;
                            }
                        }

                        @Override
                        public void onCompleted(long playlistId, int index, String uri) {
                            if (!mAudioServiceBound) {
                                return;
                            }
                            if (playlistId != mPlaylistId) {
                                return;
                            }
                            Audio requestAudio = mAudioList.get(index);
                            requestAudio.setData(uri);
                            if (mActiveIndex == index) {
                                if (mPlayStatus == STATUS_PLAY) {
                                    AudioPlayerService.play(mAudioPlayer.getApplicationContext(), requestAudio, 0);
                                }
                            }
                        }

                        @Override
                        public void onError(long playlistId, int index, Exception e, String message) {
                            if (!mAudioServiceBound) {
                                return;
                            }
                            if (playlistId != mPlaylistId) {
                                return;
                            }
                            if (mActiveIndex == index) {
                                if (mPlayStatus == STATUS_PLAY) {
                                    pausePlay();
                                }
                            }
                        }
                    });
                }
            } else {
                AudioPlayerService.play(mAudioPlayer.getApplicationContext(), audio, 0);
            }
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

    private void setPlayProgress(int progress) {
        mAudioPlayer.updatePlayPosition(progress);
    }

    public void setAudioContentRequester(AudioContentRequester contentRequester) {
        this.mContentRequester = contentRequester;
    }

    public void play(long playlistId, List<Audio> audioList, int index) {
        Log.d("AP", "mAudioServiceBound : " + mAudioServiceBound + " playlist size : " + audioList.size());
        mPlaylistId = playlistId;
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
        mActiveIndex--;
        play(mActiveIndex);
    }

    public void nextPlay() {
        mActiveIndex++;
        play(mActiveIndex);

    }

}
