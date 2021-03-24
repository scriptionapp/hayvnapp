package com.hayvn.hayvnapp.Repository;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.FirebaseConstants;
import com.hayvn.hayvnapp.Constant.RoomConstants;
import com.hayvn.hayvnapp.DataBase.AppDB;
import com.hayvn.hayvnapp.FirebaseHelper.FBSupportFunctions;
import com.hayvn.hayvnapp.Helper.NetworkStateChangeBroadcaster;
import com.hayvn.hayvnapp.Helper.PullData;
import com.hayvn.hayvnapp.Interface.AttachFileDAO;
import com.hayvn.hayvnapp.Interface.StoryDAO;
import com.hayvn.hayvnapp.Interfaces.CallbackGetFilesForStory;
import com.hayvn.hayvnapp.Interfaces.CallbackObjectUpdated;
import com.hayvn.hayvnapp.Interfaces.CallbackReceivedDataGeneral;
import com.hayvn.hayvnapp.Interfaces.CallbackRetrieveAndInsert;
import com.hayvn.hayvnapp.Model.Attachedfile;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.ObjectContainer;
import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.ProdDev.Modes;
import com.hayvn.hayvnapp.Utilities.LocalFileWriter;
import com.hayvn.hayvnapp.Utilities.Utilities;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.hayvn.hayvnapp.Constant.FirebaseConstants.FILES_COLLECTION_PATH;
import static com.hayvn.hayvnapp.Constant.MethodConstants.FAIL_FILE_PROGRESS;
import static com.hayvn.hayvnapp.Constant.MethodConstants.PROGRESSUPDATE_FILE_PROGRESS;
import static com.hayvn.hayvnapp.Constant.MethodConstants.SUCCESS_FILE_PROGRESS;
import static com.hayvn.hayvnapp.Constant.TextConstants.PROBLEM_INTERNET;


public class FileRepo {

    private static final String TAG = "FILE_REPO";
    private static AttachFileDAO mFileDao;
    private static FirebaseAuth mAuth;
    public static CallbackRetrieveAndInsert myCallback;
    public static CallbackGetFilesForStory myCallbackFilesForStory;
    //?
    public FirebaseFirestore mFirestore;
    private static LocalFileWriter ufw;
    private Context context;
    private Application application;
    private AppDB db;
    Disposable disposable = null;

    public FileRepo(Application application) {
        this.db = AppDB.getAppDatabase(application);
        this.mFileDao = db.attachFileDAO();
        this.mFirestore = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.context = application.getBaseContext();
        this.ufw = new LocalFileWriter(context);
        this.application = application;
    }

    public FileRepo(Application application, CallbackRetrieveAndInsert myCallback) {
        this.db = AppDB.getAppDatabase(application);
        this.mFileDao = db.attachFileDAO();
        this.mFirestore = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.context = application.getBaseContext();
        this.ufw = new LocalFileWriter(context);
        this.application = application;
        this.myCallback = myCallback;
    }

    public Attachedfile setUpdateCreate(Attachedfile afile_, boolean with_updated_at, boolean with_uid){
        Long now = new Date().getTime();
        if(afile_.getCreatedAt() == null || afile_.getCreatedAt() == 0){
            afile_.setCreatedAt(now);
        }
        if(with_updated_at){
            afile_.setUpdatedAt(now);
            if(with_uid && mAuth != null && mAuth.getUid() != null) afile_.setUserId(mAuth.getUid());
        }
        return afile_;
    }


    private static Attachedfile setUpdateFirestore(Attachedfile afile_){
        Long now = new Date().getTime();
        afile_.setUpdatedFirebaseAt(now);
        return afile_;
    }

