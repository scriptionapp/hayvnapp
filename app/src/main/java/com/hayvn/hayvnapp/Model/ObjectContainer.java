package com.hayvn.hayvnapp.Model;

public class ObjectContainer {
    Case case_ = null;
    Story story_ = null;
    Attachedfile afile_ = null;

    public ObjectContainer(Case case_){
        this.case_ = case_;
    }

    public ObjectContainer(Attachedfile afile_){
        this.afile_ = afile_;
    }

    public ObjectContainer(Story story_){
        this.story_ = story_;
    }

    public Story getStory_() {
        return story_;
    }

    public Case getCase_() {
        return case_;
    }

    public Attachedfile getAfile_() {
        return afile_;
    }
}
