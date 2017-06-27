package com.chaoxing.demo.audioplayer.subject;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by HuWei on 2017/6/27.
 */

public class AudioList implements Parcelable {

    private int sourceType;
    private String title;
    private int activeIndex;
    private List<SubjectAudioProfile> list;

    protected AudioList(Parcel in) {
        sourceType = in.readInt();
        title = in.readString();
        activeIndex = in.readInt();
        list = in.createTypedArrayList(SubjectAudioProfile.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sourceType);
        dest.writeString(title);
        dest.writeInt(activeIndex);
        dest.writeTypedList(list);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AudioList> CREATOR = new Creator<AudioList>() {
        @Override
        public AudioList createFromParcel(Parcel in) {
            return new AudioList(in);
        }

        @Override
        public AudioList[] newArray(int size) {
            return new AudioList[size];
        }
    };

    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
    }

    public List<SubjectAudioProfile> getList() {
        return list;
    }

    public void setList(List<SubjectAudioProfile> list) {
        this.list = list;
    }
}
