package com.hayvn.hayvnapp.Repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.sqlite.db.SimpleSQLiteQuery;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firestore.v1.WriteResult;
import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.FirebaseConstants;
import com.hayvn.hayvnapp.Constant.MethodConstants;
import com.hayvn.hayvnapp.Constant.RoomConstants;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.DataBase.AppDB;
import com.hayvn.hayvnapp.FirebaseHelper.FirestoreShard;
import com.hayvn.hayvnapp.Helper.NetworkStateChangeBroadcaster;
import com.hayvn.hayvnapp.Helper.PullData;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Interface.AttachFileDAO;
import com.hayvn.hayvnapp.Interface.CaseNamesDAO;
import com.hayvn.hayvnapp.Interface.StoryDAO;
import com.hayvn.hayvnapp.Interfaces.CallbackCaseInserted;
import com.hayvn.hayvnapp.Interfaces.CallbackCompoundStringInt;
import com.hayvn.hayvnapp.Interfaces.CallbackInt;
import com.hayvn.hayvnapp.Interfaces.CallbackReceivedCases;
import com.hayvn.hayvnapp.Interfaces.CallbackReceivedDataGeneral;
import com.hayvn.hayvnapp.Interfaces.CallbackObjectUpdated;
import com.hayvn.hayvnapp.Model.Attachedfile;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.CaseStoryFileCount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.hayvn.hayvnapp.Model.CompoundStringInt;
import com.hayvn.hayvnapp.Model.ObjectContainer;
import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.ProdDev.Modes;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;
import io.reactivex.schedulers.Schedulers;

import static com.hayvn.hayvnapp.Constant.TextConstants.PROBLEM_INTERNET;

public class CaseRepo {
    private static final String TAG = "CASE_REPO";
    public static FirebaseFirestore mFirestore;
    private static CaseNamesDAO mCaseDao;
    private static FirebaseAuth mAuth;
    private Application application;
    private AppDB db;
    private Disposable disposable;

