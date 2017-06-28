package com.chaoxing.demo.audioplayer.subject;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;

import com.chaoxing.demo.audioplayer.Audio;
import com.chaoxing.demo.audioplayer.AudioContentRequestCallbacks;
import com.chaoxing.demo.audioplayer.AudioContentRequester;
import com.chaoxing.demo.audioplayer.AudioPlayerController;
import com.chaoxing.demo.audioplayer.AudioPlayerServiceBindCallbacks;
import com.chaoxing.demo.audioplayer.util.Utils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HuWei on 2017/6/27.
 */

public class SubjectAudioHelper {

    private final static SubjectAudioHelper sInstance = new SubjectAudioHelper();

    private AudioList mAudioList;

    public static SubjectAudioHelper getInstance() {
        return sInstance;
    }

    private List<SubjectAudioProfile> profileList = new ArrayList<>();

    public void play(Context context, AudioList audioList) {
        mAudioList = audioList;
        if (!AudioPlayerController.getInstance().isAudioServiceBound()) {
            AudioPlayerController.getInstance().bindMediaService(context, mAudioPlayerServiceBindCallbacks);
        } else {
            play();
        }
    }

    private void play() {
        AudioPlayerController.getInstance().setAudioContentRequester(mAudioContentRequester);
        List<Audio> audioList = new ArrayList<>();
        for (SubjectAudioProfile profile : mAudioList.getList()) {
            Audio audio = new Audio();
            audio.setTitle(profile.getMediaTitle());
            audioList.add(audio);
        }
        profileList.clear();
        profileList.addAll(mAudioList.getList());
        AudioPlayerController.getInstance().play(System.currentTimeMillis(), mAudioList.getTitle(), audioList, mAudioList.getActiveIndex());
    }

    AudioPlayerServiceBindCallbacks mAudioPlayerServiceBindCallbacks = new AudioPlayerServiceBindCallbacks() {
        @Override
        public void onBound() {
            play();
        }

        @Override
        public void onUnbound() {

        }
    };

    AudioContentRequester mAudioContentRequester = new AudioContentRequester() {
        @Override
        public void request(final long playlistId, final int index, final AudioContentRequestCallbacks callbacks) {
            AsyncTask<SubjectAudioProfile, Integer, String> requestTask = new AsyncTask<SubjectAudioProfile, Integer, String>() {
                @Override
                protected void onPreExecute() {
                    if (callbacks != null) {
                        callbacks.onStart(playlistId, index);
                    }
                }

                @Override
                protected String doInBackground(SubjectAudioProfile... params) {
                    String result = null;
                    try {
                        result = Utils.loadString(params[0].getMediaInfoUrl());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return result;
                }

                @Override
                protected void onPostExecute(String data) {
                    if (data != null && data.length() != 0) {
                        AudioContentResult result = new Gson().fromJson(data, AudioContentResult.class);
                        if (result != null) {
                            if (result.getResult() == 1) {
                                if (callbacks != null) {
                                    callbacks.onCompleted(playlistId, index, result.getData().getMp3());
                                }
                            } else {
                                if (callbacks != null) {
                                    callbacks.onError(playlistId, index, null, result.getMsg());
                                }
                            }
                        }
                    } else {
                        if (callbacks != null) {
                            callbacks.onCompleted(playlistId, index, null);
                        }
                    }
                }
            };
            SubjectAudioProfile profile = profileList.get(index);
            AsyncTaskCompat.executeParallel(requestTask, profile);
        }
    };

}
