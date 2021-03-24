package com.hayvn.hayvnapp.Repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.hayvn.hayvnapp.Activities.SettingActivity;
import com.hayvn.hayvnapp.Constant.FirebaseConstants;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.DataBase.AppDB;
import com.hayvn.hayvnapp.FirebaseHelper.UserCounter;
import com.hayvn.hayvnapp.Helper.NetworkStateChangeBroadcaster;
import com.hayvn.hayvnapp.Helper.PullData;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Interface.UserDAO;
import com.hayvn.hayvnapp.Interfaces.CallbackEmptyAction;
import com.hayvn.hayvnapp.Interfaces.CallbackEmptyAction2;
import com.hayvn.hayvnapp.Interfaces.CallbackInt;
import com.hayvn.hayvnapp.Interfaces.CallbackUpdatedUser;
import com.hayvn.hayvnapp.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.hayvn.hayvnapp.R;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;

import static com.hayvn.hayvnapp.Constant.FirebaseConstants.USERS_COLLECTION_PATH;
import static com.hayvn.hayvnapp.Constant.TextConstants.PROBLEM_INTERNET;

public class UserRepo {

    private UserDAO mUserDAO;
    private final String TAG = "USER_REPO";
    private static FirebaseFirestore mFirestore;
    private static FirebaseAuth mAuth;
    private Application application;

    public UserRepo(Application application) {
        this.application = application;
        AppDB db = AppDB.getAppDatabase(application);
        mUserDAO = db.userDAO();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public LiveData<User> getSetting() {
        return mUserDAO.findOnlyUser();
    }

    public void insert(User word, CallbackEmptyAction2 callback, boolean with_firestore) {
        new UserRepo.insertAsyncTask(mUserDAO, callback, application, mFirestore, with_firestore).execute(word);
    }

    private static class insertAsyncTask extends AsyncTask<User, Void, Void> {

        private UserDAO mAsyncTaskDao;
        private CallbackEmptyAction2 callback;
        private Application app;
        private FirebaseFirestore mFirestore;
        private User user;
        private boolean with_firestore;

        insertAsyncTask(UserDAO dao, CallbackEmptyAction2 callback, Application app, FirebaseFirestore mFirestore, boolean with_firestore) {
            mAsyncTaskDao = dao;
            this.callback = callback;
            this.app = app;
            this.mFirestore = mFirestore;
            this.with_firestore = with_firestore;
        }

        @Override
        protected Void doInBackground(final User... params) {
            user = params[0];
            mAsyncTaskDao.insertAll(user);
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            if(with_firestore) {
                mFirestore.collection(USERS_COLLECTION_PATH)
                        .document(user.getId())
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (callback != null) callback.onCallbackEmptyAction2();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast toast = Toast.makeText(app.getBaseContext(),
                                        app.getBaseContext().getString(R.string.reg_fail), Toast.LENGTH_SHORT);
                            }
                        });
            }else{
                if (callback != null) callback.onCallbackEmptyAction2();
            }
        }
    }

    public void update(User setting) {
        new UserRepo.updateAsyncTask(mUserDAO, null).execute(setting);
        setUserSettings(setting);
    }

    public void update(User setting, CallbackUpdatedUser listener) {

        new UserRepo.updateAsyncTask(mUserDAO, listener).execute(setting);
        setUserSettings(setting);
    }

    private static class updateAsyncTask extends AsyncTask<User, Void, Void> {

        private UserDAO mAsyncTaskDao;
        private CallbackUpdatedUser listener;

        updateAsyncTask(UserDAO dao, CallbackUpdatedUser listener) {
            this.listener = listener;
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final User... params) {
            mAsyncTaskDao.updateSetting(params[0]);
            mFirestore.collection(USERS_COLLECTION_PATH)
                    .document(mAuth.getUid())
                    .set(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            if(listener != null) listener.onUserUpdatedLocally();
        }
    }

    public void delete(User word) {
        new UserRepo.deleteAsyncTask(mUserDAO).execute(word);
    }

    private static class deleteAsyncTask extends AsyncTask<User, Void, Void> {

        private UserDAO mAsyncTaskDao;

        deleteAsyncTask(UserDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final User... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private void setUserSettings(User setting) {

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            mFirestore.collection("users").document(userId).set(setting, SetOptions.merge());
        }
    }

    public void populateUserFromFirestore(PullData pd){
        boolean isNetworkAvailable = NetworkStateChangeBroadcaster.getIsConnected();
        if(isNetworkAvailable) {
            mFirestore.collection(USERS_COLLECTION_PATH)
                    .document(mAuth.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    User usr = new User();
                                    usr = document.toObject(User.class);
                                    usr.setId(mAuth.getUid());
                                    if(usr.getId() == null){
                                        usr.setId(mAuth.getUid()); //TODO aren't we storing user settings...
                                    }
                                    insertUserFromFire(usr, pd);
                                } else {
                                    Log.d(TAG, "No such document");
                                    moveToNextPd(pd);
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                                moveToNextPd(pd);
                            }
                        }
                    })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    moveToNextPd(pd);
                }
            });
        }else{
            pd.afterPopulateMediaStep(false);
            Toast.makeText(application.getBaseContext(),
                    PROBLEM_INTERNET, Toast.LENGTH_SHORT).show();
        }
    }

    public void retrieveUserCount(CallbackInt callback){

        mFirestore.collection(FirebaseConstants.SHARED_COLLECTION_PATH)
                .document(FirebaseConstants.NUMBER_SHARDS).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            int current_num_shards = Integer.parseInt(task.getResult().get(FirebaseConstants.CURRENT_PREFIX).toString());
                            SharedPrefHelper.writeInteger(application.getApplicationContext(), SharedprefConstants.NUMBER_OF_SHARDS, current_num_shards);
                            countUsers(callback, current_num_shards);
                        } else {
                            Log.d(TAG, "Error in getting patient prefix");
                        }
                    }
                });
    }

    private void countUsers(CallbackInt callback, int shards){
        UserCounter userCounter = new UserCounter();
        userCounter.incrementCounter(mFirestore, shards)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        userCounter.getCount(mFirestore)
                                .addOnSuccessListener(new OnSuccessListener<Integer>() {
                                    @Override
                                    public void onSuccess(Integer i) {
                                        callback.callbackReturnInt(i);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("COUNTER", "Counter get failed. Registration not complete because of " + e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("COUNTER", "Counter Increment failed because of "+ e);
                    }
                });
    }

    /////////////////////////////////////////////////////////////////////
    public void insertUserFromFire (User usr, PullData pd) {
        new insertUserFromFireAsyncTask(mUserDAO, pd).execute(usr);
    }

    private static class insertUserFromFireAsyncTask extends AsyncTask<User, Void, Void> {

        private WeakReference<UserDAO> mAsyncTaskDao;
        private PullData pd;

        insertUserFromFireAsyncTask(UserDAO dao, PullData pd) {
            mAsyncTaskDao = new WeakReference<>(dao);
            this.pd = pd;
        }

        @Override
        protected Void doInBackground(final User... params) {
            mAsyncTaskDao.get().insertAll(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute (Void result){
            moveToNextPd(pd);
        }
    }

    private static void moveToNextPd(PullData pd){
        try {
            pd.prePopulateMedia();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /////////////////////////////////////////////////////////////////////



}
