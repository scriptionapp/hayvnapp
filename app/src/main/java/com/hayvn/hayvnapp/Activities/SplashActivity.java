package com.hayvn.hayvnapp.Activities;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Helper.NetworkStateChangeBroadcaster;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Interfaces.CallbackEmptyAction;
import com.hayvn.hayvnapp.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity implements //this one is not Base activity!
        CallbackEmptyAction{
    private static final String TAG = "SPLASH";
    Context context;
    FirebaseAuth mAuth;
    CallbackEmptyAction callback_empty_act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        context = this;
        mAuth = FirebaseAuth.getInstance();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity();
            }
        }, 500);
        callback_empty_act = this;

        NetworkStateChangeBroadcaster.launchNetworkBroadcaster(context);
    }

    private void startActivity() {
        if (mAuth.getCurrentUser() == null) {
            goToLogin();
        } else {
            goToEnterPINorPullData();
        }
    }

    private void goToLogin() {
        startActivity(new Intent(context, RegisterActivity.class));
        finish();
    }

    public void goToEnterPINorPullData() {
            if (SharedPrefHelper.readString(context, Constant.PIN) == null)
                showCreatePIN();
            else showEnterPIN();
    }

    private void showCreatePIN() {
        startActivity(new Intent(context, CreatePinActivity.class));
        finish();
    }

    private void showEnterPIN() {
        startActivity(new Intent(context, PinActivity.class));
        finish();
    }

    public void moveToNextActivity(){
        if (TextUtils.isEmpty(SharedPrefHelper.readString(context, Constant.PIN)) ||
                SharedPrefHelper.readString(context, Constant.PIN) == null) {
            showCreatePIN();
            finish();
        }else{
            showEnterPIN();
            finish();
        }
    }

    @Override
    public void onCallbackEmptyAction() {
        moveToNextActivity();
    }
}
