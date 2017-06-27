package com.chaoxing.demo.audioplayer;

/**
 * Created by HuWei on 2017/6/27.
 */

public interface AudioContentRequestCallbacks {

    void onStart(long playlistId, int index);

    void onCompleted(long playlistId, int index, String uri);

    void onError(long playlistId, int index, Exception e, String message);

}
