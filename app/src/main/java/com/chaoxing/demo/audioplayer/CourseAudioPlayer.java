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

public class CourseAudioPlayer {

    private static CourseAudioPlayer sInstance = new CourseAudioPlayer();

    private CourseAudioPlayer() {
    }

    public static CourseAudioPlayer getInstance() {
        return sInstance;
    }

    private AudioPlayerService getAudioPlayer(Activity activity) {
        BaseApplication application = (BaseApplication) activity.getApplication();
        application.bindMediaService();
        AudioPlayerService audioPlayer = application.getAudioPlayer();
        return audioPlayer;
    }

    public void playList(Activity activity, ArrayList<Audio> audioList, int index, int position) {
        AudioPlayerService audioPlayer = getAudioPlayer(activity);
        audioPlayer.setOnProgressChangedListener(mOnProgressChangedListener);
        AudioPlayerService.play(activity, audioList, index, position);
    }

    public void setPlayProgress(Activity activity, int progress) {
        AudioPlayerService audioPlayer = getAudioPlayer(activity);
        audioPlayer.updatePlayPosition(progress);
    }

    private AudioPlayerService.OnProgressChangedListener mOnProgressChangedListener = new AudioPlayerService.OnProgressChangedListener() {
        @Override
        public void onProgressChanged(int currentPosition, int length) {
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
