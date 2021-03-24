package com.hayvn.hayvnapp.Activities;

import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.hayvn.hayvnapp.Constant.IntentConstants;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.Constant.TextConstants;
import com.hayvn.hayvnapp.Helper.PullData;
import com.hayvn.hayvnapp.Helper.UpdatableBCrypt;
import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.ViewModel.UserViewModel;

import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Repository.UserRepo;
import com.hayvn.hayvnapp.databinding.ActivityPinBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import online.devliving.passcodeview.PasscodeView;

public class PinActivity extends AppCompatActivity {
    private static final String TAG = "PIN_ACT";
    Context context;
    String intent_message = null;
    int wrong_pin_attempts;
    String too_many_pins = null;
    final FirebaseAuth[] mAuth = {null};

    private ActivityPinBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        Intent intent = getIntent();
        if(intent != null && intent.getExtras() != null){
            intent_message = intent.getExtras().getString(IntentConstants.WHO_CALLED_PIN_ACT);
            if(intent_message != null){
                Toast.makeText(context, getString(R.string.please_reenter_pin_security), Toast.LENGTH_SHORT).show();
            }
        }

        UpdatableBCrypt mupdatableBCrypt = new UpdatableBCrypt(4);

        too_many_pins = SharedPrefHelper.readString(this.getBaseContext(), SharedprefConstants.PIN_ATTEMPTS_TOO_MANY);
        if(too_many_pins != null && too_many_pins.equals(SharedprefConstants.YES_PIN_ATTEMPTS_TOO_MANY )){
            startReauth();
        }

       binding.passcodeView.postDelayed(binding.passcodeView::requestToShowKeyboard, 400);

        binding.passcodeView.setPasscodeEntryListener(passcode -> {
            binding.passcodeView.clearText();
            boolean cond = mupdatableBCrypt.verifyHash(passcode, SharedPrefHelper.readString(context, Constant.PIN));
            boolean attempts_cond = (wrong_pin_attempts < 5);
            if (cond && attempts_cond) {
                if(mAuth[0] == null){
                    mAuth[0] = FirebaseAuth.getInstance();
                }
                SharedPrefHelper.writeString(this.getBaseContext(), SharedprefConstants.PIN_ATTEMPTS_TOO_MANY, null );

                Objects.requireNonNull(mAuth[0].getCurrentUser()).reload()
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "Couldn't get auth");
                                e.printStackTrace();
                                if (e instanceof FirebaseAuthInvalidUserException) {
                                    eraseAndSignOut((FirebaseAuthInvalidUserException) e);
                                }else if(e instanceof FirebaseNetworkException){
                                    proceedToNextAfterPin();
                                }else{
                                    Toast.makeText(context, TextConstants.AN_ERROR, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        proceedToNextAfterPin();
                    }
                });

            } else if(attempts_cond) {
                binding.passcodeView.clearText();
                wrong_pin_attempts++;
                Toast.makeText(context,
                        getString(R.string.wrong_pin) + " ("+wrong_pin_attempts+" "+TextConstants.ATTEMPTS+")",
                        Toast.LENGTH_SHORT).show();
            } else{
                SharedPrefHelper.writeString(this.getBaseContext(), SharedprefConstants.PIN_ATTEMPTS_TOO_MANY, SharedprefConstants.YES_PIN_ATTEMPTS_TOO_MANY );
                startReauth();
            }
        });

        binding.txtResetPin.setOnClickListener(v -> {
            resetPinRequest();
        });


        binding.txtUpdatePin.setOnClickListener(v -> {
            startActivity(new Intent(context, UpdatePinActivity.class));
            finish();
        });
    }

    private void eraseAndSignOut(FirebaseAuthInvalidUserException e){
        Log.d(TAG, "user doesn't exist anymore");
        if(((FirebaseAuthInvalidUserException) e).getErrorCode().equals("ERROR_USER_DISABLED")){
            Toast.makeText(context, TextConstants.USER_DISABLED, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, TextConstants.AUTH_PROBLEM, Toast.LENGTH_SHORT).show();
        }
        PullData pd = new PullData();
        pd.eraseDB(context, "PinAct");
    }

    private void resetPinRequest(){
        if(mAuth[0] == null){
            mAuth[0] = FirebaseAuth.getInstance();
        }
        if(mAuth[0] != null) {
            Objects.requireNonNull(mAuth[0].getCurrentUser()).reload()
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Couldn't get auth");
                            e.printStackTrace();
                            if (e instanceof FirebaseAuthInvalidUserException) {
                                eraseAndSignOut((FirebaseAuthInvalidUserException) e);
                            } else if (e instanceof FirebaseNetworkException) {
                                preparePinDatabase();
                                startActivity(new Intent(context, ForgotActivity.class));
                                finish();
                            } else {
                                Toast.makeText(context, TextConstants.AN_ERROR, Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            preparePinDatabase();
                            startActivity(new Intent(context, ForgotActivity.class));
                            finish();
                        }
                    });
        }
    }

    private void startReauth(){
        Toast.makeText(context, getString(R.string.wrong_pin_exceeded), Toast.LENGTH_SHORT).show();
        Intent intent1 = new Intent(this.getBaseContext(), ReauthenticateActivity.class);
        startActivity(intent1);
    }
    private void proceedToNextAfterPin(){
        SharedPrefHelper.writeString(context, SharedprefConstants.TIME_LAST_PIN, String.valueOf((new Date()).getTime()) );
        if(intent_message==null || !intent_message.equals(IntentConstants.REVERIFY_PIN)){
            startActivity(new Intent(getApplicationContext(), MainAppActivity.class));
        }
        finish();
    }

    private void preparePinDatabase() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        double tempPin = Math.random();
        String tempPinStr = String.valueOf(tempPin);
        tempPinStr = tempPinStr.substring(2, 6);
        String finalTempPinStr = tempPinStr;
        UserViewModel mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mViewModel.getSettings().observe(this, setting -> {
            String tempPinNow = formatter.format(date);
            setting.setTempPinTime(tempPinNow);
            setting.setTempPinValue(finalTempPinStr);
            new UserRepo(this.getApplication()).update(setting);
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), TextConstants.ENTER_PIN, Toast.LENGTH_SHORT).show();
    }

    public void onDBErased(){
        FirebaseAuth.getInstance().signOut();
        context.startActivity(new Intent(context, LoginActivity.class));
    }

}
