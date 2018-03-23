package com.railscon.englishnotebook.model;

import android.os.Parcel;
import android.os.Parcelable;

public class DataItem implements Parcelable {

    private int id;
    private String cTitle;
    private String cBody;
    private String cVoice;
    private String cCreated;
    private int cSort;
    private int cRank;
    private String cTags;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCTitle() {
        return cTitle;
    }

    public void setCTitle(String cTitle) {
        this.cTitle = cTitle;
    }

    public String getCBody() {
        return cBody;
    }

    public void setCBody(String cBody) {
        this.cBody = cBody;
    }

    public String getCVoice() {
        return cVoice;
    }

    public void setCVoice(String cVoice) {
        this.cVoice = cVoice;
    }

    public String getCCreated() {
        return cCreated;
    }

    public void setCCreated(String cCreated) {
        this.cCreated = cCreated;
    }

    public int getCSort() {
        return cSort;
    }

    public void setCSort(int cSort) {
        this.cSort = cSort;
    }

    public int getCRank() {
        return cRank;
    }

    public void setCRank(int cRank) {
        this.cRank = cRank;
    }

    public String getCTags() {
        return cTags;
    }

    public void setCTags(String cTags) {
        this.cTags = cTags;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.cTitle);
        dest.writeString(this.cBody);
        dest.writeString(this.cVoice);
        dest.writeString(this.cCreated);
        dest.writeInt(this.cSort);
        dest.writeInt(this.cRank);
        dest.writeString(this.cTags);
    }

    public DataItem() {
    }

    protected DataItem(Parcel in) {
        this.id = in.readInt();
        this.cTitle = in.readString();
        this.cBody = in.readString();
        this.cVoice = in.readString();
        this.cCreated = in.readString();
        this.cSort = in.readInt();
        this.cRank = in.readInt();
        this.cTags = in.readString();
    }

    public static final Parcelable.Creator<DataItem> CREATOR = new Parcelable.Creator<DataItem>() {
        @Override
        public DataItem createFromParcel(Parcel source) {
            return new DataItem(source);
        }

        @Override
        public DataItem[] newArray(int size) {
            return new DataItem[size];
        }
    };
}