package com.hayvn.hayvnapp.Interfaces;

import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.Model.StoryFileCount;

import java.util.List;

public interface CallbackLocalStories {
    void callbackGetLocalStories(List<StoryFileCount> result1, List<Story> result2);
}
