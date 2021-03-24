package com.hayvn.hayvnapp.ViewModel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.hayvn.hayvnapp.Model.User;
import com.hayvn.hayvnapp.Repository.UserRepo;

public class UserViewModel extends AndroidViewModel {
    private UserRepo mRepository;
    
    public UserViewModel(Application application) {
        super(application);
        mRepository = new UserRepo(application);
    }

    public LiveData<User> getSettings() {
        return mRepository.getSetting();
    }
}