package com.chaoxing.demo.audioplayer.subject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by HuWei on 2017/6/27.
 */

public class SourceConfig implements Parcelable {

    public String weblink;

    protected SourceConfig(Parcel in) {
        weblink = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(weblink);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SourceConfig> CREATOR = new Creator<SourceConfig>() {
        @Override
        public SourceConfig createFromParcel(Parcel in) {
            return new SourceConfig(in);
        }

        @Override
        public SourceConfig[] newArray(int size) {
            return new SourceConfig[size];
        }
    };

    public String getWeblink() {
        return weblink;
    }

    public void setWeblink(String weblink) {
        this.weblink = weblink;
    }

}
