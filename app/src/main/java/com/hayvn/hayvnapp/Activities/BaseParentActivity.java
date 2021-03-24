package com.hayvn.hayvnapp.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.IntentConstants;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;

import java.util.Date;

public class BaseParentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        checkPinReenter();
    }

    @Override
    protected void onResume(){
        super.onResume();
        checkPinReenter();
    }

    private void checkPinReenter(){
        String lastTime = SharedPrefHelper.readString(this.getBaseContext(), SharedprefConstants.TIME_LAST_PIN);
        if(lastTime != null){
            long time_ = Long.parseLong(lastTime);
            long cur_time = (new Date()).getTime();
            long delta = (cur_time - time_)/1000/60;
            if(delta > 10){
                Intent intent = new Intent(this.getBaseContext(), PinActivity.class);
                intent.putExtra(IntentConstants.WHO_CALLED_PIN_ACT, IntentConstants.REVERIFY_PIN);
                startActivity(intent);
            }
        }else{
            SharedPrefHelper.writeString(this.getBaseContext(), SharedprefConstants.TIME_LAST_PIN, String.valueOf((new Date()).getTime()) );
        }
    }

    public void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
