package com.hayvn.hayvnapp.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hayvn.hayvnapp.Adapter.CustExpandListAdapter;
import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.IntentConstants;
import com.hayvn.hayvnapp.Constant.RoomConstants;
import com.hayvn.hayvnapp.Constant.TextConstants;
import com.hayvn.hayvnapp.Dialog.DialogLocation;
import com.hayvn.hayvnapp.Fragment.FragmentPatientPhoto;
import com.hayvn.hayvnapp.Helper.TreatmentForDialog;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.CaseRepo;
import com.hayvn.hayvnapp.Utilities.DateUtils;
import com.hayvn.hayvnapp.Utilities.Utilities;
import com.hayvn.hayvnapp.databinding.DrawerSummaryBinding;

import org.threeten.bp.format.DateTimeParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.hayvn.hayvnapp.Constant.Constant.REQUEST_IMAGE_CAPTURE;
import static com.hayvn.hayvnapp.Constant.Constant.REQUEST_WRITE_STORAGE_PERMISSION;
import static com.hayvn.hayvnapp.Constant.IntentConstants.NEW_SUMMARY_INSTANCE;
import static com.hayvn.hayvnapp.Constant.TextConstants.EXITING;
import static com.hayvn.hayvnapp.Constant.TextConstants.EXIT_WITHOUT_SAVING;
import static com.hayvn.hayvnapp.Constant.TextConstants.INCONSISTENT_ALLERGIES;
import static com.hayvn.hayvnapp.Constant.TextConstants.LOCATION_PLACEHOLDER;
import static com.hayvn.hayvnapp.Constant.TextConstants.SAVE_CHANGES;
import static com.hayvn.hayvnapp.Constant.TextConstants.SPECIFY_ALLERGIES;

