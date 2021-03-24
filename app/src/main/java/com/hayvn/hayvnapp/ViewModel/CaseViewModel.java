package com.hayvn.hayvnapp.ViewModel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.CaseStoryFileCount;
import com.hayvn.hayvnapp.Repository.CaseRepo;

import java.util.List;

public class CaseViewModel extends AndroidViewModel {
    private CaseRepo mRepository;

    public CaseViewModel (Application application) {
        super(application);
        mRepository = new CaseRepo(application);
    }

    public LiveData<List<CaseStoryFileCount>> getCasesCountsForCase_vm() { return mRepository.getCasesCountsForCase_repo(); }
    public LiveData<List<CaseStoryFileCount>> getCasesCountsForCase_filtered(SimpleSQLiteQuery filt) {
        return mRepository.getUserViaQuery(filt);
    }

    public LiveData<Case> getCaseById(int id) { return mRepository.findCaseByCid(id); }

    public void insert(Case word) { mRepository.insert(word); }
}
