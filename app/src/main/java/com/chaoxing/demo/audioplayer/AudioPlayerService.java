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

    private final IBinder mIBinder = new LocalBinder();

    private MediaPlayer mMediaPlayer;

    // 音频焦点处理
    private AudioManager mAudioManager;

    // 来电处理
    private boolean mOngoingCall;
    private PhoneStateListener mPhoneStateListener;
    private TelephonyManager mTelephonyManager;

    private List<Audio> mAudioList = new ArrayList<>();
    private int mActiveIndex = -1;  // 当前音频在列表中的下标
    private Audio mActiveAudio;  // 当前播放音频
    private int mActivePosition;  // 当前音频暂停播放位置

    private OnProgressChangedListener onProgressChangedListener;

    @Override
    public void onCreate() {
        super.onCreate();

        callStateListener();
        registerPlayNewAudio();
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
        return mIBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            stopMedia();
            mMediaPlayer.release();
        }
        removeAudioFocus();
        if (mPhoneStateListener != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        unregisterReceiver(mPlayReceiver);
    }

    private void playList(List<Audio> audioList, int index, int position) {
        stopMedia();
        mAudioList.clear();
        mAudioList.addAll(audioList);
        if (!mAudioList.isEmpty()) {
            mActiveIndex = index;
            if (mActiveIndex < 0 || mActiveIndex >= mAudioList.size()) {
                mActiveIndex = 0;
            }
            mActivePosition = position;
            mActiveAudio = mAudioList.get(mActiveIndex);
        } else {
            mActiveIndex = -1;
            mActivePosition = 0;
            mActiveAudio = null;
        }
        initMediaPlayer();
    }

    private void playNext() {
        int index = mActiveIndex + 1;
        play(index, 0);
    }

    private void play(int index, int position) {
        if (index >= mAudioList.size()) {
            mActiveIndex = -1;
            mActiveAudio = null;
            mActivePosition = 0;
        } else {
            mActiveIndex = index;
            mActiveAudio = mAudioList.get(mActiveIndex);
            mActivePosition = position;
            initMediaPlayer();
        }
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
        if (mMediaPlayer == null) {
            return;
        }

        mMediaPlayer.stop();
        mActivePosition = 0;
    }

    private void pauseMedia() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mActivePosition = mMediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(mActivePosition);
            mMediaPlayer.start();
        }
    }

    private int setProgress(int currentProgress) {
        if (mMediaPlayer == null)
            return -1;

        currentProgress = currentProgress > 0 ? currentProgress : mMediaPlayer.getCurrentPosition();
        int length = mMediaPlayer.getDuration();

        if (onProgressChangedListener != null) {
            onProgressChangedListener.onProgressChanged(currentProgress, length);
        }

        mHandler.postDelayed(mUpdateProgressRunnable, 1000);
        return currentProgress;
    }

    private Runnable mUpdateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            setProgress(0);
        }
    };

    public interface OnProgressChangedListener {
        void onProgressChanged(int currentPosition, int length);
    }

    public void setOnProgressChangedListener(OnProgressChangedListener onProgressChangedListener) {
        this.onProgressChangedListener = onProgressChangedListener;
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
            playMedia();
            setProgress(0);
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

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
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
                        // Phone idle. Start playing.
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

    public class LocalBinder extends Binder {

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

    private void registerPlayNewAudio() {
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

    public void updatePlayPosition(int position) {
        if (mMediaPlayer == null) {
            return;
        }
        int length = mMediaPlayer.getDuration();
        if (position > length) {
            position = length;
        }
        mMediaPlayer.seekTo(position);
    }
}
