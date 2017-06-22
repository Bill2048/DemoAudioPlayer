package com.chaoxing.demo.audioplayer;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by HuWei on 2017/6/14.
 */

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
    }


    private int mStartActivityCount = 0;
    private boolean mBackground;
    private ActivityLifecycleCallbacks mActivityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            mStartActivityCount++;
            if (mStartActivityCount == 1 && mBackground) {
                Iterator<AppForegroundBackgroundSwitchListener> it = mAppForegroundBackgroundSwitchListenerSet.iterator();
                while (it.hasNext()) {
                    AppForegroundBackgroundSwitchListener listener = it.next();
                    if (listener == null) {
                        it.remove();
                        continue;
                    }
                    listener.onForeground();
                }
            }
            mBackground = false;
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            mStartActivityCount--;
            if (mStartActivityCount <= 0) {
                mBackground = true;
                Iterator<AppForegroundBackgroundSwitchListener> it = mAppForegroundBackgroundSwitchListenerSet.iterator();
                while (it.hasNext()) {
                    AppForegroundBackgroundSwitchListener listener = it.next();
                    if (listener == null) {
                        it.remove();
                        continue;
                    }
                    listener.onBackground();
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };

    private Set<AppForegroundBackgroundSwitchListener> mAppForegroundBackgroundSwitchListenerSet = new HashSet<>();

    public void addAppForegroundBackgroundSwitchListener(AppForegroundBackgroundSwitchListener listener) {
        mAppForegroundBackgroundSwitchListenerSet.add(listener);
    }

    public void removeAppForegroundBackgroundSwitchListener(AppForegroundBackgroundSwitchListener listener) {
        mAppForegroundBackgroundSwitchListenerSet.remove(listener);
    }

    public interface AppForegroundBackgroundSwitchListener {
        void onForeground();

        void onBackground();
    }
}
