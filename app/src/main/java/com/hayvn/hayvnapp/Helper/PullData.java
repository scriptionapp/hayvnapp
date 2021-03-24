package com.hayvn.hayvnapp.Helper;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.hayvn.hayvnapp.Activities.MainAppActivity;
import com.hayvn.hayvnapp.Activities.PinActivity;
import com.hayvn.hayvnapp.Activities.StoryActivity;
import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.IntentConstants;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.DataBase.AppDB;
import com.hayvn.hayvnapp.FirebaseHelper.FBSupportFunctions;
import com.hayvn.hayvnapp.Interfaces.CallbackLocalStories;
import com.hayvn.hayvnapp.Interfaces.CallbackRetrieveAndInsert;
import com.hayvn.hayvnapp.Interfaces.CallbackUriCreated;
import com.hayvn.hayvnapp.Model.Attachedfile;
import com.hayvn.hayvnapp.Model.FileNameProgress;
import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.Model.StoryFileCount;
import com.hayvn.hayvnapp.Repository.CaseRepo;
import com.hayvn.hayvnapp.Repository.FileRepo;
import com.hayvn.hayvnapp.Repository.RepoUtils;
import com.hayvn.hayvnapp.Repository.StoryRepo;
import com.hayvn.hayvnapp.Repository.UserRepo;
import com.hayvn.hayvnapp.Utilities.LocalFileWriter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.hayvn.hayvnapp.Constant.Constant.FILE_NOT_AVAIL_LOCALLY;

public class PullData implements CallbackUriCreated, CallbackRetrieveAndInsert {

    public Context mContext;
    Application app;
    public Activity act;
    ContentResolver cr;
    FileRepo fr;
    String calledFrom;
    boolean populateMedia;
    public static final int WRITE_EXTERNAL_STORAGE = 300;
    final int[] downloadedFiles = {0};
    final int[] failedFiles = {0};
    int totalFiles = 0;
    ArrayList <FileNameProgress> fnps = new ArrayList<FileNameProgress>();
    List<Attachedfile> files_ = new ArrayList<Attachedfile>();
    public String currentStatus = "";
    private LocalFileWriter ufw;
    private String sync_start;
    private boolean use_compressed_data = true;
    private boolean watch_for_fbid_conflicts = true;
    private boolean working = false;
    private String pull_what = "others"; //"own", "others", "all"
    public final static String TAG = "PULL_DATA";

    public PullData(){
        //for clearing database
    }

    public PullData(Context mContext,
                    Application app, Activity act, ContentResolver cr, String calledFrom,
                    boolean populateMedia, String pull_what){
        this.mContext = mContext;
        this.app = app;
        this.calledFrom = calledFrom;
        this.cr = cr;
        this.populateMedia = populateMedia;
        this.act = act;
        this.fr = new FileRepo(app, this);
        this.ufw = new LocalFileWriter(mContext);
        this.pull_what = pull_what;
        this.sync_start = String.valueOf(new Date().getTime());

    }

    public Context getContext(){
        return mContext;
    }

    public boolean getWatchForFBIDConflicts(){
        return watch_for_fbid_conflicts;
    }