    public CaseRepo(Application application) {
        this.application = application;
        db = AppDB.getAppDatabase(application);
        mCaseDao = db.caseNamesDAO();
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    private void completeDispose(){
        if(disposable!=null) disposable.dispose();
    }

    public void getCountCases(CallbackCompoundStringInt callback, String uid) {
        new getCountCasesAsyncTask(callback, uid).execute();
    }
    private static class getCountCasesAsyncTask extends AsyncTask<Void, Void, CompoundStringInt>
    {
        CallbackCompoundStringInt callback;
        String uid;
        private WeakReference<CaseNamesDAO> mAsyncTaskDao;

        getCountCasesAsyncTask(CallbackCompoundStringInt callback, String uid) {
            mAsyncTaskDao = new WeakReference<>(mCaseDao);
            this.callback = callback;
            this.uid = uid;
        }

        @Override
        protected CompoundStringInt doInBackground(Void... url) {
            return mAsyncTaskDao.get().getCountForUserDoctorId(uid);
        }

        @Override
        protected void onPostExecute(CompoundStringInt result) {
            callback.callbackGetCompoundStringInt(result.getName(), result.getCounter());
        }
    }

    public LiveData<List<CaseStoryFileCount>> getCasesCountsForCase_repo(){
        return mCaseDao.getCasesCountsForCase();
    }
    public LiveData<List<CaseStoryFileCount>> getCasesCountsForCase_filt(String filt){
        return mCaseDao.getCasesCountsForCaseFilt(filt);
    }
    public LiveData<List<CaseStoryFileCount>> getUserViaQuery(SimpleSQLiteQuery filt){
        return mCaseDao.getUserViaQuery(filt);
    }


    private static Case setUpdateCreate(Case case_){
        Long now = new Date().getTime();
        if(case_.getCreatedAt() == null || case_.getCreatedAt() == 0){
            case_.setCreatedAt(now);
        }
        return case_;
    }

    public Case setUpdateCreate(Case case_, boolean with_updated_at, boolean with_uid){
        Long now = new Date().getTime();
        if(case_.getCreatedAt() == null || case_.getCreatedAt() == 0){
            case_.setCreatedAt(now);
        }
        if(with_updated_at){
            case_.setUpdatedAt(now);
            if(with_uid && mAuth != null && mAuth.getUid() != null) case_.setUserId(mAuth.getUid());
        }
        return case_;
    }

    private static Case setUpdateFirestore(Case case_){
        Long now = new Date().getTime();
        case_.setUpdatedFirebaseAt(now);
        return case_;
    }

    public LiveData<Case> findCaseByCid(int cid) { return mCaseDao.findById(cid); }

    private static void addToFavourites(Case newCase,Application application){
        ArrayList<String> curr = SharedPrefHelper.getStringList(application.getBaseContext(), SharedprefConstants.FAVOURITES_LIST);
        if(curr == null){
            curr = new ArrayList<>();
        }
        curr.add(String.valueOf(newCase.getCid()));
        SharedPrefHelper.writeStringList(application.getBaseContext(), SharedprefConstants.FAVOURITES_LIST, curr);

    }
    public void insert (Case case_) {
        case_ = setUpdateCreate(case_);
        new insertAsyncTask(mCaseDao, null,application).execute(case_);
    }
    public void insertWithCallback (Case case_, CallbackCaseInserted callbackCaseInserted) {
        case_ = setUpdateCreate(case_);
        new insertAsyncTask(mCaseDao, callbackCaseInserted,application).execute(case_);
    }
    private static class insertAsyncTask extends AsyncTask<Case, Void, Void> {

        private WeakReference<CaseNamesDAO> mAsyncTaskDao;
        private CallbackCaseInserted callbackCaseInserted;
        private Case newCase;
        private WeakReference<Application> applicationWeakReference;

        insertAsyncTask(CaseNamesDAO dao, CallbackCaseInserted callbackCaseInserted,Application application) {
            mAsyncTaskDao = new WeakReference<>(dao);
            this.callbackCaseInserted = callbackCaseInserted;
            applicationWeakReference = new WeakReference<>(application);
        }

        @Override
        protected Void doInBackground(final Case... params) {
            newCase = params[0];
            long newId = mAsyncTaskDao.get().insertCase(newCase);
            newCase.setCid((int) newId);
            addToFavourites(newCase,applicationWeakReference.get());
            syncCaseToFirestore(newCase, null);
            return null;
        }

        @Override
        protected void onPostExecute(Void avoid){
            if(callbackCaseInserted != null) callbackCaseInserted.callbackCaseWasInserted(newCase);
        }
    }

    public void getCasesByQuery(CallbackReceivedDataGeneral callback_general, String query_type) {
        new GetUnsyncedCasesAsyncTask(callback_general, query_type, null).execute();
    }
    public static void getCasesByQueryWithFilt(CallbackReceivedDataGeneral callback_general, String query_type, List<String> filter_str) {
        new GetUnsyncedCasesAsyncTask(callback_general, query_type, filter_str).execute();
    }
    private static class GetUnsyncedCasesAsyncTask extends AsyncTask<Void, Void, List<Case>>
    {
        CallbackReceivedDataGeneral callback_general;
        String query_type;
        List<String> filter_str;
        private WeakReference<CaseNamesDAO> mAsyncTaskDao;

        GetUnsyncedCasesAsyncTask(CallbackReceivedDataGeneral callback_general, String query_type, List<String> filter_str) {
            mAsyncTaskDao = new WeakReference<>(mCaseDao);
            this.callback_general = callback_general;
            this.query_type = query_type;
            this.filter_str = filter_str;
        }

        @Override
        protected List<Case> doInBackground(Void... url) {
            if(query_type.equals(RoomConstants.QUERY_UNSYNCED)){
                return mAsyncTaskDao.get().getUnsynced();
            }else if(query_type.equals(RoomConstants.QUERY_BY_FBID)){
                return mAsyncTaskDao.get().getByFbIds(filter_str);
            }else{
                return new ArrayList<Case>();
            }
        }

        @Override
        protected void onPostExecute(List<Case> result) {
            callback_general.callbackReceivedData(result, null, null);
        }
    }

    /////////////////////////////////////////////////////////////////////
    public void insertAllFromFire (List<Case> cases, PullData pd, boolean watch_fbid_conflict, List<String> ids_to_delete_later) {
        new insertAllFromFireAsyncTask(mCaseDao, pd, watch_fbid_conflict, cases, ids_to_delete_later).execute();
    }
    private static class insertAllFromFireAsyncTask extends AsyncTask<Void, Void, Void> implements CallbackReceivedDataGeneral{

        private WeakReference<CaseNamesDAO> mAsyncTaskDao;
        private PullData pd;
        private boolean watch_fbid_conflict;
        private boolean ready_to_go_to_next = false;
        private List<Case> cases;
        private CaseNamesDAO dao;
        private List<String> ids_to_delete_later;

        insertAllFromFireAsyncTask(CaseNamesDAO dao, PullData pd, boolean watch_fbid_conflict, List<Case> cases, List<String> ids_to_delete_later) {
            this.dao = dao;
            mAsyncTaskDao = new WeakReference<>(dao);
            this.pd = pd;
            this.watch_fbid_conflict = watch_fbid_conflict;
            this.cases = cases;
            this.ids_to_delete_later = ids_to_delete_later;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if(watch_fbid_conflict){
                List<String> fbids = new ArrayList<String>();
                for(Case cs: cases){fbids.add(cs.getFbId());}
                getCasesByQueryWithFilt(this, RoomConstants.QUERY_BY_FBID, fbids);
                //calls callbackReceivedData
                return null;
            }else{
                mAsyncTaskDao.get().insertAll(cases);
                ready_to_go_to_next = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute (Void result){
            if(ids_to_delete_later != null) RepoUtils.cleanUpSyncInFirestoreForUser(
                    mFirestore, ids_to_delete_later, mAuth.getUid(),
                    FirebaseConstants.CASES_COLLECTION_PATH);
            if(ready_to_go_to_next) pd.cleanUpAndPopulateStories();
        }

        @Override
        public void callbackReceivedData(List<Case> cases_, List<Story> stories_,
                                         List<Attachedfile> afiles_) {
            //local received data
           if(cases_ != null){
                List<Case> newcases = new ArrayList<Case>();
                List <String> fbids_already_indb = new ArrayList<String>();
                List <Integer> local_ids = new ArrayList<Integer>();
                for(Case cs: cases_){
                    fbids_already_indb.add(cs.getFbId());
                    local_ids.add(cs.getCid());
                }
                for(Case cs: cases){
                    if(!fbids_already_indb.contains(cs.getFbId())){
                        cs.setCid(0);
                        newcases.add(cs);
                    }else{
                        int i = fbids_already_indb.indexOf(cs.getFbId());
                        cs.setCid(local_ids.get(i));
                        newcases.add(cs);
                    }
                }
                cases = newcases;
            }
            watch_fbid_conflict = false;
            new insertAllFromFireAsyncTask(dao, pd, watch_fbid_conflict, cases, ids_to_delete_later).execute();
        }
    }
    /////////////////////////////////////////////////////////////////////

    public void getUnsyncedEverything(CallbackReceivedDataGeneral callback_received){
        WeakReference<CaseNamesDAO> mAsyncCaseDao = new WeakReference<>(mCaseDao);

        StoryDAO mStoryDao = db.storyDAO();
        WeakReference<StoryDAO> mAsyncStoryDao = new WeakReference<>(mStoryDao);

        AttachFileDAO mFileDao = db.attachFileDAO();
        WeakReference<AttachFileDAO> mAsyncFileDao = new WeakReference<>(mFileDao);

        Maybe<List<Case>> obs1 = mAsyncCaseDao.get().getUnsyncedMaybeRX()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Maybe<List<Story>> obs2 = mAsyncStoryDao.get().getUnsyncedMaybeRX()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Maybe<List<Attachedfile>> obs3 =mAsyncFileDao.get().getUnsyncedMaybeRX()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        disposable = Maybe.zip(obs1, obs2,obs3,
                new Function3<List<Case>, List<Story>, List<Attachedfile>, String>(){
                    @Override
                    public String apply(List<Case> l1, List<Story> l2, List<Attachedfile> l3) throws Exception{
                        callback_received.callbackReceivedData(l1, l2, l3);
                        return "sss";
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        completeDispose();
                    }
                });

    }

    public void getCasesByCids(CallbackReceivedCases callback_cases, List<Integer> cids){
        WeakReference<CaseNamesDAO> mAsyncCaseDao = new WeakReference<>(mCaseDao);
        disposable = mAsyncCaseDao.get().getByCidsMaybeRX(cids)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Case>>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull List<Case> cases) throws Exception {
                        callback_cases.onCallbackReceivedCases(cases);
                        completeDispose();
                    }
                });
    }