public class SummaryActivity extends BaseParentActivity
        implements DialogLocation.EditedLocation {
    private static final String TAG = "SETTINGS";
    ActionBar toolbar;
    Case case_;
    String this_summary;
    String this_name;
    String this_dob = "";
    String this_hospital_id;
    String this_phone;
    String this_allergies_status = "";
    CaseRepo mCaseRepo;
    FragmentPatientPhoto fragment;
    boolean has_edited = false;
    boolean is_checked = false;
    Date latestDate;
    Calendar calendar;
    boolean date_format_issue;
    Context context;

    private DrawerSummaryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DrawerSummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();

        toolbar = getSupportActionBar();
        case_ = (Case) Objects.requireNonNull(intent.getExtras()).getSerializable("MODIFY");

        this_name = case_.getName();
        this_hospital_id = case_.getString4();
        this_summary = case_.getSummary();
        this_phone = case_.getString6();
        if(this_phone==null ){this_phone="";}
        if(this_summary==null || this_summary.equals("")){this_summary=";";}
        this_dob = case_.getDateofbirth();
        this_allergies_status = case_.getStringOne();

        calendar = Calendar.getInstance();

        binding.edtCaseName.setText(this_name);
        binding.hospitalPatientId.setText(this_hospital_id);
        binding.edtPhoneNumber.setText(this_phone);
        toolbar.setTitle(this_name);
        binding.edtDob.setText(this_dob);
        binding.patientId.setText(case_.getPatientId());
        context = this;

        setSummaryUI();
        setLocationUI();
        selectFragment(null);

        if(this_allergies_status!=null) is_checked = this_allergies_status.equals(RoomConstants.NO_KNOWN_ALLERGIES);
        binding.edtAllergies.setVisibility(is_checked ? View.GONE : View.VISIBLE);
        if(is_checked){
            binding.noAllergiesChbox.setChecked(true);
        }else{
            binding.edtAllergies.setText(parseAllergyStatus(this_allergies_status));
        }

        binding.noAllergiesChbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                is_checked = isChecked;
                binding.edtAllergies.setVisibility(is_checked ? View.GONE : View.VISIBLE);
                if(is_checked){
                    //
                }else{
                    binding.edtAllergies.setText(parseAllergyStatus(this_allergies_status));
                }
            }
        });

        binding.edtAllergies.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                this_allergies_status = binding.edtAllergies.getText().toString();
            }
        });

        binding.summaryLogLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogLocation dial_loc = new DialogLocation(case_.getStringTwo(), false);
                dial_loc.show(getSupportFragmentManager(), "LocationDialog");
            }
       });

        mCaseRepo = new CaseRepo(getApplication());

        latestDate = new Date();
        if (this_dob != null) {
            try {
                latestDate = DateUtils.stringToDate(this_dob);
                calendar.setTime(latestDate);
                binding.edtDob.setText(this_dob);
                binding.edtDob.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                date_format_issue = false;
            }catch(Exception e){
                binding.edtDob.setText(this_dob);
                binding.edtDob.getBackground().setColorFilter(Color.argb(100,255,50,50), PorterDuff.Mode.SRC_ATOP);
                date_format_issue = true;
            }
        }else{
            latestDate = new Date();
            calendar.setTime(latestDate);
            binding.edtDob.setText(DateUtils.dateToString(latestDate));
            binding.edtDob.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            date_format_issue = false;
        }

        binding.edtDobPicker.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String txt =  binding.edtDob.getText().toString();
                try {
                    date_format_issue = false;
                    latestDate = DateUtils.stringToDate(txt);
                    calendar.setTime(latestDate);
                    binding.edtDob.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    binding.edtDob.setTextColor(Color.BLACK);
                } catch (DateTimeParseException ex) {
                    date_format_issue = true;
                    binding.edtDob.getBackground().setColorFilter(Color.argb(100,255,50,50), PorterDuff.Mode.SRC_ATOP);
                    Toast tst = Toast.makeText(this, getString(R.string.format_date), Toast.LENGTH_SHORT);
                    tst.show();
                }catch(Exception e){
                    Log.d(TAG, e.toString());
                }
            }

        });

        binding.edtDob.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String txt =  binding.edtDob.getText().toString();
                try {
                    date_format_issue = false;
                    latestDate = DateUtils.stringToDate(txt);
                    has_edited = true;
                    calendar.setTime(latestDate);
                    binding.edtDob.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    binding.edtDob.setTextColor(Color.BLACK);
                } catch (DateTimeParseException ex) {
                    date_format_issue = true;
                    binding.edtDob.getBackground().setColorFilter(Color.argb(100,255,50,50), PorterDuff.Mode.SRC_ATOP);
                } catch(Exception e){
                    Log.d(TAG, e.toString());
                }
            }
        });

        //This is the method that is triggered when someone selects a date on the calendar
        binding.buttonCalDob.setOnClickListener(view -> {
            binding.edtDobPicker.setVisibility(View.VISIBLE);
            hideKeyboard(view);
            binding.edtDobPicker.init(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    (datePicker, year, month, dayOfMonth) -> {
                        int previous_month = calendar.get(Calendar.MONTH);
                        int previous_day = calendar.get(Calendar.DAY_OF_MONTH);
                        int previous_yr = calendar.get(Calendar.YEAR);
                        if (((month != previous_month) || (dayOfMonth != previous_day)) & (year == previous_yr)) {
                            datePicker.setVisibility(View.GONE);
                        }
                        calendar.set(year, month, dayOfMonth);
                        latestDate = calendar.getTime(); //????
                        binding.edtDob.setText(DateUtils.dateToString(latestDate));
                    });
        });
    }

    public void selectFragment(Bundle bundle) {
        fragment = new FragmentPatientPhoto();
        Bundle args = new Bundle();
        args.putInt(IntentConstants.CASE_ID_FOR_FRAG, case_.getCid());
        args.putString(IntentConstants.CASE_FBID_FOR_FRAG, case_.getFbId());
        fragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_patient_photo, fragment);
        fragmentTransaction.commit();
    }


    private void setSummaryUI(){
        String summary = case_.getSummary();
        if(summary != null && !summary.equals("")){
            ArrayList<String> summ_parts = new ArrayList<String>();
            summ_parts = new ArrayList<String>( Arrays.asList(summary.split(";", -1)) );
            if(summ_parts.size() > 1){
                binding.edtSummaryMedHist.setText(summ_parts.get(0));
                binding.edtSummaryDrugHist.setText(summ_parts.get(1));
            }else if(summ_parts.size() == 1){
                binding.edtSummaryMedHist.setText(summ_parts.get(0));
            }
        }

        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();
        try{
            Gson gson = new Gson();
            Type empMapType = new TypeToken<Map<String, TreatmentForDialog>>() {}.getType();
            Map<String, TreatmentForDialog> raw_map = gson.fromJson(emptyOrNull(case_.getString5()), empMapType);
            if(raw_map==null || raw_map.size()==0 || raw_map.keySet().size()==0){
                binding.summaryTreatNoHistory.setVisibility(View.VISIBLE);
            }else{
                for(String key: raw_map.keySet()){
                    String drug = raw_map.get(key).getDrugName() + " to: " + raw_map.get(key).getDateEnd();
                    if(expandableListDetail.keySet().contains(drug)){
                        List<String> curr = expandableListDetail.get(drug);
                        curr.addAll(raw_map.get(key).toStringList());
                        expandableListDetail.put(drug, curr);
                    }else{
                        expandableListDetail.put(drug, raw_map.get(key).toStringList());
                    }
                }
                List sortedKeys=new ArrayList(expandableListDetail.keySet());
                Collections.sort(sortedKeys);
                CustExpandListAdapter expandableListAdapter = new CustExpandListAdapter(context,
                        new ArrayList<String>(sortedKeys), expandableListDetail);
                binding.summaryExpandable.setAdapter(expandableListAdapter);
            }
        }catch(Exception e) {
            Log.d(TAG, "Issue with converting the data" + e.toString());
        }

    }

    private String parseAllergyStatus(String allerg){
        if(allerg != null && allerg.equals(RoomConstants.NO_KNOWN_ALLERGIES)){
            return("");
        }else if(allerg==null){
            return "";
        }else{return allerg;}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    private void showWarningDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(getString(R.string.okok), (dialog, id2) -> {})
                .setMessage(message)
                .setTitle(getString(R.string.warning));
        builder.setCancelable(true);
        AlertDialog dialog1 = builder.create();
        dialog1.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if ( id == R.id.action_complete_new_log) {
            if(checkAllergies().equals("")){
                updateCaseWithNewValsAndSave();
                returnIntentAndFinish();
                return true;
            }else{
                showWarningDialog(checkAllergies());
                return false;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private String emptyOrNull(String s){
        if(s==null){return "";}
        return s;
    }

    private String summaryString(){
        String s1 = emptyOrNull(binding.edtSummaryMedHist.getText().toString());
        String s2 = emptyOrNull(binding.edtSummaryDrugHist.getText().toString());
        s1 = s1.replace(";", ",");
        s2 = s2.replace(";", ",");
        return s1+";" +s2;
    }

    private void updateCaseWithNewValsAndSave(){
        case_.setSummary(summaryString());
        case_.setName(binding.edtCaseName.getText().toString());
        case_.setString4(binding.hospitalPatientId.getText().toString());
        case_.setDateofbirth( binding.edtDob.getText().toString());
        case_.setStringTwo(binding.summaryLogLocation.getText().toString());
        case_.setString6(binding.edtPhoneNumber.getText().toString());
        String allerg = binding.edtAllergies.getText().toString();
        if(allerg.equals("") || binding.noAllergiesChbox.isChecked()){
            allerg = RoomConstants.NO_KNOWN_ALLERGIES;
        }
        case_.setStringOne(allerg);
        case_ = mCaseRepo.setUpdateCreate(case_, true, true);
        mCaseRepo.update(case_);
    }

    private String checkAllergies(){
        String txt_error = "";
        String allergies_text = Utilities.getInstance().getString(binding.edtAllergies);
        if(!is_checked && allergies_text.equals("")){
            txt_error = SPECIFY_ALLERGIES;
        }else if(is_checked && !allergies_text.equals("")){
            txt_error = INCONSISTENT_ALLERGIES;
        }
        return txt_error;
    }

    private void returnIntentAndFinish(){
        checkCaseCompletenessAndExit();
    }

    private void checkCaseCompletenessAndExit(){
        if(
                nullAndLen(case_.getName(), 3) &&
                nullAndLen(case_.getStringTwo(), 6) && //address
                nullAndLen(case_.getDateofbirth(), 6)
        ){
            Intent returnIntent = new Intent();
            returnIntent.putExtra(NEW_SUMMARY_INSTANCE, (Case) case_);
            setResult(Activity.RESULT_OK, returnIntent);
            fragment.closeIt();
            finish();
        }else{
            ArrayList<String>al = new ArrayList<>();
            String txt = "";
            if( !nullAndLen(case_.getName(), 3)){al.add(getString(R.string.name));}
            if( !nullAndLen(case_.getStringTwo(), 6)){al.add(getString(R.string.overarching_address));}
            if( !nullAndLen(case_.getDateofbirth(), 6)){al.add(getString(R.string.date_dob));}
            if(al.size()>0) txt = al.get(0);
            if(al.size()>1) {
                for(String s: al.subList(1, al.size())){
                    txt = txt + ", " + s;
                }
            }

            Toast.makeText(
                    this,
                    TextConstants.PLEASE_ADD+": " + txt,
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkEdited(){
        return ((   !emptyIfNull(this_summary).equals(summaryString())) ||
                (   !emptyIfNull(this_name).equals(binding.edtCaseName.getText().toString())) ||
                (   !emptyIfNull(this_phone).equals(binding.edtPhoneNumber.getText().toString())) ||
                (   !emptyIfNull(this_hospital_id).equals(binding.hospitalPatientId.getText().toString())) ||
                (   !emptyIfNull(this_dob).equals( binding.edtDob.getText().toString()))||
                (   !emptyIfNull(  parseAllergyStatus(this_allergies_status)).equals(binding.edtAllergies.getText().toString())) );
    }

    @Override
    public void onBackPressed() {
        if(checkAllergies().equals("")){
            handleBackPressOK();
        }else{
            showWarningDialog(checkAllergies());
        }
    }

    private String emptyIfNull(String s){
        if(s==null) return "";
        else return s;
    }

    private void handleBackPressOK(){
        has_edited = checkEdited();
        if (has_edited) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(SAVE_CHANGES)
                    .setTitle(EXITING);

            builder.setPositiveButton(getString(R.string.oksave), (dialog, id) -> {
                updateCaseWithNewValsAndSave();
                returnIntentAndFinish();
            });

            builder.setNegativeButton(EXIT_WITHOUT_SAVING, (dialog, id) -> {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(NEW_SUMMARY_INSTANCE, case_);
                setResult(Activity.RESULT_CANCELED, returnIntent);
                fragment.closeIt();
                finish();
            });
            builder.setCancelable(true);
            AlertDialog dialog = builder.create();
            dialog.show();
        }else{
            fragment.closeIt();
            finish();
        }
    }

    private boolean nullAndLen(String s, int len){
        return s != null && s.length() > len;
    }

    private String replaceSemicols(String s){
        return s.replaceAll("((;))\\2+","$1");
    }
    private void setLocationUI(){
        if(nullAndLen(case_.getStringTwo(), 6)){
            String raw_string = case_.getStringTwo();
            String edited_string = replaceSemicols(raw_string);
            edited_string = edited_string.replace(";", "\n");
            binding.summaryLogLocation.setText(edited_string);
        }else{
            binding.summaryLogLocation.setText(LOCATION_PLACEHOLDER);
        }
    }

    @Override
    public void onLocationEdited(String location) {
        case_.setStringTwo(location);
        setLocationUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(
                            this,
                            TextConstants.PERMISSION_DENIED,
                            Toast.LENGTH_LONG).show();
                }

                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            fragment.updateAfAndInsertFile(Constant.IMG_FILE_TYPE);
        }
    }
}

