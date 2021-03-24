package com.hayvn.hayvnapp.ViewModel;

import android.app.Application;

import com.hayvn.hayvnapp.Interface.StoryDAO;
import com.hayvn.hayvnapp.Interfaces.CallbackObjectUpdated;
import com.hayvn.hayvnapp.Model.Attachedfile;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.Model.StoryFileCount;
import com.hayvn.hayvnapp.Repository.FileRepo;
import com.hayvn.hayvnapp.Repository.StoryRepo;

import java.util.List;
import java.util.ArrayList;

public class StoryViewModel extends AndroidViewModel {
    private StoryRepo mRepository;
    private FileRepo mFileRepo;
    private MutableLiveData<Story> mStory;
    private MutableLiveData<List<Attachedfile>> mFileList;
    private StoryDAO mStoryDao;

    public StoryViewModel (Application application) {
        super(application);
        mRepository = new StoryRepo(application);
        mFileRepo = new FileRepo(application);
    }

    public void update(Story story_, CallbackObjectUpdated callback,
                       boolean update_files, boolean with_firebase) {
        mRepository.updateInsert(story_, callback, update_files, with_firebase);
    }

    public void delete(Story word) { mRepository.deleteWithFiles(word, false, false); }

    public MutableLiveData<Story> getNewStory() {
        if (mStory == null) {
            mStory = new MutableLiveData<>();
        }
        return mStory;
    }

    public MutableLiveData<Story> getStory() { return mStory; }

    public LiveData<List<StoryFileCount>> getStoriesFileCountByCid(int id) {
        return mRepository.getStoriesFileCountByCid(id);
    }

    public MutableLiveData<List<Attachedfile>> getFiles() {
        if (mFileList == null) {
            mFileList = new MutableLiveData<List<Attachedfile>>();
            mFileList.setValue(new ArrayList<Attachedfile>());
        }
        return mFileList;
    }

    public LiveData<Story>getProfilePhotoStory(int cid){
        return mRepository.getProfilePhotoCID(cid);
    }

}