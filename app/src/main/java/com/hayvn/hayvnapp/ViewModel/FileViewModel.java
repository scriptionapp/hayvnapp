package com.hayvn.hayvnapp.ViewModel;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.hayvn.hayvnapp.Model.Attachedfile;
import com.hayvn.hayvnapp.Repository.FileRepo;

import java.util.List;

public class FileViewModel extends AndroidViewModel {
    private FileRepo mRepository;

    public FileViewModel (Application application) {
        super(application);
        mRepository = new FileRepo(application);
    }

    public LiveData<List<Attachedfile>> getStoryFiles(int storyId) {
        return mRepository.getStoryFiles(storyId);
    }

}