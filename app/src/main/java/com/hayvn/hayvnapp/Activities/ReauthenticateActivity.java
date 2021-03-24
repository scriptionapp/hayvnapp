package com.hayvn.hayvnapp.Activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.Constant.TextConstants;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Utilities.Utilities;
import com.hayvn.hayvnapp.databinding.ActivityReauthenticateBinding;

import org.jetbrains.annotations.NotNull;


import static com.hayvn.hayvnapp.Constant.TextConstants.AN_ERROR;

public class ReauthenticateActivity extends AppCompatActivity {


    private static final String TAG = "REAUTH_ACT";
    Context context;
    FirebaseAuth mAuth;
    private ActivityReauthenticateBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReauthenticateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        mAuth = FirebaseAuth.getInstance();
        binding.forgotPwdReauth.setOnClickListener(v -> {
            String eml = Utilities.getInstance().getString(binding.edtEmailReauth);
            if(eml != null) {
                if(Utilities.getInstance().checkEmailOk(eml)) {
                    mAuth.sendPasswordResetEmail(eml)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, getString(R.string.pw_reset_ok), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context,
                                            AN_ERROR + e.toString(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }else{
                    Toast.makeText(context, getString(R.string.enter_email_format), Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(context, getString(R.string.enter_email), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSignInReauth.setOnClickListener(v -> {
            if (Utilities.getInstance().isNetworkAvailable(context)) {
                if (Utilities.getInstance().isValid(binding.edtEmailReauth, binding.edtPasswordReauth)) {
                    signIn(Utilities.getInstance().getString(binding.edtEmailReauth), Utilities.getInstance().getString(binding.edtPasswordReauth));
                }
            } else Toast.makeText(context, getString(R.string.internet_problem), Toast.LENGTH_SHORT).show();
        });
    }


    private void signIn(String email, String password) {

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User re-authenticated.");
                        SharedPrefHelper.writeString(context, SharedprefConstants.PIN_ATTEMPTS_TOO_MANY, null );
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener(){
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, TextConstants.AUTH_PROBLEM + ": " +e.getClass().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onBackPressed(){
        //cannot back press out of here
    }

}
