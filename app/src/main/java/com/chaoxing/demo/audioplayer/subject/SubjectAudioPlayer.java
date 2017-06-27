package com.chaoxing.demo.audioplayer.subject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
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

public class SubjectAudioPlayer {

    private final static SubjectAudioPlayer sInstance = new SubjectAudioPlayer();

    private AudioList mAudioList;

    public static SubjectAudioPlayer getInstance() {
        return sInstance;
    }

    private List<SubjectAudioProfile> profileList = new ArrayList<>();

    public void play(Context context, AudioList audioList) {
        mAudioList = audioList;
        AudioPlayerController.getInstance().bindMediaService(context, mAudioPlayerServiceBindCallbacks);
    }

    AudioPlayerServiceBindCallbacks mAudioPlayerServiceBindCallbacks = new AudioPlayerServiceBindCallbacks() {
        @Override
        public void onBind() {
            AudioPlayerController.getInstance().setAudioContentRequester(mAudioContentRequester);
            List<Audio> audioList1 = new ArrayList<>();
            for (SubjectAudioProfile profile : mAudioList.getList()) {
                Audio audio = new Audio();
                audio.setTitle(profile.getMediaTitle());
                audioList1.add(audio);
            }
            profileList.clear();
            profileList.addAll(mAudioList.getList());
            AudioPlayerController.getInstance().play(System.currentTimeMillis(), audioList1, mAudioList.getActiveIndex());
        }

        @Override
        public void onUnbind() {

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

class AudioContentResult implements Parcelable {

    private int result;
    private String msg;
    private SubjectAudioContent data;

    protected AudioContentResult(Parcel in) {
        result = in.readInt();
        msg = in.readString();
        data = in.readParcelable(SubjectAudioContent.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(result);
        dest.writeString(msg);
        dest.writeParcelable(data, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AudioContentResult> CREATOR = new Creator<AudioContentResult>() {
        @Override
        public AudioContentResult createFromParcel(Parcel in) {
            return new AudioContentResult(in);
        }

        @Override
        public AudioContentResult[] newArray(int size) {
            return new AudioContentResult[size];
        }
    };

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public SubjectAudioContent getData() {
        return data;
    }

    public void setData(SubjectAudioContent data) {
        this.data = data;
    }
}
