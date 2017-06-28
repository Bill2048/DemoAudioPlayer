package com.chaoxing.demo.audioplayer.subject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by HuWei on 2017/6/28.
 */

public class AudioContentResult implements Parcelable {

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
