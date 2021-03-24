package com.hayvn.hayvnapp.Repository;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hayvn.hayvnapp.Constant.FirebaseConstants;
import com.hayvn.hayvnapp.Constant.MethodConstants;
import com.hayvn.hayvnapp.Constant.RoomConstants;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.Helper.NetworkStateChangeBroadcaster;
import com.hayvn.hayvnapp.Helper.PullData;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Interfaces.CallbackReceivedDataGeneral;
import com.hayvn.hayvnapp.Model.Attachedfile;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.Story;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.hayvn.hayvnapp.Constant.Constant.FILE_IS_AVAIL_LOCALLY;
import static com.hayvn.hayvnapp.Constant.Constant.FILE_NOT_AVAIL_LOCALLY;
import static com.hayvn.hayvnapp.Constant.FirebaseConstants.USER_ID;

public class RepoUtils {
    private static final String TAG = "REPO_UTILS";

    public static void getCurrentPatientPrefix(Context mContext){
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection(FirebaseConstants.SHARED_COLLECTION_PATH)
                .document(FirebaseConstants.PATIENT_PREFIX).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String current_prefix = "";
                            try{
                                current_prefix = task.getResult().get(FirebaseConstants.CURRENT_PREFIX).toString();
                            }catch(Exception e){
                                current_prefix = SharedprefConstants.DEFAULT_PATIENT_PREFIX;
                            }
                            SharedPrefHelper.writeString(mContext, SharedprefConstants.PATIENT_PREFIX, current_prefix);
                        } else {
                            SharedPrefHelper.writeString(mContext, SharedprefConstants.PATIENT_PREFIX, SharedprefConstants.DEFAULT_PATIENT_PREFIX);
                        }
                    }
        }).addOnFailureListener(
                new OnFailureListener(){
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        SharedPrefHelper.writeString(mContext, SharedprefConstants.PATIENT_PREFIX, SharedprefConstants.DEFAULT_PATIENT_PREFIX);
                    }
                }
        );;
    }

    public static void  getCurrentListOfCountries(Context mContext){
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection(FirebaseConstants.SHARED_COLLECTION_PATH)
                .document(FirebaseConstants.COUNTRIES).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Long local_time = 0L;
                            Long time_ = 0L;

                            Object task2 = task.getResult().get(FirebaseConstants.COUNTRIES_ENTRIES_LAST_UPD);
                            if(task2 != null){
                                String fire_time = task2.toString();
                                time_ = Long.parseLong(fire_time);
                            }
                            String local_upd_time = SharedPrefHelper.readString(mContext, SharedprefConstants.COUNTRY_LIST_UPD_TIME);
                            if(local_upd_time != null && !local_upd_time.equals("")){
                                local_time = Long.parseLong(local_upd_time);
                            }
                            if(time_ > local_time){
                                Object task1 = task.getResult().get(FirebaseConstants.COUNTRIES_ENTRIES_ALL);
                                if(task1 != null){
                                    String countries = task1.toString();
                                    ArrayList<String> country_list = new ArrayList<String>( Arrays.asList(countries.split(";")) );
                                    SharedPrefHelper.writeStringList(mContext.getApplicationContext(), SharedprefConstants.COUNTRY_LIST_FULL, country_list);
                                    SharedPrefHelper.writeString(mContext, SharedprefConstants.COUNTRY_LIST_UPD_TIME, String.valueOf(new Date().getTime()) );
                                }
                            }
                        } else {
                            Log.d(TAG, "Error in getting patient prefix");
                        }
                    }
                });
    }

    public static void getCurrentNumberOfShards(Context mContext){
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection(FirebaseConstants.SHARED_COLLECTION_PATH)
                .document(FirebaseConstants.NUMBER_SHARDS).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            int current_num_shards = Integer.parseInt(task.getResult().get(FirebaseConstants.CURRENT_PREFIX).toString());
                            SharedPrefHelper.writeInteger(mContext.getApplicationContext(), SharedprefConstants.NUMBER_OF_SHARDS, current_num_shards);
                        } else {
                            Log.d(TAG, "Error in getting patient prefix");
                        }
                    }
                });
    }

    public static void  getCurrentListOfEntryTitles(Context mContext){
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection(FirebaseConstants.SHARED_COLLECTION_PATH)
                .document(FirebaseConstants.ENTRY_TITLES).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Long local_time = 0L;
                            Long time_ = 0L;
                            Object task2 = task.getResult().get(FirebaseConstants.COUNTRIES_ENTRIES_LAST_UPD);
                            String local_upd_time = SharedPrefHelper.readString(mContext, SharedprefConstants.ENTRYTITLE_LIST_UPD_TIME);
                            if(task2 != null){
                                String fire_time = task2.toString();
                                time_ = Long.parseLong(fire_time);
                            }
                            if(local_upd_time != null && !local_upd_time.equals("")){
                                local_time = Long.parseLong(local_upd_time);
                            }
                            if(time_ > local_time){
                                Object task1 = task.getResult().get(FirebaseConstants.COUNTRIES_ENTRIES_ALL);
                                if(task1 != null){
                                    String titles = task1.toString();
                                    ArrayList<String> title_list = new ArrayList<String>( Arrays.asList(titles.split(";")) );
                                    SharedPrefHelper.writeStringList(mContext.getApplicationContext(), SharedprefConstants.ENTRYTITLE_LIST_FULL, title_list);
                                    SharedPrefHelper.writeString(mContext, SharedprefConstants.ENTRYTITLE_LIST_UPD_TIME, String.valueOf(new Date().getTime()) );
                                }
                            }
                        } else {
                            Log.d(TAG, "Error in getting patient prefix");
                        }
                    }
                });
    }


    private static long returnFbUpdComparison(PullData pd, String parent_collection){
        Context context = pd.getContext();
        String time_str = null;

        if(parent_collection.equals(FirebaseConstants.CASES_COLLECTION_PATH)){
            time_str = SharedPrefHelper.readString(context, SharedprefConstants.TIME_LAST_SYNC_START_CASES);
        }else if(parent_collection.equals(FirebaseConstants.STORIES_COLLECTION_PATH)){
            time_str = SharedPrefHelper.readString(context, SharedprefConstants.TIME_LAST_SYNC_START_STORIES);
        }else if(parent_collection.equals(FirebaseConstants.FILES_COLLECTION_PATH)){
            time_str = SharedPrefHelper.readString(context, SharedprefConstants.TIME_LAST_SYNC_START_FILES);
        }
        if(time_str == null) time_str = "0";
        return Long.parseLong(time_str);
    }


    public static Query getQueryLink(PullData pd, FirebaseFirestore mFirestore, String parent_collection, String pull_what, String uid){
        if(parent_collection.equals(FirebaseConstants.CASES_COLLECTION_PATH) && pull_what.equals(MethodConstants.PULL_WHAT_ALL_RECENT))
            pull_what = MethodConstants.PULL_WHAT_ALL;

        if(pull_what.equals(MethodConstants.PULL_WHAT_ALL)){
            return mFirestore.collection(parent_collection);
        }else if(pull_what.equals(MethodConstants.PULL_WHAT_OWN)){
            return mFirestore.collection(parent_collection)
                    .whereEqualTo(USER_ID, uid);
        }else if(pull_what.equals(MethodConstants.PULL_WHAT_ALL_RECENT)){
            long comparator2 = cutOffRecency(pd.getContext());
            return mFirestore.collection(parent_collection)
                    .whereGreaterThanOrEqualTo(FirebaseConstants.UPDATED_FIREBASE_AT, comparator2);
        }else if(pull_what.equals(MethodConstants.PULL_WHAT_OTHERS)){
            long comparator = returnFbUpdComparison(pd, parent_collection) - 24*60*60*1000;
            return mFirestore.collection(parent_collection)
                    .whereGreaterThanOrEqualTo(FirebaseConstants.UPDATED_FIREBASE_AT, comparator);
        }else{
            return null;
        }
    }

    public static long cutOffRecency(Context context){
        String keep_past_x_pref = SharedPrefHelper.readString(context, SharedprefConstants.KEEP_PAST_PREF);
        if(keep_past_x_pref==null || keep_past_x_pref.equals("")){
            keep_past_x_pref = SharedprefConstants.KEEP_PAST_PREF_DEFAULT;
            SharedPrefHelper.writeString(context, SharedprefConstants.KEEP_PAST_PREF, keep_past_x_pref);
        }
        ArrayList<String> ar = new ArrayList<String>(Arrays.asList(SharedprefConstants.KEEP_PAST_PREF_ARR));
        int target_max_days = SharedprefConstants.KEEP_PAST_PREF_ARR_INT[ar.indexOf(keep_past_x_pref)];
        long nowish = (new Date()).getTime();
        long comparator2 = nowish - 24*60*60*1000*target_max_days;
        return comparator2;
    }


    public static void cleanUpSyncInFirestoreForUser(FirebaseFirestore mFirestore, List<String> ids_to_delete_later, String uid, String parent_collection){
        try {
            for (String id : ids_to_delete_later) {
                mFirestore.collection(FirebaseConstants.TOSYNC_COLLECTION_PATH).document(uid).collection(parent_collection).document(id).delete();
            }
        } catch (Exception e) {
           Log.d(TAG, "Error deleting collection : " + e.getMessage());
        }
    }

    public static void getCasesFromGetterLink(Query getter_link, PullData pd, List<String> ids_to_delete_later, CaseRepo cr){
        if(pd!=null)pd.syncIsWorking(false);
        getter_link.get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Case> cases = new ArrayList<Case>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        QueryDocumentSnapshot d = document;
                        Case c_ = document.toObject(Case.class);
                        c_ = cleanUpBeforeAddingToRoom(c_);
                        cases.add(c_);
                    }
                    if(pd!=null) pd.syncIsWorking(true);
                    cr.insertAllFromFire(cases, pd, pd.getWatchForFBIDConflicts(), ids_to_delete_later); //OLGA CHECK PLEASE
                } else {
                    if(pd!=null) pd.cleanUpAndPopulateStories();
                }
            }
        })
        .addOnFailureListener(
                new OnFailureListener(){
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(pd!=null) pd.cleanUpAndPopulateStories();
                    }
                }
        );
    }

    public static void getStoriesFromGetterLink(Query getter_link, PullData pd, List<String> ids_to_delete_later, StoryRepo sr){
        pd.syncIsWorking(false);
        getter_link.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Story> stories = new ArrayList<Story>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        QueryDocumentSnapshot d = document;
                        Story s_ = document.toObject(Story.class);
                        s_ = cleanUpBeforeAddingToRoom(s_);
                        stories.add(s_);
                    }
                    pd.syncIsWorking(true);
                    sr.insertAllFromFire(stories, pd, pd.getWatchForFBIDConflicts(), ids_to_delete_later); //OLGA CHECK PLEASE
                } else {
                    pd.cleanUpAndPopulateFiles();
                }
            }
        })
        .addOnFailureListener(
                new OnFailureListener(){
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.cleanUpAndPopulateFiles();
                    }
                }
        );
    }


    public static void getLatestorOldestStoriesAttachedFilesFire(long cutoff, String case_fbid, FirebaseFirestore m_firestore, StoryRepo sr, FileRepo fr, String direction, CallbackReceivedDataGeneral callback){
        Query qr = null;
        if(direction.equals(MethodConstants.FIRESTORE_GET_LATEST)){
            qr = m_firestore.collection(FirebaseConstants.STORIES_COLLECTION_PATH)
                    .whereEqualTo(FirebaseConstants.CASE_FB_ID, case_fbid)
                    .whereGreaterThanOrEqualTo(FirebaseConstants.UPDATED_FIREBASE_AT, cutoff - 20);
        }else if(direction.equals(MethodConstants.FIRESTORE_GET_OLDER)){
            qr = m_firestore.collection(FirebaseConstants.STORIES_COLLECTION_PATH)
                    .whereEqualTo(FirebaseConstants.CASE_FB_ID, case_fbid)
                    .orderBy(FirebaseConstants.UPDATED_FIREBASE_AT, Query.Direction.DESCENDING)
                    .startAt(cutoff)
                    .limit(FirebaseConstants.PAGINATION_STEP);
        }
        boolean isNetworkAvailable = NetworkStateChangeBroadcaster.getIsConnected();
        if(isNetworkAvailable && qr!=null) {

            qr.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Story> stories = new ArrayList<Story>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Story s_ = document.toObject(Story.class);
                            s_ = cleanUpBeforeAddingToRoom(s_);
                            stories.add(s_);
                            getTheFilesForStoryFire(cutoff, fr, s_.getFbId(), m_firestore, direction);
                        }
                        sr.insertAllFromFire(stories, null, true, null);
                        if(callback != null){
                            callback.callbackReceivedData(null, stories, null);
                        }
                    } else {
                        if(callback != null){
                            callback.callbackReceivedData(null, null, null);
                        }
                    }
                }
            }).addOnFailureListener(
                    new OnFailureListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(callback != null){
                                callback.callbackReceivedData(null, null, null);
                            }
                        }
                    });
        }else{
            if(callback != null){
                callback.callbackReceivedData(null, null, null);
            }
        }
    }

    private static void getTheFilesForStoryFire(Long cutoff, FileRepo fr, String story_fbid, FirebaseFirestore m_firestore, String direction){
        Query qr = null;
        if(direction.equals(MethodConstants.FIRESTORE_GET_LATEST)){
            qr =  m_firestore.collection(FirebaseConstants.FILES_COLLECTION_PATH)
                    .whereEqualTo(FirebaseConstants.STORY_FB_ID, story_fbid);
        }else if(direction.equals(MethodConstants.FIRESTORE_GET_OLDER)){
            qr =  m_firestore.collection(FirebaseConstants.FILES_COLLECTION_PATH)
                    .whereEqualTo(FirebaseConstants.STORY_FB_ID, story_fbid);
        }

        if(qr!=null &&NetworkStateChangeBroadcaster.getIsConnected()){
           qr.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Attachedfile> files = new ArrayList<Attachedfile>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Attachedfile f_ = document.toObject(Attachedfile.class);
                            f_ = cleanUpBeforeAddingToRoom(f_);
                            files.add(f_);
                        }
                        fr.insertAllFromFire(files, null,  true, null);
                    } else {
                        //nothing here
                    }
                }
            }).addOnFailureListener(
                    new OnFailureListener(){
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //nothing here
                        }
                    });
        }
    }


    public static void getFilesFromGetterLink(Query getter_link, PullData pd, List<String> ids_to_delete_later, FileRepo fr){
        pd.syncIsWorking(false);
        getter_link.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Attachedfile> afs = new ArrayList<Attachedfile>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Attachedfile af_ = document.toObject(Attachedfile.class);
                        Uri uri = Uri.parse(af_.getLocalFilePath());
                        af_ = cleanUpBeforeAddingToRoom(af_);
                        afs.add(af_);
                    }
                    pd.syncIsWorking(true);
                    fr.insertAllFromFire(afs, pd,  pd.getWatchForFBIDConflicts(), ids_to_delete_later);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        }).addOnFailureListener(
                new OnFailureListener(){
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.populateUsers();
                    }
                }
        );
    }

    private static Attachedfile cleanUpBeforeAddingToRoom(Attachedfile af_){
        af_.setLocalStatus(FILE_NOT_AVAIL_LOCALLY);
        af_.setFid(0);
        af_.setSid(0);
        af_.setCid(0);
        if(af_.getUpdatedFirebaseAt() == null || af_.getUpdatedFirebaseAt() == 0){
            af_.setUpdatedFirebaseAt(new Date().getTime());
        }
        return af_;
    }

    private static Story cleanUpBeforeAddingToRoom(Story af_){
        af_.setSid(0);
        af_.setCid(0);
        if(af_.getUpdatedFirebaseAt() == null || af_.getUpdatedFirebaseAt() == 0){
            af_.setUpdatedFirebaseAt(new Date().getTime());
        }
        return af_;
    }

    private static Case cleanUpBeforeAddingToRoom(Case af_){
        af_.setCid(0);
        af_.setStringThree(RoomConstants.IS_NOT_FAVOURITE_ED);
        if(af_.getUpdatedFirebaseAt() == null || af_.getUpdatedFirebaseAt() == 0){
            af_.setUpdatedFirebaseAt(new Date().getTime());
        }
        return af_;
    }
}
