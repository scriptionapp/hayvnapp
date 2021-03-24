package com.hayvn.hayvnapp.Activities;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.FirebaseConstants;
import com.hayvn.hayvnapp.Constant.MethodConstants;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.Constant.TextConstants;
import com.hayvn.hayvnapp.Dialog.DialogPushPullData;
import com.hayvn.hayvnapp.Dialog.DialogView;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Interfaces.CallbackEmptyAction;
import com.hayvn.hayvnapp.Model.User;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.UserRepo;
import com.hayvn.hayvnapp.Utilities.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hayvn.hayvnapp.databinding.ActivityLoginBinding;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.hayvn.hayvnapp.Constant.IntentConstants.USER_EMAIL;

public class LoginActivity extends AppCompatActivity implements
        CallbackEmptyAction {
    private static final String TAG = "LoginActivity";
    Context context;
    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;
    String email_from_login = "";
    boolean restore_media = true;
    CallbackEmptyAction callback_empty_act = this;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();


        Intent intent = getIntent();
        if (intent.hasExtra(USER_EMAIL)) {
            email_from_login = (Objects.requireNonNull(intent.getExtras())).getString(USER_EMAIL);
        }
        if(email_from_login != null){
            binding.edtEmail.setText(email_from_login);
        }

        binding.tvRegister.setOnClickListener(v -> goToRegister());

        binding.forgotPwd.setOnClickListener(v -> {
            String eml = Utilities.getInstance().getString(binding.edtEmail);
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
                                    Toast.makeText(context, TextConstants.AN_ERROR + ": " +
                                            e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }else{
                    Toast.makeText(context, getString(R.string.enter_email_format), Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(context, getString(R.string.enter_email), Toast.LENGTH_SHORT).show();
            }
        });

       binding.btnSignIn.setOnClickListener(v -> {
            if (Utilities.getInstance().isNetworkAvailable(context)) {
                if (Utilities.getInstance().isValid(binding.edtEmail, binding.edtPasswordLogin)) {
                    deactivateButton(binding.btnSignIn);
                    signIn(Utilities.getInstance().getString(binding.edtEmail), Utilities.getInstance().getString(binding.edtPasswordLogin));
                }
            } else Toast.makeText(context, getString(R.string.internet_problem), Toast.LENGTH_SHORT).show();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void deactivateButton(Button btn){
        btn.setActivated(false);
        btn.setAlpha(0.5f);
    }
    private void reactivateButton(Button btn){
        btn.setActivated(true);
        btn.setAlpha(1.0f);
    }

    private void goToRegister() {
        startActivity(new Intent(context, RegisterActivity.class));
        finish();
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    reactivateButton(binding.btnSignIn);
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "login success");
                        if (Utilities.getInstance().isNetworkAvailable(context)) {
                            getUser(mAuth.getUid());
                        } else Toast.makeText(context, getString(R.string.internet_problem), Toast.LENGTH_SHORT).show();
                    } else {try {
                                throw task.getException();
                            } catch(FirebaseAuthInvalidUserException e) {
                                Toast.makeText(context, getString(R.string.login_fail), Toast.LENGTH_LONG).show();
                            } catch(Exception e) {
                                Log.w(TAG, "loginWithEmail:failure", task.getException());
                            }
                    }
                });
    }

    private void getUser(String userId) {
        mFirestore.collection(FirebaseConstants.USERS_COLLECTION_PATH).document(userId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    User user = task.getResult().toObject(User.class);
                    new UserRepo(getApplication()).insert(user, null, false);
                    assert user != null;
                    // we take the old pin code
                    SharedPrefHelper.writeString(context, Constant.PIN, user.getPin());

                    if (Utilities.getInstance().isNetworkAvailable(context)) {
                        handleLoginDataRestore();
                    } else {
                        Toast.makeText(context, getString(R.string.internet_problem), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "Failed to sync user info from server");
                }
            }
        });
    }

    public void handleLoginDataRestore(){
        moveToNextActivity();
    }

    public void moveToNextActivity(){
        SharedPrefHelper.writeString(context, SharedprefConstants.FOR_WELCOME_DIALOG,
                SharedprefConstants.FOR_WELCOME_DIALOG_VAL_LOGIN);
        SharedPrefHelper.writeString(context, SharedprefConstants.KEEP_PAST_PREF,
                SharedprefConstants.KEEP_PAST_PREF_DEFAULT);
        if (TextUtils.isEmpty(SharedPrefHelper.readString(context, Constant.PIN)) ||
                SharedPrefHelper.readString(context, Constant.PIN) == null) {
            startActivity(new Intent(context, CreatePinActivity.class));
            finish();
        }else{
            startActivity(new Intent(context, PinActivity.class));
            finish();
        }
    }

    @Override
    public void onCallbackEmptyAction() {
        moveToNextActivity();
    }
}
