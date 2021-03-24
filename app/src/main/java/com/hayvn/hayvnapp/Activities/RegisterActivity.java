package com.hayvn.hayvnapp.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Interfaces.CallbackEmptyAction;
import com.hayvn.hayvnapp.Interfaces.CallbackEmptyAction2;
import com.hayvn.hayvnapp.Interfaces.CallbackInt;
import com.hayvn.hayvnapp.Model.User;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.UserRepo;
import com.hayvn.hayvnapp.Utilities.Utilities;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.hayvn.hayvnapp.databinding.ActivityRegisterBinding;


public class RegisterActivity extends AppCompatActivity implements
        CallbackInt,
        CallbackEmptyAction,
        CallbackEmptyAction2
{

    Context context;
    FirebaseAuth mAuth;
    boolean userexists_error = false;
    CallbackInt callback;
    private String user_email = "";
    private UserRepo userRepo;
    CallbackEmptyAction2 callback_empty2 = this;

    private static final String TAG = "REGISTER_ACTIVITY";

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        userRepo = new UserRepo(getApplication());
        mAuth = FirebaseAuth.getInstance();
        binding.tvLogin.setOnClickListener(v -> goToLogin());
        binding.btnCreate.setOnClickListener(v -> {
            if (Utilities.getInstance().isNetworkAvailable(context)) {
                if (Utilities.getInstance().isValid(binding.edtEmail, binding.edtPassword)) {
                    createUser(Utilities.getInstance().getString(binding.edtEmail), Utilities.getInstance().getString(binding.edtPassword));
                }
            } else Toast.makeText(context, getString(R.string.internet_problem), Toast.LENGTH_SHORT).show();
        });
    }

    private void goToLogin() {
        Intent myIntent = new Intent(context, LoginActivity.class);
        if(userexists_error){
            myIntent.putExtra("useremail", Utilities.getInstance().getString(binding.edtEmail));
        }
        startActivity(myIntent);
        finish();
    }

    private void createUser(String email, String password) {
        callback = this;
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        user_email = email;
                        userRepo.retrieveUserCount(callback);
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(context, getString(R.string.user_exists_error), Toast.LENGTH_SHORT).show();
                            userexists_error = true;
                        }else{
                            Toast.makeText(context, getString(R.string.reg_fail) + " " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void callbackReturnInt(Integer i) {
        User localUser = new User();
        localUser.setEmail(user_email);
        localUser.setNotification(true);
        localUser.setTempPinTime("");
        localUser.setTempPinValue("");
        localUser.setDoctorId(String.valueOf(i));
        String userId = mAuth.getUid();
        assert userId != null;
        localUser.setId(userId);
        saveUser(localUser);
    }

    private void saveUser(User localUser) {
        new UserRepo(getApplication()).insert(localUser, callback_empty2, true);
        //calls onCallbackEmptyAction2
    }

    @Override
    public void onCallbackEmptyAction() {
        proceedToNext();
    }

    @Override
    public void onCallbackEmptyAction2() {
        proceedToNext();
    }

    private void proceedToNext(){
        SharedPrefHelper.writeString(context, SharedprefConstants.KEEP_PAST_PREF, SharedprefConstants.KEEP_PAST_PREF_DEFAULT);
        startActivity(new Intent(context, CreatePinActivity.class));
        finish();
    }
}
