package com.hayvn.hayvnapp.Activities;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Helper.UpdatableBCrypt;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.UserRepo;
import com.hayvn.hayvnapp.ViewModel.UserViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hayvn.hayvnapp.databinding.ActivityCreatePinBinding;

import online.devliving.passcodeview.PasscodeView;

public class CreatePinActivity extends AppCompatActivity {

    private final String TAG = "CREATEPIN";
    Context context;
    FirebaseAuth mAuth;
    private ActivityCreatePinBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatePinBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        context = this;
        mAuth = FirebaseAuth.getInstance();
        UpdatableBCrypt mupdatableBCrypt = new UpdatableBCrypt(4);
        binding.passcodeView.postDelayed(binding.passcodeView::requestToShowKeyboard, 400);
        SharedPrefHelper.clear(context, Constant.PIN_TEMP);

        binding.passcodeView.setPasscodeEntryListener(passcode -> {
            binding.passcodeView.clearText();
            if (TextUtils.isEmpty(SharedPrefHelper.readString(context, Constant.PIN_TEMP)) ||
                    SharedPrefHelper.readString(context, Constant.PIN_TEMP) == null) {
                SharedPrefHelper.writeString(context, Constant.PIN_TEMP, passcode);
               binding.txtChange.setText(getString(R.string.confirm_pin));
            } else if (SharedPrefHelper.readString(context, Constant.PIN_TEMP).equals(passcode)) {
                updateSetting(mupdatableBCrypt.hash(passcode));
                Toast.makeText(context, getString(R.string.pin_set), Toast.LENGTH_SHORT).show();
                SharedPrefHelper.writeString(context, Constant.PIN, mupdatableBCrypt.hash(passcode));
                startActivity(new Intent(getApplicationContext(), MainAppActivity.class));
                finish();

            } else if (!SharedPrefHelper.readString(context, Constant.PIN_TEMP).equals(passcode)) {
                binding.passcodeView.clearText();
                Toast.makeText(context, getString(R.string.pin_notmatch), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSetting(String pin) {
        UserViewModel mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mViewModel.getSettings().observe(this, setting -> {
            assert setting != null;
            setting.setPin(pin);
            new UserRepo(getApplication()).update(setting);
            FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
            String userId = mAuth.getUid();
            assert userId != null;
            mFirestore.collection("users").document(userId)
                    .update("pin", pin)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, getString(R.string.pin_saved), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, getString(R.string.pin_error), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPrefHelper.writeString(getApplicationContext(), Constant.PIN, null);
    }
}
