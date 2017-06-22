package com.chaoxing.demo.audioplayer;

/**
 * Created by HuWei on 2017/6/22.
 */

public interface PlayCallbacks {

    void onPlay();

    void onPlay(int index);

    void onPrevious();

    void onNext();

    void onProgressChanged(int progress);

    void onShowPlaylist();

}
