package com.example.search;

import android.os.Parcel;
import android.os.Parcelable;

public class WebSites implements Parcelable {
    private String header;
    private String description;
    private String url;

    protected WebSites(Parcel in) {
        header = in.readString();
        description = in.readString();
        url = in.readString();
    }

    public static final Creator<WebSites> CREATOR = new Creator<WebSites>() {
        @Override
        public WebSites createFromParcel(Parcel in) {
            return new WebSites(in);
        }

        @Override
        public WebSites[] newArray(int size) {
            return new WebSites[size];
        }
    };

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public WebSites() {
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(header);
        parcel.writeString(description);
        parcel.writeString(url);
    }
}
