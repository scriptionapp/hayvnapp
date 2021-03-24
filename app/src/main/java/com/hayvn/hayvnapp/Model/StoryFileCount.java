package com.hayvn.hayvnapp.Model;

import androidx.room.Embedded;
import androidx.room.Entity;

@Entity(tableName = "StoryFileCount")
public class StoryFileCount {
    @Embedded
    Story story;

    long filecount;
    long imagecount;
    long audiocount;

    public long getFilecount() {
        return filecount;
    }
    public void setFilecount(long cnt) {
        this.filecount = cnt;
    }

    public long getImagecount() {
        return imagecount;
    }
    public void setImagecount(long cnt) {
        this.imagecount = cnt;
    }

    public long getAudiocount() { return audiocount;  }
    public void setAudiocount(long cnt) {
        this.audiocount = cnt;
    }

    public Story getStory() { return story;  }
    public void setStory(Story sid) {
        this.story = sid;
    }

}
