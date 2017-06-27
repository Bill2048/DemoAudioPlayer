package com.chaoxing.demo.audioplayer.subject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by HuWei on 2017/6/27.
 */

public class SubjectAudioContent implements Parcelable {

    private String mp3;
    private String mediaPathIOS;

    protected SubjectAudioContent(Parcel in) {
        mp3 = in.readString();
        mediaPathIOS = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mp3);
        dest.writeString(mediaPathIOS);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SubjectAudioContent> CREATOR = new Creator<SubjectAudioContent>() {
        @Override
        public SubjectAudioContent createFromParcel(Parcel in) {
            return new SubjectAudioContent(in);
        }

        @Override
        public SubjectAudioContent[] newArray(int size) {
            return new SubjectAudioContent[size];
        }
    };

    public String getMp3() {
        return mp3;
    }

    public void setMp3(String mp3) {
        this.mp3 = mp3;
    }

    public String getMediaPathIOS() {
        return mediaPathIOS;
    }

    public void setMediaPathIOS(String mediaPathIOS) {
        this.mediaPathIOS = mediaPathIOS;
    }

}
