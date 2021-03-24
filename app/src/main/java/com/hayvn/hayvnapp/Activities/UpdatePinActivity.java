package com.hayvn.hayvnapp.Activities;

import android.annotation.SuppressLint;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Helper.UpdatableBCrypt;
import com.hayvn.hayvnapp.Model.User;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.UserRepo;
import com.hayvn.hayvnapp.ViewModel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hayvn.hayvnapp.databinding.ActivityForgotBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

import online.devliving.passcodeview.PasscodeView;

import static com.hayvn.hayvnapp.Constant.FirebaseConstants.TEMP_PIN;
import static com.hayvn.hayvnapp.Constant.FirebaseConstants.USERS_COLLECTION_PATH;

public class UpdatePinActivity extends AppCompatActivity {

    private ActivityForgotBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Context context = this;
        UpdatableBCrypt mupdatableBCrypt = new UpdatableBCrypt(4);
        binding.textTempOldPin.setText(getString(R.string.current_pin));
        binding.oldPasscodeView.postDelayed(binding.oldPasscodeView::requestToShowKeyboard, 400);
        binding.oldPasscodeView.setPasscodeEntryListener(passcode -> {
            if (mupdatableBCrypt.verifyHash(passcode, SharedPrefHelper.readString(context, Constant.PIN))) {
               binding.lyNewPin.setVisibility(View.VISIBLE);
                binding.newPasscodeView.postDelayed(binding.newPasscodeView::requestToShowKeyboard, 400);
            } else
                binding.oldPasscodeView.clearText();
                Toast.makeText(context, getString(R.string.wrong_pin), Toast.LENGTH_SHORT).show();
        });

        binding.newPasscodeView.setPasscodeEntryListener(passcode -> {
            if (passcode.length() == 4)
                binding.confirmPasscodeView.postDelayed(binding.confirmPasscodeView::requestToShowKeyboard, 400);
        });

        binding.confirmPasscodeView.setPasscodeEntryListener(passcode -> {
            if (passcode.equals(binding.newPasscodeView.getText().toString())) {
                preparePinDatabase(mupdatableBCrypt.hash(passcode));
                SharedPrefHelper.writeString(context, Constant.PIN, mupdatableBCrypt.hash(passcode));
                startActivity(new Intent(getApplicationContext(), MainAppActivity.class));
                finish();
            } else {
                binding.newPasscodeView.clearText();
                binding.confirmPasscodeView.clearText();
                binding.newPasscodeView.postDelayed(binding.newPasscodeView::requestToShowKeyboard, 400);
                Toast.makeText(context, getString(R.string.pin_notmatch), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preparePinDatabase(String pin) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        double tempPin = Math.random();
        String tempPinStr = String.valueOf(tempPin);
        tempPinStr = tempPinStr.substring(2, 6);
        String finalTempPinStr = tempPinStr;
        UserViewModel mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mViewModel.getSettings().observe(this, setting -> {
            String tempPinNow = formatter.format(date);
            assert setting != null;
            setting.setTempPinTime(tempPinNow);
            setting.setTempPinValue(finalTempPinStr);
            setting.setPin(pin);
            new UserRepo(this.getApplication()).update(setting);
            updateFirestore(setting, finalTempPinStr);
        });

    }

    private void updateFirestore(User setting, String tempPin) {
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            mFirestore.collection(USERS_COLLECTION_PATH)
                    .document(userId).update(TEMP_PIN, tempPin);
        }
    }
}
