package com.chaoxing.demo.audioplayer;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by HuWei on 2017/6/14.
 */

public class BaseApplication extends Application {

    private boolean mAudioServiceBound;
    private AudioPlayerService mAudioPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        bindMediaService();
    }

    public void initCourseAudioPlayer() {

    }

    public void bindMediaService() {
        if (!mAudioServiceBound) {
            Intent playerIntent = new Intent(this, AudioPlayerService.class);
//        startService(playerIntent);
            getApplicationContext().bindService(playerIntent, mAudioServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unBindAudioService() {
        if (mAudioServiceBound && mAudioServiceConnection != null) {
            unbindService(mAudioServiceConnection);
            mAudioServiceBound = false;
            mAudioPlayer.stopSelf();
        }
    }

    private ServiceConnection mAudioServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            mAudioPlayer = binder.getService();
            mAudioServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAudioServiceBound = false;
        }
    };

    public AudioPlayerService getAudioPlayer() {
        return mAudioPlayer;
    }
}
