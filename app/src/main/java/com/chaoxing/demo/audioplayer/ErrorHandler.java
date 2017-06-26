package com.chaoxing.demo.audioplayer;

/**
 * Created by HuWei on 2017/6/26.
 */

public interface ErrorHandler {

    boolean onErrorWithMediaPlayer(int what, int extra);

    void onError(Exception e);

}