    public void getFileFromFirebaseWithCallback(Attachedfile af, boolean use_compressed_data, Uri localUri, int position_in_list, Context mContext){
        String storage_path = FBSupportFunctions.getCompressedFirebaseStoragePath(af, use_compressed_data);
        StorageReference ref = FirebaseStorage.getInstance().getReference(storage_path);
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri downloadUri) {
                //implements callbackRetrieveAndInsert:
                retrieveAndInsert(downloadUri.toString(), localUri, position_in_list, af, mContext);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                myCallback.callbackRetrieveAndInsert(position_in_list,
                        0,
                        FAIL_FILE_PROGRESS,
                        localUri);
            }
        });
    }

    public void getFilesForStories(CallbackGetFilesForStory callback_received, List<Integer> sids){
        WeakReference<AttachFileDAO> mAsyncStoryDao = new WeakReference<>(mFileDao);
        disposable = mAsyncStoryDao.get().getBySids(sids)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Attachedfile>>() {
                    @Override
                    public void accept(List<Attachedfile> fls) throws Exception {
                        callback_received.callbackGetFilesForStory(fls);
                        completeDispose();
                    }
                });
    }

    private void completeDispose(){
        if(disposable!=null) disposable.dispose();
    }

    private void retrieveAndInsert (String downloadUri,
                                    Uri localUri,
                                    int indexI,
                                    Attachedfile af,
                                    Context context) {
        new RetrieveTask(localUri, indexI, af, context).execute(downloadUri);
    }


    static class  RetrieveTask extends AsyncTask<String, Void, Void> {
        String downloadUri;
        Uri localUri;
        int position_in_list;
        Attachedfile af;
        ContentResolver cr;
        WeakReference<Context> contextWeakReference;

        RetrieveTask(Uri localUri, int position_in_list, Attachedfile af, Context context) {
            this.localUri = localUri;
            this.position_in_list = position_in_list;
            this.af = af;
            this.cr = context.getContentResolver();
            contextWeakReference = new WeakReference<>(context);
            this.cr = contextWeakReference.get().getContentResolver();
        }
        @Override
        protected Void doInBackground(final String... downloadUri) {
            this.downloadUri = downloadUri[0];
            InputStream inp = null;
            OutputStream output = null;
            try{
                URL url = new URL(this.downloadUri);
                URLConnection urlCon = url.openConnection();
                HttpURLConnection httpCon = (HttpURLConnection) urlCon;
                httpCon.connect();

                output = cr.openOutputStream(localUri);
                int lengthOfFile = httpCon.getContentLength();
                inp = new BufferedInputStream(url.openStream(), 8192);// httpCon.getInputStream();

                byte[] buffer = new byte[1024];
                int bytesRead;
                long total = 0;
                while ((bytesRead = inp.read(buffer)) != -1) {
                    total += bytesRead;
                    assert output != null;
                    output.write(buffer, 0, bytesRead);
                    myCallback.callbackRetrieveAndInsert(position_in_list,
                            total/(float)lengthOfFile,
                            PROGRESSUPDATE_FILE_PROGRESS,
                            localUri);
                }

                af.setLocalFilePath(localUri.toString());
                af.setLocalStatus(Constant.FILE_IS_AVAIL_LOCALLY);

                myCallback.callbackRetrieveAndInsert(position_in_list,
                        1,
                        SUCCESS_FILE_PROGRESS,
                        localUri);
                insertUpdate(af, null, false, false);//these are the files downloaded from firestore, so no need to recompress;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                cr.delete(localUri, null, null);
                myCallback.callbackRetrieveAndInsert(position_in_list,
                        0,
                        FAIL_FILE_PROGRESS,
                        localUri);

            } catch (IOException e) {
                e.printStackTrace();
                myCallback.callbackRetrieveAndInsert(position_in_list,
                        0,
                        FAIL_FILE_PROGRESS,
                        localUri);
            } finally{
                try {
                    assert inp != null;
                    inp.close();
                    assert output != null;
                    output.flush();
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }



    public LiveData<List<Attachedfile>> getStoryFiles(int sid) {
        return mFileDao.findBySid(sid);
    }

    public insertUpdateAsyncTask insertWithProgressTask(CallbackObjectUpdated callback_updated,
                                                        boolean with_firebase, boolean compress_images) {
        return(new insertUpdateAsyncTask(mFileDao, callback_updated, with_firebase, compress_images));
    }
    public static void insertUpdate(Attachedfile af, CallbackObjectUpdated callback_updated,
                                    boolean with_firebase, boolean compress_images) {
        new insertUpdateAsyncTask(mFileDao, callback_updated, with_firebase, compress_images).execute(af);
    }


    private static final boolean COMPRESS_IMAGES = true;
    public static class insertUpdateAsyncTask extends AsyncTask<Attachedfile, Void, Void> {
        private WeakReference<AttachFileDAO> mAsyncTaskDao;
        boolean with_firebase;
        Attachedfile newAF;
        CallbackObjectUpdated callback_updated;
        boolean compress_images;

        insertUpdateAsyncTask(AttachFileDAO dao, CallbackObjectUpdated callback_updated, boolean with_firebase, boolean compress_images) {
            mAsyncTaskDao = new WeakReference<>(dao);
            this.with_firebase = with_firebase;
            this.callback_updated = callback_updated;
            this.compress_images = compress_images;
        }

        @Override
        protected Void doInBackground(final Attachedfile... params) {
            newAF = params[0];
            if(newAF.getFid() > 0){
                mAsyncTaskDao.get().updateAttachedFiles(newAF);
            }else{
                long newId = mAsyncTaskDao.get().insert(newAF);
                newAF.setFid((int) newId);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(callback_updated != null) callback_updated.onObjectUpdatedLocally(new ObjectContainer(newAF));
            if(with_firebase) syncFileToFirebase(newAF, callback_updated, compress_images);
        }
    }



    public void deleteFileCompletely(Attachedfile af) { new deleteByFidAsyncTaskStory(mFileDao,context).execute(af);  }
    private static class deleteByFidAsyncTaskStory extends AsyncTask<Attachedfile, Void, Void> {
        private WeakReference<AttachFileDAO> mAsyncTaskDao;
        private WeakReference<Context> contextWeakReference;

        deleteByFidAsyncTaskStory(AttachFileDAO dao,Context context) {
            mAsyncTaskDao = new WeakReference<>(dao);
            contextWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(final Attachedfile... params) {
            Attachedfile todeleteAF = params[0];
            deleteListOfFiles(true, true, contextWeakReference.get(),todeleteAF);
            return null;
        }
    }

    public void getAllFilesByStatus(PullData pd, String status) {
        new GetFilesByStatusAsyncTask(pd, status, mFileDao).execute();
    }
    private static class GetFilesByStatusAsyncTask extends AsyncTask<Void, Void, List<Attachedfile>>
    {
        PullData pd;
        String status;
        AttachFileDAO mFileDao;

        GetFilesByStatusAsyncTask(PullData pd, String status, AttachFileDAO mFileDao) {
            this.pd = pd;
            this.status = status;
            this.mFileDao = mFileDao;
        }

        @Override
        protected List<Attachedfile> doInBackground(Void... url) {
            return mFileDao.getAllByLocalStatus(status);
        }

        @Override
        protected void onPostExecute(List<Attachedfile> result) {
            try {
                pd.OnFileListReceived(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getAllFilesForStory(Story story_) {
        new GetFilesForStory(story_).execute();
    }
    private static class GetFilesForStory extends AsyncTask<Void, Void, List<Attachedfile>>
    {
        Story story_;

        GetFilesForStory(Story story_) {
            this.story_ = story_;
        }

        @Override
        protected List<Attachedfile> doInBackground(Void... url) {
            return mFileDao.getAllForStory(story_.getSid());
        }

        @Override
        protected void onPostExecute(List<Attachedfile> result) {
            myCallbackFilesForStory.callbackGetFilesForStory(result);
        }
    }

    private static void putFileInStorage(Attachedfile af, boolean compress_images){
        String userId = mAuth.getUid();
        UploadTask uploadTask;
        Uri fileUri = Uri.parse(af.getLocalFilePath());
        assert userId != null;
        StorageReference cloudFileRef = FirebaseStorage.
                getInstance().
                getReference().
                child(userId).
                child(Objects.requireNonNull(af.getFileName()));

        uploadTask = cloudFileRef.putFile(fileUri);
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
        }).addOnCanceledListener(() -> {

        }).addOnSuccessListener(taskSnapshot -> {
            af.setFireStorageFilePath(Objects.requireNonNull(taskSnapshot.getMetadata()).getPath());
            insertUpdate(af, null, true, compress_images); //this method will trigger syncFileToFirebase again
        });
    }


    private static void compress_image_locally(Attachedfile af){
        LocalFileWriter ufw = new LocalFileWriter(null); //context null 20210324
        ufw.compressImageAtUri(af.getLocalFilePath());
    }

    private static void addFileToFirestore(Attachedfile af, CallbackObjectUpdated callback_updated, boolean compress_images){
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        DocumentReference new_doc = mFirestore.collection(FirebaseConstants.FILES_COLLECTION_PATH).document();
        String new_id = new_doc.getId();
        af.setFbId(new_id);
        mFirestore.collection(FILES_COLLECTION_PATH).document(af.getFbId()).set(af, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        insertUpdate(af, null, false, compress_images); //callback_updated should be passed as null
                        if(compress_images && af.getType().equals(Constant.IMG_FILE_TYPE)){
                            compress_image_locally(af); //TODO
                        }
                        if (callback_updated != null)
                            callback_updated.onObjectGotFireId(new ObjectContainer(af), true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "uploaded file to firestore FAIL");
                    }
                });
    }


    private static void updFileInFirestore(Attachedfile af, CallbackObjectUpdated callback_updated, boolean compress_images){
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection(FILES_COLLECTION_PATH).document(af.getFbId()).set(af)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        insertUpdate(af, null, false, compress_images); //callback_updated should be passed as null
                        if (callback_updated != null)
                            callback_updated.onObjectGotFireId(new ObjectContainer(af), true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "uploaded file to firestore FAIL");
                    }
                });
    }

    private static void syncFileToFirebase(Attachedfile af, CallbackObjectUpdated callback_updated, boolean compress_images) {
        // We only upload to cloud if there is no firestore id for the AF yet
        boolean isNetworkAvailable = NetworkStateChangeBroadcaster.getIsConnected();
        if(Constant.IGNORE_NETWORK_AVAIL || isNetworkAvailable) {
            if (af != null) {
                if (af.getFireStorageFilePath() == null ) {
                    putFileInStorage(af, compress_images);
                } else if (af.getFbId() == null) { //this part only runs if you are saving a new file
                    af = setUpdateFirestore(af);
                    addFileToFirestore(af, callback_updated, compress_images);
                } else { //this is just an update, no need to sync the media, just the info about the file
                    af = setUpdateFirestore(af);
                    updFileInFirestore(af, callback_updated, compress_images);
                }
            }
        }
    }

    public void deleteListOfFiles(Attachedfile... afs) {
        deleteMedia(context,afs);
        new deleteListAsyncTask(mFileDao, mFirestore, true).execute(afs);
    }
    public static void deleteListOfFiles(boolean with_media, boolean with_firebase, Context context,Attachedfile... afs) {
        if(with_media) deleteMedia(context,afs);
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        new deleteListAsyncTask(mFileDao, mFirestore, with_firebase).execute(afs);
    }
    private static void deleteMedia(Context context,Attachedfile... afs){
        LocalFileWriter lfw = new LocalFileWriter(context);
        for(Attachedfile af: afs){
            String path = af.getLocalFilePath();
            lfw.deleteByPath(path);
        }
    }
    private static class deleteListAsyncTask extends AsyncTask<Attachedfile, Void, Void> {

        private AttachFileDAO mAsyncTaskDao;
        private FirebaseFirestore mFirestore;
        private boolean with_firebase;

        deleteListAsyncTask(AttachFileDAO dao, FirebaseFirestore mFirestore, boolean with_firebase) {
            this.mFirestore = mFirestore;
            this.with_firebase = with_firebase;
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Attachedfile... params) {
            mAsyncTaskDao.delete(params);
            if(Modes.IS_ADMIN) {
                if (with_firebase) deleteFilesFromFirestore(mFirestore, params);
            }
            return null;
        }
    }

    public static void deleteFilesFromFirestore(FirebaseFirestore mFirestore, Attachedfile... allFiles){
        for(Attachedfile af : allFiles){
            if(af!=null){
                if(af.getFbId()!=null) mFirestore.collection(FILES_COLLECTION_PATH).document(af.getFbId()).delete();
                if(af.getFireStorageFilePath()!=null) FirebaseStorage.getInstance().getReference(Objects.requireNonNull(af.getFireStorageFilePath())).delete();
            }
        }
    }


    public void populateFilesFromFirestore(PullData pd, String pull_what){
        boolean isNetworkAvailable = NetworkStateChangeBroadcaster.getIsConnected();
        if(isNetworkAvailable) {
            Query getter_link;
            FileRepo fr = this;
            getter_link = RepoUtils.getQueryLink(pd, mFirestore, FILES_COLLECTION_PATH, pull_what, mAuth.getUid()); //, null);
            assert getter_link != null;
            RepoUtils.getFilesFromGetterLink(getter_link, pd, null, fr);
        }else{
            pd.afterPopulateMediaStep(false);
            Toast.makeText(application, PROBLEM_INTERNET, Toast.LENGTH_SHORT).show();
        }
    }

    public void insertAllFromFire (List<Attachedfile> files, PullData pd, boolean watch_fbid_conflict, List<String> ids_to_delete_later) {
        preprocessFilesSidCidRX(files, pd, watch_fbid_conflict, ids_to_delete_later);
    }

    private void preprocessFilesSidCidRX(List<Attachedfile> files, PullData pd, boolean watch_fbid_conflict, List<String> ids_to_delete_later){
        StoryDAO mStoryDao = db.storyDAO();
        WeakReference<StoryDAO> mAsyncTaskDao = new WeakReference<>(mStoryDao);
        List<String> parent_story_fbids = getListOfStoryFbIds(files);

        disposable = mAsyncTaskDao.get().getByFbIdsMaybeRX(parent_story_fbids)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Story>>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull List<Story> stories) throws Exception {
                        List<Attachedfile> files_new = assignCorrectSidCid(stories, files);
                        new insertAllFromFireAsyncTask(mFileDao, pd, watch_fbid_conflict, files_new, ids_to_delete_later).execute();
                        completeDispose();
                    }
                });
    }

    private static class insertAllFromFireAsyncTask extends AsyncTask<Void, Void, Void> implements CallbackReceivedDataGeneral{
        private WeakReference<AttachFileDAO> mAsyncTaskDao;
        private PullData pd;
        private boolean watch_fbid_conflict;
        private boolean ready_to_go_to_next = false;
        private List<Attachedfile> files;
        private AttachFileDAO dao;
        private List<String> ids_to_delete_later;

        insertAllFromFireAsyncTask(AttachFileDAO dao, PullData pd, boolean watch_fbid_conflict, List<Attachedfile> files, List<String> ids_to_delete_later) {
            this.dao = dao;
            mAsyncTaskDao = new WeakReference<>(dao);
            this.pd = pd;
            this.watch_fbid_conflict = watch_fbid_conflict;
            this.files = files;
            this.ids_to_delete_later = ids_to_delete_later;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if(watch_fbid_conflict){
                List<String> fbids = new ArrayList<String>();
                for(Attachedfile af: files){fbids.add(af.getFbId());}
                getFilesByQuery(this, RoomConstants.QUERY_BY_FBID, fbids);
                //calls callbackReceivedData
                return null;
            }else{
                mAsyncTaskDao.get().insertAll(files);
                ready_to_go_to_next = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute (Void result){
            FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
            if(ids_to_delete_later != null) RepoUtils.cleanUpSyncInFirestoreForUser(mFirestore,
                    ids_to_delete_later, mAuth.getUid(), FILES_COLLECTION_PATH);
            if(ready_to_go_to_next && pd!=null) pd.populateUsers();
        }

        @Override
        public void callbackReceivedData(List<Case> cases_, List<Story> stories_, List<Attachedfile> afiles_) {
            if(afiles_ != null){
                List<Attachedfile> newfiles = new ArrayList<Attachedfile>();
                List <String> fbids_already_indb = new ArrayList<String>();
                List <Integer> local_ids = new ArrayList<Integer>();
                List <Integer> local_urls = new ArrayList<Integer>();
                for(Attachedfile af: afiles_){
                    fbids_already_indb.add(af.getFbId());
                    local_ids.add(af.getFid());
                }

                for(Attachedfile af: files){
                    if(!fbids_already_indb.contains(af.getFbId())){
                        af.setFid(0);
                        newfiles.add(af);
                    }//files cannot be edited, so we really don'y need else
                }
                files = newfiles;
            }
            watch_fbid_conflict = false;
            new insertAllFromFireAsyncTask(dao, pd, watch_fbid_conflict, files, ids_to_delete_later).execute();
        }

    }



    public static void getFilesByQuery(CallbackReceivedDataGeneral callback_general, String query, List<String> fbids) {
        new GetFilesByQueryAsyncTask(callback_general, query, fbids).execute();
    }
    private static class GetFilesByQueryAsyncTask extends AsyncTask<Void, Void, List<Attachedfile>>
    {
        CallbackReceivedDataGeneral callback_general;
        private WeakReference<AttachFileDAO> mAsyncTaskDao;
        private String query;
        private List<String> fbids;

        GetFilesByQueryAsyncTask(CallbackReceivedDataGeneral callback_general, String query, List<String> fbids) {
            mAsyncTaskDao = new WeakReference<>(mFileDao);
            this.callback_general = callback_general;
            this.query = query;
            this.fbids = fbids;
        }

        @Override
        protected List<Attachedfile> doInBackground(Void... param) {
            if(query.equals(RoomConstants.QUERY_UNSYNCED)){
                return mAsyncTaskDao.get().getUnsynced();
            }else if(query.equals(RoomConstants.QUERY_BY_FBID)){
                return mAsyncTaskDao.get().getByFbIds(fbids);
            }else{
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Attachedfile> result) {
            callback_general.callbackReceivedData(null, null, result);
        }
    }


    private List<String> getListOfStoryFbIds(List<Attachedfile> files){
        List<String> parent_story_fbids = new ArrayList<String>();
        for(Attachedfile file: files){
            String st = file.getStoryFbId();
            if(st != null) parent_story_fbids.add(st);
        }
        return parent_story_fbids;
    }


    private List<Attachedfile> assignCorrectSidCid(List<Story> stories_, List<Attachedfile> original_files){

        List<String> keys = new ArrayList<String>();
        List<Integer> vals_sid = new ArrayList<Integer>();
        List<Integer> vals_cid = new ArrayList<Integer>();
        for(int y=0;y<stories_.size(); y++){
            Story st = stories_.get(y);
            if(st.getFbId()!=null && !keys.contains(st.getFbId()) ){
                keys.add(st.getFbId());
                vals_sid.add(st.getSid());
                vals_cid.add(st.getCid());
            }

        }
        //once we pull the data we need to make sure that the local ids are aligned
        Map<String, Integer> fb_sid = Utilities.zipToMap(keys, vals_sid);
        Map<String, Integer> fb_cid = Utilities.zipToMap(keys, vals_cid);
        List<Attachedfile> files_new = new ArrayList<Attachedfile>();

        for(int i = 0; i<original_files.size(); i++){
            Attachedfile af = original_files.get(i);
            String story_fbid = af.getStoryFbId();
            if(story_fbid != null &&
                    fb_sid.get(story_fbid) != null &&
                    fb_cid.get(story_fbid)!=null){
                int local_sid = fb_sid.get(story_fbid);
                int local_cid = fb_cid.get(story_fbid);
                af.setSid(local_sid);
                af.setCid(local_cid);
                files_new.add(af);
            }
        }
        return files_new;
    }
}