    public void sendIntent(String val) {
        Intent intent = new Intent();
        intent.setAction(IntentConstants.SYNC_TO_FIRESTORE_ACTION);
        intent.putExtra(IntentConstants.SYNC_BROADCAST_MESSAGE, val);
        app.sendBroadcast(intent);
    }
    private void sendIntent(String val, ArrayList<FileNameProgress> fnps) {
        Intent intent = new Intent();
        intent.setAction(IntentConstants.SYNC_TO_FIRESTORE_ACTION);
        intent.putExtra(IntentConstants.SYNC_BROADCAST_MESSAGE, val);
        intent.putParcelableArrayListExtra(IntentConstants.LIST_FILENAMEPROGRESS, fnps);
        app.sendBroadcast(intent);
    }
    private void sendIntent(String val, int ii, FileNameProgress fnp) {
        Intent intent = new Intent();
        intent.setAction(IntentConstants.SYNC_TO_FIRESTORE_ACTION);
        intent.putExtra(IntentConstants.SYNC_BROADCAST_MESSAGE, val);
        intent.putExtra(IntentConstants.INDIVIDUAL_FILENAMEPROGRESS, fnp);
        intent.putExtra(IntentConstants.LIST_FILENAMEPROGRESS_INDEX, ii);
        app.sendBroadcast(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void populateDB(){
        RepoUtils.getCurrentNumberOfShards(mContext);
        RepoUtils.getCurrentPatientPrefix(mContext);
        RepoUtils.getCurrentListOfCountries(mContext);
        RepoUtils.getCurrentListOfEntryTitles(mContext);
        //in case it was previously interrupted
        String latest_status = SharedPrefHelper.readString(mContext, Constant.FIREBASESYNCSTATUS);
        if (latest_status != null && latest_status.equals("doneStories")) {
            cleanUpAndPopulateFiles();
        } else {
            populateCases(); //all other methods will be called automatically one after the other
        }

    }

    public void syncIsWorking(boolean working){
        this.working = working;
    }

    private void populateCases(){
        sendIntent("Pulling data 1/4");
        CaseRepo cr = new CaseRepo(app);
        cr.populateCasesFromFirestore(this, pull_what);
        //cleanUpAndPopulateStories is called
    }

    public void cleanUpAndPopulateStories(){
        sendIntent("Pulling data 2/4");
        //write only here, because
        if(working) SharedPrefHelper.writeString(mContext.getApplicationContext(), SharedprefConstants.TIME_LAST_SYNC_START_CASES, sync_start);
        SharedPrefHelper.writeString(mContext.getApplicationContext(), Constant.FIREBASESYNCSTATUS, "doneCases");
        StoryRepo sr = new StoryRepo(app);
        //SharedPrefHelper.writeString(mContext.getApplicationContext(), SharedprefConstants.TIME_LAST_SYNC_START_STORIES, String.valueOf(new Date().getTime()));
        sr.populateStoriesFromFirestore(this, pull_what);
        CaseRepo cr = new CaseRepo(app);
        cr.findFavouriteCases();
        //cleanUpAndPopulateFiles is called
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void cleanUpAndPopulateFiles(){
        currentStatus = "firestore";
        sendIntent("Pulling data 3/4");
        if(working) SharedPrefHelper.writeString(mContext.getApplicationContext(), SharedprefConstants.TIME_LAST_SYNC_START_STORIES, sync_start);
        SharedPrefHelper.writeString(mContext.getApplicationContext(), Constant.FIREBASESYNCSTATUS, "doneStories");

        if (mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Need to request permissions");
            ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE);
        }else {
            FileRepo fr = new FileRepo(app);
            fr.populateFilesFromFirestore(this, pull_what);
        }
    }

    public void populateUsers(){
        sendIntent("Pulling data 4/4");
        if(working) SharedPrefHelper.writeString(mContext.getApplicationContext(), SharedprefConstants.TIME_LAST_SYNC_START_FILES, sync_start);
        SharedPrefHelper.writeString(mContext.getApplicationContext(), Constant.FIREBASESYNCSTATUS, "doneFiles");
        UserRepo ur = new UserRepo(app);
        ur.populateUserFromFirestore(this);
        //calls prePopulateMedia
    }

    // the execution of the below should not depend on pull_what
    public void prePopulateMedia() throws ExecutionException, InterruptedException {
        currentStatus = "storage";
        if(populateMedia){
            FileRepo fr = new FileRepo(app);
            fr.getAllFilesByStatus(this, FILE_NOT_AVAIL_LOCALLY);

        }else{
            afterPopulateMediaStep(false);
        }
    }

    public void OnFileListReceived(List<Attachedfile> files_) throws IOException {
        totalFiles = files_.size();
        this.files_ = files_;

        if(files_.size() > 0){
                pullFilesFromFB();
        }else{
            afterPopulateMediaStep(true);
        }
    }


    private void pullFilesFromFB(){
        int i = 0;
        fnps = new ArrayList<FileNameProgress>();
        for(i = 0; i< files_.size(); i++) {
            Attachedfile af_ = files_.get(i);
            FileNameProgress fnp_  = new FileNameProgress(af_.getFileName(), "0");
            fnps.add(fnp_);
        }
        sendIntent(IntentConstants.PULL_LAUNCH_ADAPTER, fnps);
        if(totalFiles == 0){
            check_if_can_close_dialog(totalFiles, downloadedFiles[0], failedFiles[0]);
        }
        for(i = 0; i< files_.size(); i++){
            final Attachedfile af  = files_.get(i);
            if(af.getFileName() != null && !af.getFileName().equals("") &&
                    af.getFireStorageFilePath() != null && !af.getFireStorageFilePath().equals("")&&
                    af.getFbId() != null && !af.getFbId().equals("")) {
                Uri uri = ufw.createUriWithoutCallback(af);
                downloadFromUri(uri, af, i);
            }else{
                failedFiles[0]++;
                check_if_can_close_dialog(totalFiles, downloadedFiles[0], failedFiles[0]);
            }
        }
    }

    @Override
    public void callbackUriCreated(Uri uri, Attachedfile af, int finalI) {
        downloadFromUri(uri, af, finalI);
    }

    private void downloadFromUri(Uri uri, Attachedfile af, int position_in_list){
        if(     uri != null &&
                !uri.getPath().equals("") &&
                af.getFireStorageFilePath() != null &&
                !af.getFireStorageFilePath().equals("")){
            fr.getFileFromFirebaseWithCallback(af, use_compressed_data, uri, position_in_list, mContext);
        }else{
            failedFiles[0]++;
            check_if_can_close_dialog(totalFiles, downloadedFiles[0], failedFiles[0]);
        }
    }

    @Override
    public void callbackRetrieveAndInsert(int finalI, float shareBytesReceived, String typeUpdate, Uri localUri) {
        callbackRetrieveAndInsert_(finalI, shareBytesReceived, typeUpdate, localUri);
    }

    private void callbackRetrieveAndInsert_(int ii,
                                          float bytesReceived,
                                          String typeUpdate,
                                          Uri localUri){
        FileNameProgress fnp = fnps.get(ii);
        if(typeUpdate.equals("progressupdate")){
            fnp.setProgress(String.valueOf(Math.round(bytesReceived*100)));
            fnps.set(ii, fnp);
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendIntent(IntentConstants.PULL_UPDATE_ADAPTER, ii, fnp);
                }
            });
        }else if(typeUpdate.equals("downloadsuccess")){
            fnp.setProgress(String.valueOf(100));
            fnps.set(ii, fnp);
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendIntent(IntentConstants.PULL_UPDATE_ADAPTER, ii, fnp);
                }
            });

            ufw.updateValuesAfterDownload(localUri);
            downloadedFiles[0]++;
            check_if_can_close_dialog(totalFiles, downloadedFiles[0], failedFiles[0]);
        }else if(typeUpdate.equals("fail")){
            failedFiles[0]++;
            cr.delete(localUri, null, null);
            fnp.setProgress("FAILED");
            check_if_can_close_dialog(totalFiles, downloadedFiles[0], failedFiles[0]);
        }
    }

    private void check_if_can_close_dialog(int tot, int success, int fail){
        if((success + fail) == tot){
            if(fail == 0){
                //
            }
            afterPopulateMediaStep(true);
        }
    }


    public void afterPopulateMediaStep(boolean wasItPopulated){
        SharedPrefHelper.writeString(mContext.getApplicationContext(), Constant.FIREBASESYNCSTATUS, null);
        sendIntent(IntentConstants.PULL_FROM_FIRESTORE_FINISHED);
    }




    ///////////ERASE DATA
    public void eraseDB(Context context, String typeActivity){
        Log.d(TAG,"delete all invoked");
        SharedPrefHelper.writeString(context.getApplicationContext(), Constant.WELCOME, null);
        SharedPrefHelper.writeString(context.getApplicationContext(), Constant.PIN, null);
        SharedPrefHelper.writeString(context.getApplicationContext(), Constant.FIREBASESYNCSTATUS, null);
        SharedPrefHelper.writeString(context.getApplicationContext(), Constant.PIN_TEMP, null);
        SharedPrefHelper.writeString(context.getApplicationContext(), SharedprefConstants.TIME_LAST_SYNC, null);
        SharedPrefHelper.writeString(context.getApplicationContext(), SharedprefConstants.FOR_WELCOME_DIALOG, null);
        SharedPrefHelper.writeString(context.getApplicationContext(), SharedprefConstants.SYNCED_ALL_ALREADY, null);
        SharedPrefHelper.writeString(context.getApplicationContext(), SharedprefConstants.KEEP_PAST_PREF, null);

        AppDB db = AppDB.getAppDatabase(context);
        Completable one = Completable.fromAction(() ->  db.clearAllTables());
        Completable.concatArray(one).observeOn(Schedulers.single()) // OFF UI THREAD
                .doOnSubscribe(__ -> {
                    Log.w(TAG, "Begin transaction. " + Thread.currentThread().toString());
                })
                .doOnComplete(() -> {
                    Log.w(TAG, "Set transaction successful."  + Thread.currentThread().toString());
                })
                .doFinally(() -> {
                    Log.w(TAG, "End transaction."  + Thread.currentThread().toString());
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread()) // ON UI THREAD
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.w(TAG, "onSubscribe."  + Thread.currentThread().toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.w(TAG, "onComplete."  + Thread.currentThread().toString());
                        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                            ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE))
                                    .clearApplicationUserData();// note: it has a return value!
                        }
                        if(typeActivity.equals("MainApp")){
                            ((MainAppActivity)context).onDBErased();
                        }else if(typeActivity.equals("StoryAct")){
                            ((StoryActivity)context).onDBErased();
                        }else if(typeActivity.equals("PinAct")){
                            ((PinActivity)context).onDBErased();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError." + Thread.currentThread().toString());
                    }
                });
    }



}
