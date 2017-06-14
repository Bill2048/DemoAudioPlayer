package com.chaoxing.demo.audioplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private List<Audio> mAudioList = new ArrayList<>();  // 当前播放列表
    private int mActiveIndex = -1;  // 当前音频在列表中的下标
    private Audio mActiveAudio;  // 当前播放音频
    private int mActivePosition;  // 当前音频暂停播放位置

    private OnPositionChangedListener onPositionChangedListener;  // 播放进度

    private boolean mPause;  // 当前是否暂停

    @Override
    public void onCreate() {
        super.onCreate();

        callStateListener();
        requestAudioFocus();
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
        mAudioList.clear();

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

    private void playList(List<Audio> audioList, int index, int position) {
        stopMedia();
        mAudioList.clear();
        mAudioList.addAll(audioList);
        play(index, position);
    }

    private void play(int index, int position) {
        if (index < 0 || index >= mAudioList.size()) {
            stopMedia();
        } else {
            mActiveIndex = index;
            mActiveAudio = mAudioList.get(mActiveIndex);
            mActivePosition = position;
            initMediaPlayer();
        }
    }

    private void playPrevious() {
        int index = mActiveIndex - 1;
        stopMedia();
        play(index, 0);
    }

    private void playNext() {
        int index = mActiveIndex + 1;
        stopMedia();
        play(index, 0);
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
                mMediaPlayer.setDataSource(mActiveAudio.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.prepareAsync();
        }
    }

    private void playMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    private void stopMedia() {
        mHandler.removeCallbacks(mUpdatePositionRunnable);
        mActiveIndex = -1;
        mActiveAudio = null;
        mActivePosition = 0;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mActivePosition = mMediaPlayer.getCurrentPosition();
            mPause = true;
        }
    }

    private void resumeMedia() {
        if (mPause) {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.seekTo(mActivePosition);
                mMediaPlayer.start();
            }
        }
        mPause = false;
    }

    private int updatePosition(int position) {
        if (mMediaPlayer == null)
            return -1;

        position = position > 0 ? position : mMediaPlayer.getCurrentPosition();
        int length = mMediaPlayer.getDuration();

        if (onPositionChangedListener != null) {
            onPositionChangedListener.onPositionChanged(position, length);
        }
        mHandler.removeCallbacks(mUpdatePositionRunnable);
        mHandler.postDelayed(mUpdatePositionRunnable, 1000);
        return position;
    }

    private Runnable mUpdatePositionRunnable = new Runnable() {
        @Override
        public void run() {
            updatePosition(0);
        }
    };

    public interface OnPositionChangedListener {
        void onPositionChanged(int currentPosition, int length);
    }

    public void setOnPositionChangedListener(OnPositionChangedListener onPositionChangedListener) {
        this.onPositionChangedListener = onPositionChangedListener;
    }

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
            updatePosition(0);
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
                    // resume playback
                    if (mMediaPlayer == null) initMediaPlayer();
                    else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                    break;
                // 失去音频焦点（可以降低音量继续播放）
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // Lost focus for a short time, but it's ok to keep playing
                    // at an attenuated level
                    if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                    break;
                // 暂时失去音频焦点（停止播放，短时间可能再次获得音频焦点）
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // Lost focus for a short time, but we have to stop
                    // playback. We don't release the media player because playback
                    // is likely to resume
                    if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                    break;
                // 长时间失去音频焦点（停止播放，释放资源）
                case AudioManager.AUDIOFOCUS_LOSS:
                    // Lost focus for an unbounded amount of time: stop playback and release media player
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
            playNext();
        }
    };

    public class AudioPlayerBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

    public final static String PLAY_ARGS_PLAY_LIST = "playList";
    public final static String PLAY_ARGS_INDEX = "index";
    public final static String PLAY_ARGS_POSITION = "position";

    public static void play(Context context, ArrayList<Audio> playList, int index, int position) {
        Intent intent = new Intent(AudioPlayerService.BROADCAST_PLAY_NEW_AUDIO);
        intent.putParcelableArrayListExtra(PLAY_ARGS_PLAY_LIST, playList);
        intent.putExtra(PLAY_ARGS_INDEX, index);
        intent.putExtra(PLAY_ARGS_POSITION, position);
        context.sendBroadcast(intent);
    }

    public static final String BROADCAST_PLAY_NEW_AUDIO = "com.chaoxing.mobile.audioplayer.PlayNewAudio";

    private void registerPlayReceiver() {
        IntentFilter filter = new IntentFilter(BROADCAST_PLAY_NEW_AUDIO);
        registerReceiver(mPlayReceiver, filter);
    }

    private BroadcastReceiver mPlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<Audio> audioList = intent.getParcelableArrayListExtra(PLAY_ARGS_PLAY_LIST);
            int index = intent.getIntExtra(PLAY_ARGS_INDEX, 0);
            int position = intent.getIntExtra(PLAY_ARGS_POSITION, 0);
            playList(audioList, index, position);
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

    public void previous() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying() || mPause) {
            playPrevious();
        }
    }

    public void next() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying() || mPause) {
            playNext();
        }
    }

    public boolean isPause() {
        return mPause;
    }

    public void updatePlayPosition(int position) {
        if (mMediaPlayer == null) {
            return;
        }
        int length = mMediaPlayer.getDuration();
        if (position > length) {
            position = length;
        }
        mMediaPlayer.seekTo(position);
        mActivePosition = position;
    }
}
