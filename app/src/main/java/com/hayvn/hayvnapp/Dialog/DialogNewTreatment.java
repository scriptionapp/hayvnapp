package com.hayvn.hayvnapp.Dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.hayvn.hayvnapp.Activities.SettingActivity;
import com.hayvn.hayvnapp.Adapter.CustExpandListAdapter;
import com.hayvn.hayvnapp.Constant.TextConstants;
import com.hayvn.hayvnapp.Helper.DatePickerExt;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Helper.TreatmentForDialog;
import com.hayvn.hayvnapp.Interfaces.CallbackObjectUpdated;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.ObjectContainer;
import com.hayvn.hayvnapp.Model.User;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.CaseRepo;
import com.hayvn.hayvnapp.Utilities.DateUtils;
import com.hayvn.hayvnapp.ViewModel.UserViewModel;
import com.hayvn.hayvnapp.databinding.DialogNewTreatmentBinding;

import org.threeten.bp.format.DateTimeParseException;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DialogNewTreatment  extends DialogFragment implements CallbackObjectUpdated {
    private final static String TAG = "DIALOG_TREAT";

    Case main_case;
    String entered_treatment = "";
    String key_to_amend = "";
    Calendar calendar_start;
    Calendar calendar_end;
    Date current_date_start;
    Date current_date_end;
    User user;
    String uid;
    String user_email = "", user_name = "";
    UserViewModel mViewModel;
    Context context;
    Application app;
    boolean date_format_issue;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;
    HashMap<String, TreatmentForDialog> map_obj = new HashMap<String, TreatmentForDialog>();
    TreatmentForDialog tfd = new TreatmentForDialog();
    private DialogNewTreatmentBinding binding;

    public DialogNewTreatment(Case main_case, String entered_treatment, Application app, String key_to_amend){
        this.main_case = main_case;
        this.entered_treatment = entered_treatment;
        this.app = app;
        this.key_to_amend = key_to_amend;
    }

    public interface EditedTreatmentInDialog {
        void onTreatmentEdited(String treatment, String key);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {//onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        binding = DialogNewTreatmentBinding.inflate(LayoutInflater.from(getContext()));
        builder.setCancelable(false);
        context = getContext();

        builder.setView(binding.getRoot());

        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow())
                .setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN |
                                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getUser();
        setViews();
        setViewsValues();
        setViewsReactivity();
        return binding.getRoot();
    }

    private void getUser(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getUid();
        mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mViewModel.getSettings().observe(this, setting -> {
            user = setting;
            assert user != null;
            user_email = user.getEmail();
            user_name = user.getStringThree();
            if(user_name==null || user_name.equals("")){
                notifyAndAddName();
            }
        });
    }

    private void notifyAndAddName(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.dialog_add_name)
                .setTitle("");
        builder.setPositiveButton(R.string.okok, (dialog, id) -> {
            startSettingsActivity();
        });
        AlertDialog dialog1 = builder.create();
        dialog1.show();
    }

    private void startSettingsActivity(){
        Intent myIntent = new Intent(context, SettingActivity.class);
        startActivity(myIntent);
    }

    private void setViews(){
        calendar_start = Calendar.getInstance();
        calendar_end = Calendar.getInstance();
    }

    private void setViewsValues(){
        expandableListDetail = convertStringToMap(main_case.getString5()); //CustExpandListAdapter.getData();
        expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new CustExpandListAdapter(context, expandableListTitle, expandableListDetail);
       binding.expandableListView.setAdapter(expandableListAdapter);

        if(map_obj==null || map_obj.size()==0){binding.treatNoHistory.setVisibility(View.VISIBLE);}

        if(key_to_amend.equals("")){
            binding.elementAdd.treatDrugName.setText(entered_treatment);
        }else{
            dateConvertNotify(tfd.getDateStart(), binding.elementAdd.treatStartEdittext, "start");
            dateConvertNotify(tfd.getDateEnd(), binding.elementAdd.treatEndEdittext, "end");
            binding.elementAdd.treatDrugDose.setText(tfd.getDosageAmount());
            binding.elementAdd.treatDrugName.setText(tfd.getDrugName());
            binding.elementAdd.treatComments.setText(tfd.getComment());
        }
    }

    private void showAlert(String text){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(TextConstants.ALERT_TITLE)
                .setMessage(text);
        builder.setPositiveButton(R.string.okok, (dialog1, id) -> {
        });
        AlertDialog dialog1 = builder.create();
        dialog1.show();
    }
    private void checkCompletenessAndExit(){
        if(date_format_issue){
            showAlert(TextConstants.CORRECT_ERRORS_PLS_1);
        }else{
            String dateStart = binding.elementAdd.treatStartEdittext.getText().toString();
            String dateEnd = binding.elementAdd.treatEndEdittext.getText().toString();
            String dosageAmount = binding.elementAdd.treatDrugDose.getText().toString();
            String drugName = binding.elementAdd.treatDrugName.getText().toString();

            if(nullOrEmpty(dateStart)){
                showAlert(TextConstants.CORRECT_ERRORS_PLS_2);
            }else if(nullOrEmpty(dateEnd)){
                showAlert(TextConstants.CORRECT_ERRORS_PLS_3);
            }else if(nullOrEmpty(dosageAmount)){
                showAlert(TextConstants.CORRECT_ERRORS_PLS_5);
            }else if(nullOrEmpty(drugName)){
                showAlert(TextConstants.CORRECT_ERRORS_PLS_4);
            }else{
                saveAndFinish();
            }
        }
    }

    private void clearAndExit(){
        DialogNewTreatment.EditedTreatmentInDialog mListener = (DialogNewTreatment.EditedTreatmentInDialog) context;
        mListener.onTreatmentEdited("", "");
        dismiss();
    }

    private boolean nullOrEmpty(String s){
        return (s==null) || s.equals("");
    }
    private void saveAndFinish(){
        mViewModel.getSettings().removeObservers(this);
        CaseRepo cRepo = new CaseRepo(app);
        String new_medicat_list = assembleMedicationList();
        main_case.setString5(new_medicat_list);
        cRepo.update(main_case, true, this);
        //calls onObjectGotFireId
    }

    @Override
    public void onObjectUpdatedLocally(ObjectContainer obj) {

    }

    @Override
    public void onObjectGotFireId(ObjectContainer obj, boolean internet_was_avail) {
        DialogNewTreatment.EditedTreatmentInDialog mListener = (
                DialogNewTreatment.EditedTreatmentInDialog) context;
        mListener.onTreatmentEdited(tfd.toString(), key_to_amend);
        dismiss();
    }

    private String assembleMedicationList(){
        String dateStart = binding.elementAdd.treatStartEdittext.getText().toString();
        String dateEnd = binding.elementAdd.treatEndEdittext.getText().toString();
        String dosageAmount = binding.elementAdd.treatDrugDose.getText().toString();
        String drugName = binding.elementAdd.treatDrugName.getText().toString();
        String comment = binding.elementAdd.treatComments.getText().toString();
        String doctorUid = uid;
        String doctorScriptionId = user.getDoctorId();
        String doctorEmail = user_email;
        String doctorName = user_name;
        tfd = new TreatmentForDialog();
        tfd.setDateStart(dateStart);
        tfd.setDateEnd(dateEnd);
        tfd.setDosageAmount(dosageAmount);
        tfd.setDrugName(drugName);
        tfd.setComment(comment);
        tfd.setDoctorUid(doctorUid);
        tfd.setDoctorScriptionId(doctorScriptionId);
        tfd.setDoctorEmail(doctorEmail);
        tfd.setDoctorName(doctorName);
        tfd.setDateNow(DateUtils.dateToString(new Date()));
        updateKey();
        map_obj.put(key_to_amend, tfd);
        Gson gson = new Gson();
        return gson.toJson(map_obj);
    }

    private void updateKey(){
        if(key_to_amend.equals("")){
            String ms = String.valueOf((new Date()).getTime());
            String doc_id = user.getDoctorId();
            key_to_amend = ms + "_" + doc_id;
        }
    }

    private HashMap<String, List<String>> convertStringToMap(String json_string){
        HashMap<String, List<String>> hMap = new HashMap<String, List<String>>();
        try{
            Gson gson = new Gson();
            Type empMapType = new TypeToken<Map<String, TreatmentForDialog>>() {}.getType();
            Map<String, TreatmentForDialog> raw_map = gson.fromJson(json_string, empMapType);
            map_obj = new HashMap<String, TreatmentForDialog>(raw_map);
            for(String key: map_obj.keySet()){
                if(!key.equals(key_to_amend)){
                    String drug = Objects.requireNonNull(map_obj.get(key)).getDrugName() +
                            " from: " + Objects.requireNonNull(map_obj.get(key)).getDateStart();
                    if(hMap.containsKey(drug)){
                        List<String> curr = hMap.get(drug);
                        assert curr != null;
                        curr.addAll(Objects.requireNonNull(map_obj.get(key)).toStringList());
                        hMap.put(drug, curr);
                    }else{
                        hMap.put(drug, Objects.requireNonNull(map_obj.get(key)).toStringList());
                    }
                }else{
                    tfd = new TreatmentForDialog();
                    tfd = map_obj.get(key);
                }
            }
        }catch(Exception e) {
            Log.d(TAG, "Issue with converting the data" + e);
        }
        return hMap;
    }

    private void setViewsReactivity(){
        binding.treatSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCompletenessAndExit();
            }
        });

        binding.treatClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAndExit();
            }
        });

        binding.elementAdd.treatCalStartButton.setOnClickListener(view -> {
            hideKeyboard(view);
            launchDatePickerStart();
        });

        binding.elementAdd.treatStartEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String txt = binding.elementAdd.treatStartEdittext.getText().toString();
                dateConvertNotify(txt, binding.elementAdd.treatStartEdittext, "start");
            }
        });

        binding.elementAdd.treatCalEndButton.setOnClickListener(view -> {
            hideKeyboard(view);
            launchDatePickerEnd();
        });

        binding.elementAdd.treatEndEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String txt = binding.elementAdd.treatEndEdittext.getText().toString();
                dateConvertNotify(txt, binding.elementAdd.treatEndEdittext, "end");
            }
        });
    }

    private void launchDatePickerStart(){
        binding.elementAdd.treatStartPicker.setVisibility(View.VISIBLE);
        binding.elementAdd.treatStartPicker.init(
        calendar_start.get(Calendar.YEAR),
        calendar_start.get(Calendar.MONTH),
        calendar_start.get(Calendar.DAY_OF_MONTH),
        (datePicker, year, month, dayOfMonth) -> {
            int previous_month = calendar_start.get(Calendar.MONTH);
            int previous_day = calendar_start.get(Calendar.DAY_OF_MONTH);
            int previous_yr = calendar_start.get(Calendar.YEAR);
            if ((month != previous_month) || (dayOfMonth != previous_day) || (year != previous_yr)) {
                datePicker.setVisibility(View.GONE);
            }
            calendar_start.set(year, month, dayOfMonth);
            current_date_start = calendar_start.getTime(); //????
            binding.elementAdd.treatStartEdittext.setText(DateUtils.dateToString(current_date_start));
        });
    }
    private void launchDatePickerEnd(){
        binding.elementAdd.treatEndPicker.setVisibility(View.VISIBLE);
        binding.elementAdd.treatEndPicker.init(
            calendar_end.get(Calendar.YEAR),
            calendar_end.get(Calendar.MONTH),
            calendar_end.get(Calendar.DAY_OF_MONTH),
            (datePicker, year, month, dayOfMonth) -> {
                int previous_month = calendar_end.get(Calendar.MONTH);
                int previous_day = calendar_end.get(Calendar.DAY_OF_MONTH);
                int previous_yr = calendar_end.get(Calendar.YEAR);
                if ((month != previous_month) || (dayOfMonth != previous_day) || (year != previous_yr)) {
                    datePicker.setVisibility(View.GONE);
                }
                calendar_end.set(year, month, dayOfMonth);
                current_date_end = calendar_start.getTime(); //????
                binding.elementAdd.treatEndEdittext.setText(DateUtils.dateToString(current_date_start));
            });
    }

    private void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private void dateConvertNotify(String raw, EditText et, String which_cal){
        try {
            Date latest_date = DateUtils.stringToDate(raw);
            if(which_cal.equals("start")){
                calendar_start.setTime(latest_date);
            }else{
                calendar_end.setTime(latest_date);
            }
            et.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            et.setTextColor(Color.BLACK);
            date_format_issue = false;
        } catch (DateTimeParseException ex) {
            date_format_issue = true;
            et.getBackground().setColorFilter(Color.argb(100,255,50,50),
                    PorterDuff.Mode.SRC_ATOP);
        } catch(Exception e){
            Log.d(TAG, e.toString());
        }
    }

}
