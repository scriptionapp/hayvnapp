package com.hayvn.hayvnapp.Interfaces;

import com.hayvn.hayvnapp.Model.Attachedfile;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.Story;

import java.util.List;

public interface CallbackReceivedDataGeneral {
    void callbackReceivedData(List<Case> cases_, List<Story> stories_, List<Attachedfile> afiles_);
}
