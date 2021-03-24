package com.hayvn.hayvnapp.Repository;

import android.app.Application;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.Query;
import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.FirebaseConstants;
import com.hayvn.hayvnapp.Constant.RoomConstants;
import com.hayvn.hayvnapp.DataBase.AppDB;
import com.hayvn.hayvnapp.Helper.NetworkStateChangeBroadcaster;
import com.hayvn.hayvnapp.Helper.PullData;
import com.hayvn.hayvnapp.Interface.AttachFileDAO;
import com.hayvn.hayvnapp.Interface.StoryDAO;
import com.hayvn.hayvnapp.Interfaces.CallbackLocalStories;
import com.hayvn.hayvnapp.Interfaces.CallbackGetFilesForStory;
import com.hayvn.hayvnapp.Interfaces.CallbackObjectUpdated;
import com.hayvn.hayvnapp.Interfaces.CallbackReceivedDataGeneral;
import com.hayvn.hayvnapp.Model.Attachedfile;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.ObjectContainer;
import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.Model.StoryFileCount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.hayvn.hayvnapp.ProdDev.Modes;
import com.hayvn.hayvnapp.Utilities.Utilities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.hayvn.hayvnapp.Constant.FirebaseConstants.FILES_COLLECTION_PATH;
import static com.hayvn.hayvnapp.Constant.TextConstants.PROBLEM_INTERNET;

public class StoryRepo {

    private static final String TAG = "STORY_REPO";
    private StoryDAO mStoryDao;
    private AttachFileDAO mFileDao;
    private static FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private  Application application;
    private   Context context;
    private static Disposable disposable = null;

    public StoryRepo(Application application) {
        AppDB db = AppDB.getAppDatabase(application);
        mStoryDao = db.storyDAO();
        mFileDao = db.attachFileDAO();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        this.application = application;
        context = application.getApplicationContext();
    }

    private static void completeDispose(){
        if(disposable!=null) disposable.dispose();
    }

    public Story setUpdateCreate(Story story_, boolean with_updated_at, boolean with_uid){
        Long now = new Date().getTime();
        if(story_.getCreatedAt() == null || story_.getCreatedAt() == 0){
            story_.setCreatedAt(now);
        }
        if(with_updated_at){
            story_.setUpdatedAt(now);
        }
        if(with_uid && mAuth != null && mAuth.getUid() != null){
            story_.setUserId(mAuth.getUid());
        }
        return story_;
    }

    private Story setUpdateFirestore(Story story_){
        Long now = (new Date()).getTime();
        story_.setUpdatedFirebaseAt(now);
        return story_;
    }

    public LiveData<List<StoryFileCount>> getStoriesFileCountByCid(int cid){
        return mStoryDao.getStoriesFileCountByCid(cid);
    }
    public LiveData<Story> getProfilePhotoCID(int cid){
        return mStoryDao.getProfilePhotoStoryCID(cid);
    }

    private void syncToFirestore(Story story, CallbackObjectUpdated listener, boolean update_files) {

        boolean isNetworkAvailable = NetworkStateChangeBroadcaster.getIsConnected();
        Story story_ = setUpdateFirestore(story);
        if(Constant.IGNORE_NETWORK_AVAIL || isNetworkAvailable) {
            if (story_.getFbId() == null || story_.getFbId().equals("")) {
                addStoryToFirestore(story_, listener, update_files);
            } else {
                updStoryInFirestore(story_, listener, update_files);
            }
        }else{
            Log.w(TAG, "No internet connection to upload story");
        }
    }

