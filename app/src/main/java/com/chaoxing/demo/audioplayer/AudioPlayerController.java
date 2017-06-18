package com.chaoxing.demo.audioplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by HuWei on 2017/6/14.
 */

public class AudioPlayerController {

    private static AudioPlayerController sInstance = new AudioPlayerController();

    private AudioPlayerController() {
    }

    public static AudioPlayerController getInstance() {
        return sInstance;
    }

    public void launchPlayerFloatWindow(Context context) {
        AudioPlayerFloatWindow window = new AudioPlayerFloatWindow(context);
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        WindowManager.LayoutParams wmLayoutParams = new WindowManager.LayoutParams();
//        以下都是WindowManager.LayoutParams的相关属性
//        具体用途可参考SDK文档
        wmLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;   //设置window type
        wmLayoutParams.format = PixelFormat.RGBA_8888;   //设置图片格式，效果为背景透明

        //设置Window flag
//        wmLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        下面的flags属性的效果形同“锁定”。
//        悬浮窗不可触摸，不接受任何事件, 同时不影响后面的事件响应。
        wmLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;


        wmLayoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;   //调整悬浮窗口至左上角，便于调整坐标
        //以屏幕左上角为原点，设置x、y初始值
//        wmLayoutParams.x = 0;
//        wmLayoutParams.y = 0;
        //设置悬浮窗口长宽数据
//        wmLayoutParams.width = 400;
//        wmLayoutParams.height = 400;
        //显示myFloatView图像
        windowManager.addView(window, wmLayoutParams);
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
