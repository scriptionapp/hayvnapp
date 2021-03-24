package com.hayvn.hayvnapp.Activities;

import android.annotation.SuppressLint;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Helper.UpdatableBCrypt;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Model.User;
import com.hayvn.hayvnapp.Repository.UserRepo;
import com.hayvn.hayvnapp.ViewModel.UserViewModel;
import com.hayvn.hayvnapp.databinding.ActivityForgotBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import online.devliving.passcodeview.PasscodeView;

import static com.hayvn.hayvnapp.Constant.TextConstants.PIN_RESET_SENT;

public class ForgotActivity extends AppCompatActivity {

    Boolean allowPinChange = true;
    String timePin;
    String tempPinValue;
    User mySetting;
    static final String TAG = "FORGOT_PIN";
    private ActivityForgotBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Context context = this;
        UpdatableBCrypt mupdatableBCrypt = new UpdatableBCrypt(4);
        binding.textTempOldPin.setText(PIN_RESET_SENT);
        binding.sendPinAgain.setVisibility(View.VISIBLE);

        UserViewModel mViewModel;
        mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mViewModel.getSettings().observe(this, setting -> {
            mySetting = setting;
            assert mySetting != null;
            tempPinValue = mySetting.getTempPinValue();
            timePin = mySetting.getTempPinTime();

            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            Date pinDate;
            long diffMinutes;
            try {
                pinDate = formatter.parse(timePin);
                assert pinDate != null;
                long diff = date.getTime() - pinDate.getTime();
                diffMinutes = diff / (60 * 1000) % 60;
                if (diffMinutes > 10) {
                    notAllowPinChange();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });


        binding.oldPasscodeView.postDelayed(binding.oldPasscodeView::requestToShowKeyboard, 400);
        binding.oldPasscodeView.setPasscodeEntryListener(passcode -> {
            // compare time difference
            if (!allowPinChange) {
                Toast.makeText(context, getString(R.string.time_limit), Toast.LENGTH_SHORT).show();
            } else {
                if (passcode.equals(tempPinValue)) {
                    binding.lyNewPin.setVisibility(View.VISIBLE);
                    binding.sendPinAgain.setVisibility(View.GONE);

                    mySetting.setTempPinTime("");
                    mySetting.setTempPinValue("");
                    new UserRepo(getApplication()).update(mySetting);
                    binding.newPasscodeView.postDelayed(binding.newPasscodeView::requestToShowKeyboard, 400);
                    binding.oldPasscodeView.setVisibility(View.GONE);
                } else {
                   binding.oldPasscodeView.clearText();
                    Toast.makeText(context, getString(R.string.wrong_reset_pin), Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.newPasscodeView.setPasscodeEntryListener(passcode -> {
            if (passcode.length() == 4)
                binding.confirmPasscodeView.postDelayed(binding.confirmPasscodeView::requestToShowKeyboard, 400);
        });

        binding.confirmPasscodeView.setPasscodeEntryListener(passcode -> {
            if (passcode.equals(binding.newPasscodeView.getText().toString())) {
                putPinIntoDatabase(mupdatableBCrypt.hash(passcode));
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

        binding.sendPinAgain.setOnClickListener(v -> {
            preparePinDatabase();
        });

       binding.cancelPinReset.setOnClickListener(v -> {
            mySetting.setTempPinTime("");
            mySetting.setTempPinValue("");
            new UserRepo(getApplication()).update(mySetting);
            startActivity(new Intent(context, PinActivity.class));
            finish();
        });
    }

    private void preparePinDatabase() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        // set Pin
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
            new UserRepo(this.getApplication()).update(setting);
        });

    }

    private void notAllowPinChange() {
        allowPinChange = false;
    }

    private void putPinIntoDatabase(String pin) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        UserViewModel mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mViewModel.getSettings().observe(this, setting -> {
            assert setting != null;
            setting.setTempPinTime("");
            setting.setTempPinValue("");
            setting.setPin(pin);
            new UserRepo(this.getApplication()).update(setting);
        });

    }
}