    private void addStoryToFirestore(Story story, CallbackObjectUpdated listener, boolean update_files){
        DocumentReference new_doc = mFirestore.collection(FirebaseConstants.STORIES_COLLECTION_PATH).document();
        String new_id = new_doc.getId();
        story.setFbId(new_id);

        mFirestore.collection(FirebaseConstants.STORIES_COLLECTION_PATH).add(story)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        updateInsert(story, null, update_files, false);
                        if(listener != null) listener.onObjectGotFireId(new ObjectContainer(story), true);
                        if(update_files) updateFilesWithStoryFbId(story, new_id);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "SyncStoryToFirestoreWithCallback failed to update story: " + story.toString(), e);
                    }
                });
    }

    private void updStoryInFirestore(Story story, CallbackObjectUpdated listener, boolean update_files){

        mFirestore.collection(FirebaseConstants.STORIES_COLLECTION_PATH)
                .document(story.getFbId()).set(story) //Setoptions.merge
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateInsert(story, null, update_files, false);
                        if(listener != null) listener.onObjectGotFireId(new ObjectContainer(story), true);//TODO ??
                        if(update_files) updateFilesWithStoryFbId(story, story.getFbId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void updateFilesWithStoryFbId(Story story_, String storyFbId){
        FileRepo fr = new FileRepo(application);
        fr.myCallbackFilesForStory = new CallbackGetFilesForStory() {
            @Override
            public void callbackGetFilesForStory(List<Attachedfile> result) {
                FileRepo fr = new FileRepo(application);
                for (Attachedfile af : result) {
                    if(af.getStoryFbId()==null || !af.getStoryFbId().equals(storyFbId)){
                        af.setStoryFbId(storyFbId);
                        fr.insertUpdate(af, null, true, false);
                        //no need to compress images, because only updating IDs
                    }
                }
            }
        };
        fr.getAllFilesForStory(story_); //will call callbackGetFilesForStory
    }


    public void updateInsert(Story story_, CallbackObjectUpdated listener, boolean update_files, boolean with_firebase){
        disposable = Single.create(new SingleOnSubscribe<Long>() {
            @Override
            public void subscribe(SingleEmitter<Long> emitter) throws Exception {
                WeakReference<StoryDAO> mAsyncTaskDao = new WeakReference<>(mStoryDao);
                story_.setUserId(mAuth.getUid());
                try {
                    if(story_.getSid() > 0){
                        mAsyncTaskDao.get().updatestory(story_);
                    }else{
                        int insertId = (int) mAsyncTaskDao.get().insert(story_);
                        story_.setSid(insertId);
                    }
                    emitter.onSuccess(Long.valueOf(0));
                } catch (Throwable t) {
                    emitter.onError(t);
                }

         }})
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribeWith(new DisposableSingleObserver<Long>() {
            @Override
            public void onSuccess(Long id) {
                if(with_firebase) syncToFirestore(story_, listener, update_files);
                if(listener != null) listener.onObjectUpdatedLocally(new ObjectContainer(story_));
                completeDispose();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                completeDispose();
            }
        });
    }


    //Delete -> delete
    public static void deleteWithFiles(Story word,StoryDAO mStoryDao,AttachFileDAO mFileDao,Application application) {
        //no delete from FIREBASE
        new deleteAsyncTask(mStoryDao, mFileDao, false, false,application).execute(word);
    }

    public void deleteWithFiles(Story word, boolean with_firebase, boolean with_media) {
        new deleteAsyncTask(mStoryDao, mFileDao, with_firebase, with_media,application).execute(word);
    }
    public void deleteOldStoriesFilesLocally(){
        long recency = RepoUtils.cutOffRecency(context);
        WeakReference<StoryDAO> mAsyncStoryDao = new WeakReference<>(mStoryDao);
        disposable = mAsyncStoryDao.get().getOldStories(recency)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Story>>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull List<Story> stories) throws Exception {
                        for(Story s: stories) deleteWithFiles(s, false, true);
                        completeDispose();
                    }
                });
    }

    private static class deleteAsyncTask extends AsyncTask<Story, Void, Void> {

        private StoryDAO mAsyncTaskDao;
        private AttachFileDAO mAsyncTaskDaoFile;
        private boolean with_firebase;
        private boolean with_media;
        private WeakReference<Application> applicationWeakReference;


        deleteAsyncTask(StoryDAO dao, AttachFileDAO afdao, boolean with_firebase,
                        boolean with_media,Application context) {
            mAsyncTaskDao = dao;
            mAsyncTaskDaoFile = afdao;
            this.with_firebase = with_firebase;
            this.with_media = with_media;
            applicationWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(final Story... stories) {
            Story storyToDelete = stories[0];
            mAsyncTaskDao.delete(storyToDelete);
            WeakReference<AttachFileDAO> mAsyncFileWeakDao = new WeakReference<>(mAsyncTaskDaoFile);
            if(!with_media){
                mAsyncFileWeakDao.get().deleteFilesForStory(storyToDelete.getSid());
            }else{
                FileRepo file_repo = new FileRepo(applicationWeakReference.get());
                disposable = mAsyncFileWeakDao.get().getBySids(
                        Arrays.asList(storyToDelete.getSid()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<Attachedfile>>() {
                            @Override
                            public void accept(
                                    @io.reactivex.annotations.NonNull List<Attachedfile> files)
                                    throws Exception {
                                FileRepo.deleteListOfFiles(with_media, with_firebase,
                                        applicationWeakReference.get().getApplicationContext(),
                                        files.toArray(new Attachedfile[files.size()]));
                                completeDispose();
                            }
                        });
            }
            if(Modes.IS_ADMIN) {
                if (with_firebase) deleteStoryAndItsFilesInFire(storyToDelete);
            }
            return null;
        }
    }


    public void getSettingsStories(CallbackLocalStories callback_received, int cid){
        WeakReference<StoryDAO> mAsyncStoryDao = new WeakReference<>(mStoryDao);
        disposable = mAsyncStoryDao.get().getProfileStories(cid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Story>>() {
                    @Override
                    public void accept(List<Story> ls) throws Exception {
                        callback_received.callbackGetLocalStories(null, ls);
                        completeDispose();
                    }
                });
    }

    public static void getAllStoriesByQuery(List<Integer> sids,
                                            CallbackLocalStories stories_callback,
                                            CallbackReceivedDataGeneral callback_general,
                                            String query_type, List<String> fbids,StoryDAO storyDAO) {
        WeakReference<StoryDAO> storyDAOWeakReference = new WeakReference<>(storyDAO);
        if(query_type.equals(RoomConstants.QUERY_STORY_FILE_BY_SID)){
            //new GetStoriesFilesByQueryAsyncTask(sids, stories_callback).execute();
            getStoriesBySids(stories_callback, sids,storyDAOWeakReference.get());
        }else{
            new GetStoriesByQueryAsyncTask(callback_general, query_type, fbids,storyDAOWeakReference.get()).execute();
        }

    }
    public  void getAllStoriesByQuery(List<Integer> sids,
                                            CallbackLocalStories stories_callback,
                                            CallbackReceivedDataGeneral callback_general,
                                            String query_type, List<String> fbids) {

        if(query_type.equals(RoomConstants.QUERY_STORY_FILE_BY_SID)){
            getStoriesBySids(stories_callback, sids,mStoryDao);
        }else{
            new GetStoriesByQueryAsyncTask(callback_general, query_type, fbids,mStoryDao).execute();
        }

    }
    private static void getStoriesBySids(CallbackLocalStories callback_received, List<Integer> sids,StoryDAO mStoryDao){
        WeakReference<StoryDAO> mAsyncStoryDao = new WeakReference<>(mStoryDao);
        disposable = mAsyncStoryDao.get().getStoriesFileCountBySids(sids)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<StoryFileCount>>() {
                    @Override
                    public void accept(List<StoryFileCount> result) throws Exception {
                        callback_received.callbackGetLocalStories(result, null);
                        completeDispose();
                    }
                });
    }

    private static class GetStoriesByQueryAsyncTask extends AsyncTask<Void, Void, List<Story>>
    {
        private CallbackReceivedDataGeneral callback_general;
        private String query_type;
        private WeakReference<StoryDAO> mAsyncTaskDao;
        List<String> filter_fbids;

        GetStoriesByQueryAsyncTask(CallbackReceivedDataGeneral callback_general, String query_type, List<String> filter_fbids,StoryDAO mStoryDao) {
            mAsyncTaskDao = new WeakReference<>(mStoryDao);
            this.callback_general = callback_general;
            this.query_type = query_type;
            this.filter_fbids = filter_fbids;
        }

        @Override
        protected List<Story> doInBackground(Void... url) {
            if(query_type.equals(RoomConstants.QUERY_UNSYNCED)){
                return mAsyncTaskDao.get().getUnsynced();
            }else if(query_type.equals(RoomConstants.QUERY_BY_FBID)){
                return mAsyncTaskDao.get().getByFbIds(filter_fbids);
            }else{
                return new ArrayList<Story>();
            }
        }

        @Override
        protected void onPostExecute(List<Story> result) {
            callback_general.callbackReceivedData(null, result, null);
        }
    }


    public void deleteStoriesOfCase(Case case_){new deleteStoriesForCaseAsyncTask(mStoryDao,mFileDao,application).execute(case_);}
    private static class deleteStoriesForCaseAsyncTask extends AsyncTask<Case, Void, List<Story>> {

        private StoryDAO mAsyncTaskDao;
        private Case caseToDelete;
        private AttachFileDAO mFileDao;
        private WeakReference<Application> applicationWeakReference;

        deleteStoriesForCaseAsyncTask(StoryDAO dao,AttachFileDAO mFileDao,Application application) {
            mAsyncTaskDao = dao;
            this.mFileDao = mFileDao;
            applicationWeakReference = new WeakReference<>(application);

        }

        @Override
        protected List<Story> doInBackground(final Case... cases) {
            caseToDelete = cases[0];
            int caseId = caseToDelete.getCid();
            mAsyncTaskDao.deleteByLocalCaseId(caseToDelete.getCid());
            return mAsyncTaskDao.getAllByLocalCid(caseId);
        }

        @Override
        protected void onPostExecute(List<Story> result) {
            for(Story st : result){
                deleteWithFiles(st,mAsyncTaskDao,mFileDao,applicationWeakReference.get()); //this removes both locally and in firebase
            }
            //remaining clean-up by case firebase id
            if(Modes.IS_ADMIN){
                deleteStoriesFilesOfCaseOnlyInFire(caseToDelete);
            }
        }
    }
    private static void deleteStoriesFilesOfCaseOnlyInFire(Case toDelete){
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        Task<QuerySnapshot> storiesToDeleteTask = mFirestore
                .collection(FirebaseConstants.STORIES_COLLECTION_PATH)
                .whereEqualTo(FirebaseConstants.CASE_FB_ID, toDelete.getFbId())
                .get();

        storiesToDeleteTask.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        Story st = doc.toObject(Story.class);
                        deleteStoryAndItsFilesInFire(st); //we know that this story is not available locally anyway
                    }catch(Exception e){
                        Log.d(TAG, "Experienced error on deleting a file: " + e.toString());
                    }

                }
            }
        });
    }


    private static void deleteStoryAndItsFilesInFire(Story toDelete) {
        if(toDelete.getFbId() != null) {
            FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
            mFirestore.collection(FirebaseConstants.STORIES_COLLECTION_PATH).document(toDelete.getFbId()).delete();
            Task<QuerySnapshot> filesToDelete = mFirestore
                                        .collection(FILES_COLLECTION_PATH)
                                        .whereEqualTo(FirebaseConstants.STORY_FB_ID, toDelete.getFbId())
                                        .get();

            filesToDelete.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        for (DocumentSnapshot doc : snapshot) {
                            mFirestore.collection(FILES_COLLECTION_PATH)
                                    .document(doc.getId()).delete();
                            try {
                                Attachedfile afinstance = doc.toObject(Attachedfile.class);
                                FirebaseStorage.getInstance()
                                        .getReference(Objects.requireNonNull(afinstance.getFireStorageFilePath()))
                                        .delete();
                            }catch(Exception e){
                                Log.d(TAG, "Experienced error on deleting a file: " +
                                        e.toString());
                            }
                        }
                    } else {
                        Log.w(TAG, "Failed to delete a file. error: " +
                                task.getException());
                    }
                }
            });
        }
    }

    public void populateStoriesFromFirestore(PullData pd, String pull_what){
        boolean isNetworkAvailable = NetworkStateChangeBroadcaster.getIsConnected();
        if(isNetworkAvailable) {
            Query getter_link;
            StoryRepo sr = this;
            getter_link = RepoUtils.getQueryLink(pd, mFirestore, FirebaseConstants.STORIES_COLLECTION_PATH, pull_what, mAuth.getUid());
            RepoUtils.getStoriesFromGetterLink(getter_link, pd, null, sr);
        }else{
            pd.afterPopulateMediaStep(false);
            Toast.makeText(application.getBaseContext(),
                    PROBLEM_INTERNET, Toast.LENGTH_SHORT).show();
        }
    }


    public void insertAllFromFire (List<Story> stories, PullData pd, boolean watch_fbid_conflict, List<String> ids_to_delete_later) {
        preprocessStoriesCid(mStoryDao, pd, watch_fbid_conflict, stories, ids_to_delete_later);
    }

    private void preprocessStoriesCid(StoryDAO dao, PullData pd, boolean watch_fbid_conflict, List<Story> stories, List<String> ids_to_delete_later){
        class ExecutorStory implements CallbackReceivedDataGeneral{
            List<Story> stories = new ArrayList<Story>();
            StoryDAO dao; PullData pd; boolean watch_fbid_conflict; List<String> ids_to_delete_later;
            CallbackReceivedDataGeneral callback_general = this;
            CaseRepo caseRepo;

            private ExecutorStory(StoryDAO dao, PullData pd, boolean watch_fbid_conflict, List<Story> stories, List<String> ids_to_delete_later){
                this.stories = stories;
                this.dao = dao;
                this.pd = pd;
                this.watch_fbid_conflict = watch_fbid_conflict;
                this.ids_to_delete_later = ids_to_delete_later;
                List<String> case_fbids = new ArrayList<String>();
                for(Story st: stories){
                    if(!case_fbids.contains(st.getCaseFbId())){
                        case_fbids.add(st.getCaseFbId());
                    }
                }
                caseRepo = new CaseRepo((Application)context);
                caseRepo.getCasesByQueryWithFilt(callback_general, RoomConstants.QUERY_BY_FBID, case_fbids);
                //calls callbackReceivedData
            }

            @Override
            public void callbackReceivedData(List<Case> cases_, List<Story> stories_, List<Attachedfile> afiles_) {
                //assign correct cids
                List<Story> stories_new =storiesWithCorrectCids(stories, cases_, caseRepo);
                new insertAllFromFireAsyncTask(dao, pd, watch_fbid_conflict, stories_new, ids_to_delete_later).execute();
            }
        }
        ExecutorStory exs = new ExecutorStory(dao, pd, watch_fbid_conflict, stories, ids_to_delete_later);
    }

    private static class insertAllFromFireAsyncTask extends AsyncTask<Void, Void, Void> implements CallbackReceivedDataGeneral{

        private WeakReference<StoryDAO> mAsyncTaskDao;
        private PullData pd;
        private boolean watch_fbid_conflict;
        private boolean ready_to_go_to_next = false;
        private List<Story> stories;
        private StoryDAO dao;
        private List<String> ids_to_delete_later;

        insertAllFromFireAsyncTask(StoryDAO dao, PullData pd, boolean watch_fbid_conflict, List<Story> stories, List<String> ids_to_delete_later) {
            this.dao = dao;
            mAsyncTaskDao = new WeakReference<>(dao);
            this.pd = pd;
            this.watch_fbid_conflict = watch_fbid_conflict;
            this.stories = stories;
            this.ids_to_delete_later = ids_to_delete_later;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if(watch_fbid_conflict){
                List<String> fbids = new ArrayList<String>();
                for(Story st: stories){
                    if(!fbids.contains(st.getFbId())) fbids.add(st.getFbId());
                }
                getAllStoriesByQuery(null,null,this, RoomConstants.QUERY_BY_FBID, fbids,dao);
                //calls callbackReceivedData
                return null;
            }else{
                mAsyncTaskDao.get().insertAll(stories);
                ready_to_go_to_next = true;
                return null;
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected void onPostExecute (Void result){
            FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
            if(ids_to_delete_later != null) RepoUtils.cleanUpSyncInFirestoreForUser(mFirestore,
                    ids_to_delete_later, mAuth.getUid(), FirebaseConstants.STORIES_COLLECTION_PATH);
            if(ready_to_go_to_next && pd!=null) pd.cleanUpAndPopulateFiles(); //OMG!
        }

        @Override
        public void callbackReceivedData(List<Case> cases_, List<Story> stories_, List<Attachedfile> afiles_) {
            stories = filterOutStories(stories_, stories);
            watch_fbid_conflict = false;
            new insertAllFromFireAsyncTask(dao, pd, watch_fbid_conflict, stories, ids_to_delete_later).execute(); //seems to work but looks super hacky...
        }
    }

    private List<Story> storiesWithCorrectCids(List<Story> stories, List<Case> cases_, CaseRepo caseRepo){
        List<Story> stories_new = new ArrayList<>();
        if(cases_ != null){
            List<String> missing_cases_fbid = new ArrayList<String>();
            List<String> keys = new ArrayList<String>();
            List<Integer> vals = new ArrayList<Integer>();
            List<String> duplicated_keys = new ArrayList<String>();
            List<Integer> duplicated_vals = new ArrayList<Integer>();
            List<Integer> correct_vals = new ArrayList<Integer>();

            for(int y=0;y<cases_.size(); y++){
                Case cs = cases_.get(y);
                String fbid = cs.getFbId();
                if(!keys.contains(fbid)){
                    keys.add(fbid);
                    vals.add(cs.getCid());
                }else{
                    duplicated_keys.add(fbid);
                    duplicated_vals.add(cs.getCid());
                    correct_vals.add(vals.get( keys.indexOf(fbid) ));
                    caseRepo.fixCasesStories(duplicated_keys, duplicated_vals, correct_vals);
                }
            }
            //once we pull the data we need to make sure that the local ids are aligned
            Map<String, Integer> fb_cid = Utilities.zipToMap(keys, vals);
            for(int i = 0; i<stories.size(); i++){
                Story st = stories.get(i);
                String case_fbid = st.getCaseFbId();
                if(case_fbid != null && fb_cid.get(case_fbid) != null &&
                        fb_cid.get(case_fbid)!=0){
                    int local_cid = fb_cid.get(case_fbid);
                    st.setSid(0);
                    st.setCid(local_cid);
                    stories_new.add(st);
                }else if(fb_cid.get(case_fbid) != null){
                    missing_cases_fbid.add(case_fbid);
                }
            }

            if(missing_cases_fbid.size() >0){
                caseRepo.syncMissingCasesByFbid(missing_cases_fbid);
            }
        }
        return stories_new;
    }

    private static List<Story> filterOutStories(List<Story> received_list, List<Story> list_being_checked){
        List<Story> to_be_returned = list_being_checked;
        if(received_list != null){
            List<Story> newstories = new ArrayList<Story>();
            List <String> fbids_already_indb = new ArrayList<String>();
            List <Integer> local_ids = new ArrayList<Integer>();
            for(Story st: received_list){
                fbids_already_indb.add(st.getFbId());
                local_ids.add(st.getSid());
            }
            for(Story st: to_be_returned){
                if(!fbids_already_indb.contains(st.getFbId())){
                    st.setSid(0);
                    newstories.add(st);
                }else{
                    int i = fbids_already_indb.indexOf(st.getFbId());
                    st.setSid(local_ids.get(i));
                    newstories.add(st);
                }
            }
            to_be_returned = newstories;
        }
        return to_be_returned;
    }

}