    public void storeFavourites(ArrayList<String> new_fbids, ArrayList<String> removed_fbids){

        Map<String, Object> update = new HashMap<>();
        for(String st: new_fbids){
            update.put(st, true);
        }
        for(String st: removed_fbids){
            update.put(st, false);
        }
        mFirestore.collection(FirebaseConstants.FAVOURITES_COLLECTION_PATH)
                        .document(Objects.requireNonNull(mAuth.getUid()))
                        .set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                SharedPrefHelper.writeStringList(application.getBaseContext(), SharedprefConstants.FAVOURITES_LIST, null);
                SharedPrefHelper.writeStringList(application.getBaseContext(), SharedprefConstants.REMOVED_FROM_FAVOURITES_LIST, null);
            }
        });
    }

    public void update (Case case_) {
        Case case__ = setUpdateCreate(case_);
        new updateAsyncTask(mCaseDao, true, null).execute(case__);
    }
    public void updateWithoutTime(Case case_) {
        new updateAsyncTask(mCaseDao, true, null).execute(case_);
    }

    public static void update(Case case_, boolean with_firestore) {
        case_ = setUpdateCreate(case_);
        new updateAsyncTask(mCaseDao, with_firestore, null).execute(case_);
    }
    public void update (Case case_, boolean with_firestore, CallbackObjectUpdated listener) {
        Case case__ = setUpdateCreate(case_);
        new updateAsyncTask(mCaseDao, with_firestore, listener).execute(case__);
    }

    private static class updateAsyncTask extends AsyncTask<Case, Void, Void> {

        private WeakReference<CaseNamesDAO> mAsyncTaskDao;
        private boolean with_firestore;
        CallbackObjectUpdated listener;
        Case newCase;

        updateAsyncTask(CaseNamesDAO dao, boolean with_firestore, CallbackObjectUpdated listener) {
            mAsyncTaskDao = new WeakReference<>(dao);
            this.with_firestore = with_firestore;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(final Case... params) {
            newCase = params[0];
            mAsyncTaskDao.get().updateCaseNames(newCase);
            if(with_firestore) syncCaseToFirestore(newCase, listener);
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            // only called after local updates
            if(listener != null) listener.onObjectUpdatedLocally(new ObjectContainer(newCase));
        }
    }

    public void delete (Case word) {
        new deleteAsyncTask(mCaseDao,application).execute(word);
    }
    private static class deleteAsyncTask extends AsyncTask<Case, Void, Void> {

        private CaseNamesDAO mAsyncTaskDao;
        private StoryRepo storyRepo ;
        private WeakReference<Application> applicationWeakReference;

        deleteAsyncTask(CaseNamesDAO dao,Application application) {
            mAsyncTaskDao = dao;
            applicationWeakReference = new WeakReference<>(application);
        }

        @Override
        protected Void doInBackground(final Case... params) {
            mAsyncTaskDao.delete(params[0]);
            if(Modes.IS_ADMIN){
                deleteCaseInFire(params[0]);
            }
            storyRepo = new StoryRepo(applicationWeakReference.get());
            storyRepo.deleteStoriesOfCase(params[0]);
            return null;
        }
    }


    public static void syncCaseToFirestore(Case case1, CallbackObjectUpdated listener) {
        // If creating case for first time, then we let firebase set the id and assign locally
        // Otherwise, use the local id to update the case
        boolean isNetworkAvailable = NetworkStateChangeBroadcaster.getIsConnected();
        case1 = setUpdateFirestore(case1);
        if(Constant.IGNORE_NETWORK_AVAIL || isNetworkAvailable){
            if (case1.getFbId() == null || case1.getFbId().equals("") ) { //NEW
                addCaseToFirestore(case1, listener);
            } else { //already exists, this is an update
                updCaseInFirestore(case1, listener);
            }
        }else{
            if(listener!=null){
                listener.onObjectGotFireId(new ObjectContainer(case1), false);
            }
        }
    }

    private static void addCaseToFirestore(Case case1, CallbackObjectUpdated listener){
        DocumentReference new_doc = mFirestore.collection(FirebaseConstants.CASES_COLLECTION_PATH).document();
        String new_id = new_doc.getId();
        case1.setFbId(new_id);
        new_doc.set(case1).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    update(case1, false);
                    if(listener != null) listener.onObjectGotFireId(new ObjectContainer(case1), true);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "Failed creating case, cid: " + case1.getCid());
                }
            });

    }

    private static void updCaseInFirestore(Case case1, CallbackObjectUpdated listener){
        mFirestore.collection(FirebaseConstants.CASES_COLLECTION_PATH)
            .document(case1.getFbId())
            .set(case1, SetOptions.merge())
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    update(case1, false);
                    if(listener != null) listener.onObjectGotFireId(new ObjectContainer(case1), true);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "failed to update case_id: " + case1.getFbId(), e);
                }
            });
    }

    //https://firebase.google.com/docs/firestore/manage-data/delete-data
    private static void deleteCaseInFire(Case toDelete) {
        mFirestore.collection(FirebaseConstants.CASES_COLLECTION_PATH).document(toDelete.getFbId())
                .delete();
    }

    private void setFavourites(ArrayList<String> fav_fbids){
        WeakReference<CaseNamesDAO> mAsyncCaseDao = new WeakReference<>(mCaseDao);
        mAsyncCaseDao.get().updateFavourites(fav_fbids).subscribeOn(Schedulers.io()).subscribe();
    }

    public void findFavouriteCases(){
        if(mAuth.getUid()!=null)
        mFirestore.collection(FirebaseConstants.FAVOURITES_COLLECTION_PATH)
                .document(mAuth.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot snapshot = task.getResult();
                        assert snapshot != null;
                        Map<String, Object> hm = snapshot.getData();
                        ArrayList<String> favourites = new ArrayList<String>();

                        if(hm != null) {
                            for (String st : hm.keySet()) {
                                try {
                                    if ((boolean) hm.get(st)) {
                                        favourites.add(st);
                                    }
                                } catch (Exception e) {

                                }
                            }
                        }
                        setFavourites(favourites);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
    }


    public void populateCasesFromFirestore(PullData pd, String pull_what){
        boolean isNetworkAvailable = NetworkStateChangeBroadcaster.getIsConnected();
        CaseRepo cr = this;
        if(isNetworkAvailable) {
            Query getter_link = RepoUtils.getQueryLink(pd, mFirestore, FirebaseConstants.CASES_COLLECTION_PATH, pull_what, mAuth.getUid());
            RepoUtils.getCasesFromGetterLink(getter_link, pd, null, cr);
            if(pull_what.equals(MethodConstants.PULL_WHAT_ALL) || pull_what.equals(MethodConstants.PULL_WHAT_ALL_RECENT)){
                Query getter_link_type = mFirestore.collection(FirebaseConstants.STORIES_COLLECTION_PATH)
                        .whereEqualTo(FirebaseConstants.STORY_TYPE, RoomConstants.STORY_TYPE_PROFILE_PHOTO);
                StoryRepo sr = new StoryRepo(application);
                RepoUtils.getStoriesFromGetterLink(getter_link_type, pd, null, sr);
            }
        }else{
            Toast.makeText(application.getBaseContext(),
                    PROBLEM_INTERNET, Toast.LENGTH_SHORT).show();
        }
    }

    public void syncMissingCasesByFbid(List<String> missing_cases_fbid){
        if(missing_cases_fbid != null && missing_cases_fbid.size()>10){
            int latest = 0; int limit_index = 0;
            while((latest +10) <= missing_cases_fbid.size()){
                limit_index = Math.min( missing_cases_fbid.size(),  latest +10);
                List<String> sub_array = missing_cases_fbid.subList(latest, limit_index); //incl excl
                syncMissingCasesBatch(sub_array);
                latest += 10;
            }
        }else if(missing_cases_fbid != null){
            syncMissingCasesBatch(missing_cases_fbid);
        }
    }

    private void syncMissingCasesBatch(List<String> missing_cases_fbid_10){
        if(missing_cases_fbid_10.size()<=10) {
            Query getter_link = mFirestore.collection(FirebaseConstants.CASES_COLLECTION_PATH)
                    .whereIn(FirebaseConstants.FB_ID, missing_cases_fbid_10);
            RepoUtils.getCasesFromGetterLink(getter_link, null, null, this);
        }else{
            Log.d(TAG, "Error in whereIn query");
        }
    }

    public void fixCasesStories(List<String> duplicated_fbids, List<Integer> cids_to_delete, List<Integer> cids_to_assign){
        int i = 0;
        int sz = duplicated_fbids.size();
        WeakReference<CaseNamesDAO> mAsyncCaseDao = new WeakReference<>(mCaseDao);
        StoryDAO mStoryDao = db.storyDAO();
        WeakReference<StoryDAO> mAsyncStoryDao = new WeakReference<>(mStoryDao);

        for(i = 0; i <sz; i++){
            String s = duplicated_fbids.get(i);
            int c = cids_to_assign.get(i);
            int wrongc = cids_to_delete.get(i);
            if( s!=null && c!=0 ){
                mAsyncStoryDao.get().setCorrectCid(s, c).subscribeOn(Schedulers.io()).subscribe();
                mAsyncStoryDao.get().setCorrectCid(wrongc, c).subscribeOn(Schedulers.io()).subscribe();
                mAsyncCaseDao.get().deleteDuplicate(wrongc).subscribeOn(Schedulers.io()).subscribe();
            }
        }
    }
}
