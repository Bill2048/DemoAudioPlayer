package com.chaoxing.demo.audioplayer;

/**
 * Created by HuWei on 2017/6/23.
 */

public interface OnPlayStatusChangedListener {

    void onStart();

    void onPause();

    void onStop();

    void onPlayPositionChanged(int position, int length);

    void onCompleted();

}
