package com.chaoxing.demo.audioplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by huwei on 2017/6/11.
 */

public class AudioPlayerService extends Service {

    private Handler mHandler = new Handler();

    private final IBinder mAudioPlayerBinder = new AudioPlayerBinder();

    private MediaPlayer mMediaPlayer;

    // 音频焦点处理
    private AudioManager mAudioManager;

    // 来电处理
    private boolean mOngoingCall;
    private PhoneStateListener mPhoneStateListener;
    private TelephonyManager mTelephonyManager;

    private Audio mActiveAudio;  // 当前播放音频
    private int mActivePosition;  // 当前音频暂停播放位置

    private OnPlayStatusChangedListener mOnPlayStatusChangedListener;  // 播放进度

    private boolean mPause;  // 当前是否暂停

    @Override
    public void onCreate() {
        super.onCreate();

        callStateListener();
//        requestAudioFocus();
        registerPlayReceiver();
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        try {
//            //An audio file is passed to the service through putExtra();
//            mActiveAudio = intent.getParcelableExtra("audio");
//        } catch (NullPointerException e) {
//            stopSelf();
//        }
//        if (mActiveAudio == null) {
//            stopSelf();
//        }
//
//        //Request audio focus
//        if (requestAudioFocus() == false) {
//            //Could not gain focus
//            stopSelf();
//        }
//
//        initMediaPlayer();
//
//        return super.onStartCommand(intent, flags, startId);
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAudioPlayerBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopMedia();

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }

        if (mPhoneStateListener != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        }

        unregisterReceiver(mPlayReceiver);
    }

    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
        mMediaPlayer.setOnErrorListener(onErrorListener);
        mMediaPlayer.setOnPreparedListener(onPreparedListener);
        mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
        mMediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
        mMediaPlayer.setOnInfoListener(onInfoListener);

        mMediaPlayer.reset();

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        if (mActiveAudio != null) {
            try {
                String path = mActiveAudio.getData();
                if (path == null || path.trim().length() == 0) {
                    return;
                }

                final Uri uri = Uri.parse(path);
                final String scheme = uri.getScheme();
                if ("file".equals(scheme)) {
                    File audioFile = new File(path);
                    if (!audioFile.exists()) {
                        return;
                    }
                }
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepareAsync();
                if (mOnPlayStatusChangedListener != null) {
                    mOnPlayStatusChangedListener.onStart();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void play(Audio audio, int position) {
        stopMedia();
        mActiveAudio = audio;
        mActivePosition = position;
        initMediaPlayer();
    }

    private void playMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
        updatePosition(0);
        if (mOnPlayStatusChangedListener != null) {
            mOnPlayStatusChangedListener.onStart();
        }
    }

    private void stopMedia() {
        removePositionListener();
        mActiveAudio = null;
        mActivePosition = 0;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        if (mOnPlayStatusChangedListener != null) {
            mOnPlayStatusChangedListener.onStop();
        }
    }

    private void pauseMedia() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mPause = true;
        }
        removePositionListener();
        if (mOnPlayStatusChangedListener != null) {
            mOnPlayStatusChangedListener.onPause();
        }
    }

    private void resumeMedia() {
        if (mPause) {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
            }
        }
        mPause = false;
        updatePosition(0);
        if (mOnPlayStatusChangedListener != null) {
            mOnPlayStatusChangedListener.onStart();
        }
    }

    private int updatePosition(int position) {
        if (mMediaPlayer == null) {
            return -1;
        }
        position = position > 0 ? position : mMediaPlayer.getCurrentPosition();
        int length = mMediaPlayer.getDuration();

        if (mOnPlayStatusChangedListener != null) {
            mOnPlayStatusChangedListener.onPlayPositionChanged(position, length);
        }
        removePositionListener();
        mHandler.postDelayed(mUpdatePositionRunnable, 1000);
        return position;
    }

    private void removePositionListener() {
        mHandler.removeCallbacks(mUpdatePositionRunnable);
    }

    private Runnable mUpdatePositionRunnable = new Runnable() {
        @Override
        public void run() {
            updatePosition(0);
        }
    };


    private MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                    Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                    break;
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                    break;
                case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                    Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private MediaPlayer.OnInfoListener onInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            return false;
        }
    };

    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            int length = mp.getDuration();
            if (mActivePosition > length) {
                mActivePosition = 0;
            }
            if (mActivePosition > 0) {
                mMediaPlayer.seekTo(mActivePosition);
            }
            playMedia();
        }
    };

    private MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {

        }
    };

    private MediaPlayer.OnSeekCompleteListener onSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            if (isPause()) {
                resumePlay();
            } else {
                mMediaPlayer.start();
                updatePosition(0);
                if (mOnPlayStatusChangedListener != null) {
                    mOnPlayStatusChangedListener.onStart();
                }
            }
        }
    };


    // 音频焦点处理
    private boolean requestAudioFocus() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                // 获得音频焦点
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mMediaPlayer == null) initMediaPlayer();
                    else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                    break;
                // 失去音频焦点（可以降低音量继续播放）
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                    break;
                // 暂时失去音频焦点（停止播放，短时间可能再次获得音频焦点）
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                    break;
                // 长时间失去音频焦点（停止播放，释放资源）
                case AudioManager.AUDIOFOCUS_LOSS:
                    if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                    break;

                default:
                    break;
            }
        }
    };

    // 来电处理
    private void callStateListener() {
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mMediaPlayer != null) {
                            pauseMedia();
                            mOngoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mMediaPlayer != null) {
                            if (mOngoingCall) {
                                mOngoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            stopMedia();
            if (mOnPlayStatusChangedListener != null) {
                mOnPlayStatusChangedListener.onCompleted();
            }
        }
    };

    public class AudioPlayerBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

    public void setOnPlayStatusChangedListener(OnPlayStatusChangedListener onPlayStatusChangedListener) {
        this.mOnPlayStatusChangedListener = onPlayStatusChangedListener;
    }

    public static final String BROADCAST_PLAY_NEW_AUDIO = "com.chaoxing.mobile.audioplayer.PlayNewAudio";
    public final static String PLAY_ARGS_AUDIO = "audio";
    public final static String PLAY_ARGS_POSITION = "position";

    public static void play(Context context, Audio audio, int position) {
        Intent intent = new Intent(AudioPlayerService.BROADCAST_PLAY_NEW_AUDIO);
        intent.putExtra(PLAY_ARGS_AUDIO, audio);
        intent.putExtra(PLAY_ARGS_POSITION, position);
        context.sendBroadcast(intent);
    }

    private void registerPlayReceiver() {
        IntentFilter filter = new IntentFilter(BROADCAST_PLAY_NEW_AUDIO);
        registerReceiver(mPlayReceiver, filter);
    }

    private BroadcastReceiver mPlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Audio audio = intent.getParcelableExtra(PLAY_ARGS_AUDIO);
            int position = intent.getIntExtra(PLAY_ARGS_POSITION, 0);
            play(audio, position);
        }
    };

    public void pausePlay() {
        if (mMediaPlayer == null) {
            return;
        }
        pauseMedia();
    }

    public void resumePlay() {
        if (mMediaPlayer == null) {
            return;
        }
        resumeMedia();
    }

    public boolean isPause() {
        return mPause;
    }

    public void updatePlayPosition(int position) {
        if (mMediaPlayer == null) {
            return;
        }
        removePositionListener();
        int length = mMediaPlayer.getDuration();
        if (position > length) {
            position = length;
        }
        mMediaPlayer.seekTo(position);
        mActivePosition = position;
    }
}
