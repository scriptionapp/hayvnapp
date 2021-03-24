package com.hayvn.hayvnapp.Activities;

import android.app.Activity;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;


import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.MethodConstants;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.Dialog.DialogPushPullData;
import com.hayvn.hayvnapp.Dialog.DialogView;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Interfaces.CallbackUpdatedUser;
import com.hayvn.hayvnapp.Model.User;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.UserRepo;
import com.hayvn.hayvnapp.Utilities.DateUtils;
import com.hayvn.hayvnapp.ViewModel.UserViewModel;
import com.hayvn.hayvnapp.databinding.DrawerAccountSettingsBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static com.hayvn.hayvnapp.Constant.TextConstants.DOCTOR_CLUSTER_OPTIONS;
import static com.hayvn.hayvnapp.Constant.TextConstants.SYNCED_LAST;


public class SettingActivity extends BaseParentActivity implements CallbackUpdatedUser {

    private final static String TAG = "SETTING";
    Activity context;
    User mySetting;
    UserViewModel mViewModel;
    String outstandingFilesToPull;
    String[] options = DOCTOR_CLUSTER_OPTIONS;
    String[] sync_options = SharedprefConstants.KEEP_PAST_PREF_ARR;
    ArrayList<String> health_worker_ids = new ArrayList<String>(Arrays.asList(options));
    ArrayList<String> sync_options_list = new ArrayList<String>(Arrays.asList(sync_options));
    ArrayList<String> countries = new ArrayList<String>();
    private DrawerAccountSettingsBinding binding;

    @Override
    public void onUserUpdatedLocally() {
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DrawerAccountSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        setViewsValues();

        binding.defaultCountry.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s != null){
                    String new_sel = s.toString();
                    if(!countries.contains(new_sel)){
                        binding.defaultCountry.setTextColor( Color.RED);
                    }else{
                        binding.defaultCountry.setTextColor( Color.BLACK);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        outstandingFilesToPull = SharedPrefHelper.readString(context.getApplicationContext(), Constant.FIREBASESYNCSTATUS);
        if(outstandingFilesToPull==null){outstandingFilesToPull="";}
        binding.syncDataSettings.setOnClickListener(view_ -> {
            DialogPushPullData.getInstance().launchPushPullDataDialog(this, this.getApplication(), context, true, MethodConstants.PULL_WHAT_OTHERS);
        });

        mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mViewModel.getSettings().observe(this, setting -> {
            mySetting = setting;
            assert mySetting != null;
            binding.txtEmail.setText(mySetting.getEmail());
            binding.txtDoctorName.setText(mySetting.getStringThree());
            setSelectionCountry(mySetting.getStringTwo());
           binding.txtHospitalIdDoctor.setSelection(Math.max(health_worker_ids.indexOf(mySetting.getStringOne()), 0 ));
            binding.swNotification.setChecked(mySetting.isNotification());
        });


        binding.swNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(mySetting != null) {
                mySetting.setNotification(isChecked);
                mySetting.setEmail(binding.txtEmail.getText().toString());
                new UserRepo(getApplication()).update(mySetting);
            }
        });

        binding.txtChange.setOnClickListener(v -> DialogView.getInstance().showEmailChangeDialog(context, mySetting));
    }

    private void setViewsValues(){
        countries = SharedPrefHelper.getStringList(context, SharedprefConstants.COUNTRY_LIST_FULL);
        ArrayAdapter<String> countries_adapter = new ArrayAdapter<String>
                (context, android.R.layout.select_dialog_item, countries);
        binding.defaultCountry.setThreshold(0);//will start working from first character
        binding.defaultCountry.setAdapter(countries_adapter);

        String last_upd_str = SharedPrefHelper.readString(context, SharedprefConstants.TIME_LAST_SYNC);
        if(last_upd_str != null){
            Long last_upd = Long.parseLong(last_upd_str);
            String date_formatted = DateUtils.dateToStringTime(new Date(last_upd));
            binding.syncMessage.setText(SYNCED_LAST+date_formatted+")");
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item,
                health_worker_ids);
        binding.txtHospitalIdDoctor.setAdapter(spinnerArrayAdapter);

        ArrayAdapter<String> arr_adp_sync_keep = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item,
                sync_options_list);
        binding.dataSyncSettingSpin.setAdapter(arr_adp_sync_keep);

        String keep_past_x_pref = SharedPrefHelper.readString(context, SharedprefConstants.KEEP_PAST_PREF);
        if(keep_past_x_pref==null || keep_past_x_pref.equals("")){
            keep_past_x_pref = SharedprefConstants.KEEP_PAST_PREF_DEFAULT;
            SharedPrefHelper.writeString(context, SharedprefConstants.KEEP_PAST_PREF, keep_past_x_pref);
        }
        binding.dataSyncSettingSpin.setSelection(Math.max(sync_options_list.indexOf(keep_past_x_pref), 0 ));
    }

    private void setSelectionCountry(String country){
        binding.defaultCountry.setText(country);
        if(!countries.contains(country)){
            binding.defaultCountry.setTextColor( Color.RED);
        }else{
           binding.defaultCountry.setTextColor( Color.BLACK);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_complete_new_log) {
            SharedPrefHelper.writeString(context, SharedprefConstants.KEEP_PAST_PREF,  binding.dataSyncSettingSpin.getSelectedItem().toString());
            mySetting.setStringOne(binding.txtHospitalIdDoctor.getSelectedItem().toString());
            mySetting.setStringTwo(binding.defaultCountry.getText().toString());
            mySetting.setStringThree(binding.txtDoctorName.getText().toString());
            mViewModel.getSettings().removeObservers(this);
            new UserRepo(getApplication()).update(mySetting, this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

