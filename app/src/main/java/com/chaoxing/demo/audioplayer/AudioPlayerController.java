package com.chaoxing.demo.audioplayer;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by HuWei on 2017/6/14.
 */

public class AudioPlayerController {

    private static AudioPlayerController sInstance = new AudioPlayerController();

    private AudioPlayerFloatWindow mPlayerWindow;

    private AudioPlayerController() {
    }

    public static AudioPlayerController getInstance() {
        return sInstance;
    }

    public void showFloatWindow(Context context) {
        if (mPlayerWindow == null) {
            mPlayerWindow = new AudioPlayerFloatWindow(context);
            mPlayerWindow.setOnOperationListener(mOnOperationListener);
            mPlayerWindow.launch();
        }
    }

    private AudioPlayerService getAudioPlayer(Activity activity) {
        BaseApplication application = (BaseApplication) activity.getApplication();
        application.bindMediaService();
        AudioPlayerService audioPlayer = application.getAudioPlayer();
        return audioPlayer;
    }

    public void play(Activity activity, ArrayList<Audio> audioList, int index, int position) {
        AudioPlayerService audioPlayer = getAudioPlayer(activity);
        audioPlayer.setOnPositionChangedListener(mOnProgressChangedListener);
        AudioPlayerService.play(activity, audioList, index, position);
    }

    public void resumePlay(Activity activity) {
        AudioPlayerService audioPlayer = getAudioPlayer(activity);
        audioPlayer.resumePlay();
    }

    public void parsePlay(Activity activity) {
        AudioPlayerService audioPlayer = getAudioPlayer(activity);
        audioPlayer.pausePlay();
    }

    public boolean isPause(Activity activity) {
        AudioPlayerService audioPlayer = getAudioPlayer(activity);
        return audioPlayer.isPause();
    }

    public void previous(Activity activity) {
        AudioPlayerService audioPlayer = getAudioPlayer(activity);
        audioPlayer.previous();
    }

    public void next(Activity activity) {
        AudioPlayerService audioPlayer = getAudioPlayer(activity);
        audioPlayer.next();
    }

    public void setPlayProgress(Activity activity, int progress) {
        AudioPlayerService audioPlayer = getAudioPlayer(activity);
        audioPlayer.updatePlayPosition(progress);
    }

    private AudioPlayerFloatWindow.OnOperationListener mOnOperationListener = new AudioPlayerFloatWindow.OnOperationListener() {
        @Override
        public void onPlay() {

        }

        @Override
        public void onPrevious() {

        }

        @Override
        public void onNext() {

        }

        @Override
        public void onProgressChanged(int progress) {

        }
    };

    private AudioPlayerService.OnPositionChangedListener mOnProgressChangedListener = new AudioPlayerService.OnPositionChangedListener() {
        @Override
        public void onPositionChanged(int currentPosition, int length) {
            Iterator<Map.Entry<Context, OnPlayListener>> it = mOnPlayListenerMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Context, OnPlayListener> entry = it.next();
                Context context = entry.getKey();
                OnPlayListener listener = entry.getValue();
                if (context == null || (context instanceof Activity && ((Activity) context).isFinishing()) || listener == null) {
                    it.remove();
                    continue;
                }
                listener.onProgressChanged(currentPosition, length);
            }
        }
    };

    private Map<Context, OnPlayListener> mOnPlayListenerMap = new HashMap<>();

    public interface OnPlayListener {
        void onProgressChanged(int currentPosition, int length);
    }

    public void addOnPlayListener(Context context, OnPlayListener listener) {
        mOnPlayListenerMap.put(context, listener);
    }

}
