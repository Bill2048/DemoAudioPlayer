package com.chaoxing.demo.audioplayer;

/**
 * Created by HuWei on 2017/6/23.
 */

public interface OnPlayStatusChangedListener {

    void onReset();

    void onStart();

    void onPause();

    void onStop();

    void onBufferingUpdate(int percent, int length);

    void onPlayPositionChanged(int position, int length);

    void onCompleted();

}
