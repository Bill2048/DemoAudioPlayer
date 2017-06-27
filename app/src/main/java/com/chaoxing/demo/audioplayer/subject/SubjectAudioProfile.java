package com.chaoxing.demo.audioplayer.subject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by HuWei on 2017/6/27.
 */

public class SubjectAudioProfile implements Parcelable {

    private String mediaId;
    private String mediaTitle;
    private String mediaInfoUrl;

    protected SubjectAudioProfile(Parcel in) {
        mediaId = in.readString();
        mediaTitle = in.readString();
        mediaInfoUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mediaId);
        dest.writeString(mediaTitle);
        dest.writeString(mediaInfoUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SubjectAudioProfile> CREATOR = new Creator<SubjectAudioProfile>() {
        @Override
        public SubjectAudioProfile createFromParcel(Parcel in) {
            return new SubjectAudioProfile(in);
        }

        @Override
        public SubjectAudioProfile[] newArray(int size) {
            return new SubjectAudioProfile[size];
        }
    };

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaTitle() {
        return mediaTitle;
    }

    public void setMediaTitle(String mediaTitle) {
        this.mediaTitle = mediaTitle;
    }

    public String getMediaInfoUrl() {
        return mediaInfoUrl;
    }

    public void setMediaInfoUrl(String mediaInfoUrl) {
        this.mediaInfoUrl = mediaInfoUrl;
    }
}
