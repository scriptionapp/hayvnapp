package com.hayvn.hayvnapp.Model;

import androidx.room.Embedded;

public class CaseStoryFileCount {
    @Embedded
    Case case_;

    long storycount;
    long filecount;

    public long getStorycount() {
        return storycount;
    }
    public void setStorycount(long cnt) {
        this.storycount = cnt;
    }

    public long getFilecount() {
        return filecount;
    }
    public void setFilecount(long cnt) {
        this.filecount = cnt;
    }

    public Case getCase_() { return case_;  }
    public void setCase_(Case case_) {  this.case_ = case_;  }
}
