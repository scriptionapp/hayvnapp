package com.hayvn.hayvnapp.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class FileNameProgress implements Parcelable {
    String name;
    String progress;

    public FileNameProgress(String name, String progress) {
        this.name = name;
        this.progress = progress;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getProgress() {
        return progress;
    }
    public void setProgress(String progress) {
        this.progress = progress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(progress);
    }

    public FileNameProgress(Parcel parcel){
        name = parcel.readString();
        progress = parcel.readString();
    }

    //creator - used when un-parceling our parcel (creating the object)
    public static final Parcelable.Creator<FileNameProgress> CREATOR = new Parcelable.Creator<FileNameProgress>(){

        @Override
        public FileNameProgress createFromParcel(Parcel parcel) {
            return new FileNameProgress(parcel);
        }

        @Override
        public FileNameProgress[] newArray(int size) {
            return new FileNameProgress[0];
        }
    };
}
