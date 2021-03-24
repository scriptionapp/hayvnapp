package com.hayvn.hayvnapp.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hayvn.hayvnapp.Adapter.FileAdapter;
import com.hayvn.hayvnapp.Constant.IntentConstants;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.Constant.TextConstants;
import com.hayvn.hayvnapp.Dialog.DialogLocation;
import com.hayvn.hayvnapp.Dialog.DialogNewTreatment;
import com.hayvn.hayvnapp.Dialog.DialogView;
import com.hayvn.hayvnapp.Helper.DatePickerExt;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Interfaces.CallbackObjectUpdated;
import com.hayvn.hayvnapp.Model.Attachedfile;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.ObjectContainer;
import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.ProdDev.Modes;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.FileRepo;
import com.hayvn.hayvnapp.Utilities.DateUtils;
import com.hayvn.hayvnapp.Utilities.LocalFileWriter;
import com.hayvn.hayvnapp.ViewModel.FileViewModel;
import com.hayvn.hayvnapp.ViewModel.StoryViewModel;
import com.hayvn.hayvnapp.Constant.Constant;
import com.google.firebase.auth.FirebaseAuth;
import com.hayvn.hayvnapp.databinding.ActivityNewstoryBinding;

import org.threeten.bp.format.DateTimeParseException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static com.hayvn.hayvnapp.Constant.Constant.REQUEST_CODE_PICK_FILE;
import static com.hayvn.hayvnapp.Constant.Constant.REQUEST_IMAGE_CAPTURE;
import static com.hayvn.hayvnapp.Constant.Constant.REQUEST_RECORD_AUDIO_PERMISSION;
import static com.hayvn.hayvnapp.Constant.Constant.REQUEST_WRITE_STORAGE_PERMISSION;
import static com.hayvn.hayvnapp.Constant.IntentConstants.CLICK_WHAT_FROMDEVICE;
import static com.hayvn.hayvnapp.Constant.IntentConstants.CLICK_WHAT_QUICKPHOTO;
import static com.hayvn.hayvnapp.Constant.IntentConstants.CLICK_WHAT_RECORDING;
import static com.hayvn.hayvnapp.Constant.TextConstants.EXIT_WITHOUT_SAVING;
import static com.hayvn.hayvnapp.Constant.TextConstants.MEDICATION_MESSAGE;
import static com.hayvn.hayvnapp.Constant.TextConstants.NO_CANCEL;
import static com.hayvn.hayvnapp.Constant.TextConstants.SAVE_CHANGES;
import static com.hayvn.hayvnapp.Constant.TextConstants.YES_INFO_OK;


public class NewStoryActivity extends BaseParentActivity implements
        DialogView.ChooseWhatDialogListener,
        DialogView.EndedRecording,
        DialogLocation.EditedLocation,
        DialogNewTreatment.EditedTreatmentInDialog,
        CallbackObjectUpdated {

    private final String TAG = "NEW_STORY_ACTIVITY";
    private final String GENERATED_FILE = "own_file";
    private final String EXTERNAL_FILE = "external_file";
    private final String LOCATION_PLACEHOLDER = "Set location";

    ArrayList<String> list_of_titles = new ArrayList<String>();
    boolean editability_condition = true;
    boolean prog_change = false;
    boolean has_edited = false;
    int attached_files_counter = 0;
    boolean date_format_issue = false;
    int times_progress_bar = 0;
    String mCurrentClicked;
    Story story_;
    Case parent_case;
    FirebaseAuth mAuth;
    LocalFileWriter ufw;
    CallbackObjectUpdated callback_updated;
    FileRepo fileRepo;
    Date latestDate;
    Calendar calendar;
    String mCurrentFilePath;
    String mCurrentFileName;
    String last_filetype_request = "";
    String original_treatment_plan;
    String solid_treatment_plan = "";
    Context context;
    FileAdapter adapter;
    FileViewModel mFileViewModel;
    StoryViewModel sViewModel;
    ArrayList<Attachedfile> attFilesSavedInRoom = new ArrayList<Attachedfile>();
    ArrayList<Attachedfile> attFilesSavedInRoomAndFirebase = new ArrayList<Attachedfile>();
    ArrayList<FileRepo.insertUpdateAsyncTask> insertFileTasks = new ArrayList<FileRepo.insertUpdateAsyncTask>();
    ArrayList<String> insertFileTaskNames = new ArrayList<String>();
    boolean toContinueDeleting = false;
    boolean medication_was_edited = false;
    boolean already_launched_dial = false;
    String medication_key_to_edit = "";
    private ActivityNewstoryBinding binding;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        binding = ActivityNewstoryBinding.inflate(getLayoutInflater());
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setLogo(R.drawable.ic_logo_white);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
        }

        setContentView(binding.getRoot());
        Intent intent = getIntent();
        story_ = (Story) Objects.requireNonNull(intent.getExtras()).getSerializable(IntentConstants.STORY_TO_MODIFY);
        parent_case = (Case)Objects.requireNonNull(intent.getExtras()).getSerializable(IntentConstants.PARENT_CASE);
        ufw = new LocalFileWriter(this);
        fileRepo = new FileRepo(getApplication());
        callback_updated = this;
        mAuth = FirebaseAuth.getInstance();
        checkAndSetEditability();
        latestDate = new Date();
        adapter = new FileAdapter(context, this, fileRepo, editability_condition);
        binding.rvFiles.setLayoutManager(new LinearLayoutManager(context));
        binding.rvFiles.setAdapter(adapter);
        calendar = Calendar.getInstance();
        mFileViewModel = ViewModelProviders.of(this).get(FileViewModel.class);
        sViewModel = ViewModelProviders.of(this).get(StoryViewModel.class);
        mCurrentClicked = "";
        if(Modes.getMedical().equals("medical")){
            binding.storyScoreView.setVisibility(View.GONE);
            binding.newlogWitnesses.setVisibility(View.GONE);
        }
        assert story_ != null;
        sViewModel.getNewStory().setValue(story_);
        original_treatment_plan = story_.getStringOne();
        update_progress_bar(false);
        handleTitle();
        handleMedication();
        if (story_.getLog() != null) {
            binding.newlogLog.setText(story_.getLog());
        }
        setLocationUI();
        binding.newlogWitnesses.setText(story_.getWitnesses());
        if(original_treatment_plan==null) original_treatment_plan="";
        binding.treatmentPlan.setText(original_treatment_plan);
        String occurredAtData = story_.getOccurredAt();
        if (occurredAtData != null) {
            try {
                latestDate = DateUtils.stringToDate(occurredAtData);
                calendar.setTime(latestDate);
                binding.newlogEventdateEt.setText(occurredAtData);
                binding.newlogEventdateEt.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                date_format_issue = false;
            }catch(Exception e){
                binding.newlogEventdateEt.setText(occurredAtData);
                binding.newlogEventdateEt.getBackground().setColorFilter(Color.argb(100,255,50,50), PorterDuff.Mode.SRC_ATOP);
                date_format_issue = true;
            }
        }else{
            latestDate = new Date();
            calendar.setTime(latestDate);
            sViewModel.getStory().getValue().setOccurredAt(DateUtils.dateToString(latestDate));
            binding.newlogEventdateEt.setText(DateUtils.dateToString(latestDate));
            binding.newlogEventdateEt.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            date_format_issue = false;
        }
        launchFilesObserver(story_.getSid());

        binding.newlogTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    ((AutoCompleteTextView)view).showDropDown();
                } else {
                    //Toast.makeText(getApplicationContext(), "Lost the focus", Toast.LENGTH_LONG).show();
                }
            }
        });
        binding.newlogLog.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!prog_change) {
                    Spannable str = binding.newlogLog.getText();
                    sViewModel.getStory().getValue().setLog(str.toString());
                    update_progress_bar(true);
                } else {
                    prog_change = false;
                }
            }
        });

        binding.treatmentPlan.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Spannable str = binding.treatmentPlan.getText();
                sViewModel.getStory().getValue().setStringOne(str.toString());
                update_progress_bar(true);

                if(!already_launched_dial && binding.treatmentPlan.getText().toString().length() > 1){
                    DialogNewTreatment dial_treat = new DialogNewTreatment(parent_case,
                            binding.treatmentPlan.getText().toString(), getApplication(), medication_key_to_edit);
                    dial_treat.show(getSupportFragmentManager(), "TREATMENT_DIALOG");
                    already_launched_dial = true;
                }
            }
        });

        //***************
        // Witnesses
        //***************
        binding.newlogWitnesses.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String txt = binding.newlogWitnesses.getText().toString();
                sViewModel.getStory().getValue().setWitnesses(txt);
                update_progress_bar(true);
            }
        });

        //***************
        // Location
        //***************
        binding.newlogLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogLocation dial_loc = new DialogLocation(sViewModel.getStory().getValue().getLocation());
                dial_loc.show(getSupportFragmentManager(), "LocationDialog");
            }
        });

        //***************
        // Date
        //***************
        binding.newlogEventdateEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String txt = binding.newlogEventdateEt.getText().toString();
                Log.d("NEW_STORY", String.format("newEventDate: %s", txt));
                dateConvertNotify(txt);
            }

        });

        binding.newlogEventdateEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                has_edited = true;
                String txt = binding.newlogEventdateEt.getText().toString();
                Log.d("NEW_STORY", String.format("newEventDate: %s", txt));
                dateConvertNotify(txt);
            }
        });

        //This is the method that is triggered when someone selects a date on the calendar
       binding.buttonCal.setOnClickListener(view -> {
            binding.newlogEventdate.setVisibility(View.VISIBLE);
            hideKeyboard(view);
           binding.newlogEventdate.init(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    (datePicker, year, month, dayOfMonth) -> {
                        int previous_month = calendar.get(Calendar.MONTH);
                        int previous_day = calendar.get(Calendar.DAY_OF_MONTH);
                        int previous_yr = calendar.get(Calendar.YEAR);
                        if ((month != previous_month) || (dayOfMonth != previous_day) || (year == previous_yr)) {
                            datePicker.setVisibility(View.GONE);
                        }
                        calendar.set(year, month, dayOfMonth);
                        latestDate = calendar.getTime(); //????
                        binding.newlogEventdateEt.setText(DateUtils.dateToString(latestDate));
                    });
        });

        //***************
        // Attached Files
        //***************
        binding.attachLogFile.setOnClickListener(view -> {
            DialogView.getInstance().showChooseWhatAttachment(context);
        });
    }


    private void checkAndSetEditability(){
        editability_condition =
                story_.getUserId() != null &&
                mAuth.getUid().equals(story_.getUserId()) &&
                story_.getCreatedAt() != 0 &&
                ((new Date()).getTime() - story_.getCreatedAt() < 7*24*60*60*1000);
        if(!editability_condition){
            binding.attachLogFile.setKeyListener(null);
            binding.newlogTitle.setKeyListener(null);
            binding.newlogEventdateEt.setKeyListener(null);
            binding.newlogWitnesses.setKeyListener(null);
            binding.newlogLocation.setKeyListener(null);
            binding.treatmentPlan.setKeyListener(null);
            binding.newlogLog.setKeyListener(null);
            binding.buttonCal.setEnabled(false);
            Toast.makeText(context, TextConstants.ENTRY_LOCKED,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void dateConvertNotify(String raw){
        try {
            date_format_issue = false;
            latestDate = DateUtils.stringToDate(raw);
            calendar.setTime(latestDate);
            binding.newlogEventdateEt.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            binding.newlogEventdateEt.setTextColor(Color.BLACK);
            sViewModel.getStory().getValue().setOccurredAt(raw);
            update_progress_bar(true);
        } catch (DateTimeParseException ex) {
            date_format_issue = true;
            binding.newlogEventdateEt.getBackground().setColorFilter(Color.argb(100,255,50,50), PorterDuff.Mode.SRC_ATOP);
            Toast tst = Toast.makeText(context, getString(R.string.format_date),
                    Toast.LENGTH_SHORT);
            tst.show();
            Log.d(TAG, ex.toString());
        } catch(Exception e){
            Log.d(TAG, e.toString());
        }
    }


    private void handleTitle(){
        list_of_titles = SharedPrefHelper.getStringList(context, SharedprefConstants.ENTRYTITLE_LIST_FULL);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, list_of_titles);

        if(sViewModel.getStory().getValue().getTitle() != null){
            binding.newlogTitle.setText(sViewModel.getStory().getValue().getTitle());
        }

        binding.newlogTitle.setAdapter(adapter);
        binding.newlogTitle.setThreshold(1); // if not enough set Integer.MAX_VALUE

        binding.newlogTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                CharSequence constraint = binding.newlogTitle.getText();
                adapter.getFilter().filter(constraint);
                binding.newlogTitle.showDropDown();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String txt = binding.newlogTitle.getText().toString();
                sViewModel.getStory().getValue().setTitle(txt);
                update_progress_bar(false);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    private boolean nullAndLen(String s, int len){
        return s != null && s.length() > len;
    }

    private void checkStoryCompletenessAndExit(){
        if(editability_condition){
            finishIt();
        }else {
            if (
                    nullAndLen(story_.getOccurredAt(), 6) &&
                            nullAndLen(story_.getTitle(), 1) &&
                            nullAndLen(story_.getLocation(), 6) &&
                            nullAndLen(story_.getLog(), 6)
            ) {
                if (medication_was_edited) warnAboutMedication();
                else finishIt();
            } else {
                String txt = "";
                ArrayList<String> al = new ArrayList<>();
                if (!nullAndLen(story_.getOccurredAt(), 6)) {
                    al.add("date");
                }
                if (!nullAndLen(story_.getTitle(), 1)) {
                    al.add("title");
                }
                if (!nullAndLen(story_.getLocation(), 6)) {
                    al.add("location");
                }
                if (!nullAndLen(story_.getLog(), 6)) {
                    al.add("comments");
                }

                if (al.size() > 0) txt = al.get(0);
                if (al.size() > 1) {
                    for (String s : al.subList(1, al.size())) {
                        txt = txt + ", " + s;
                    }
                }
                Toast.makeText(
                        this,
                        TextConstants.PLEASE_ADD + ": " + txt,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void warnAboutMedication(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(MEDICATION_MESSAGE)
                .setTitle(solid_treatment_plan);

        builder.setPositiveButton(YES_INFO_OK, (dialog, id) -> {
            finishIt();
        });

        builder.setNegativeButton(NO_CANCEL, (dialog, id) -> {

        });
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void finishIt(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra(IntentConstants.NEW_STORY_INSTANCE, story_);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void handleMedication(){
        if(original_treatment_plan != null && original_treatment_plan.length() > 4){
            String temp = original_treatment_plan.replace("\n", "<br/>");
            binding.treatmentPlan.setText(temp);
            //treatment_plan.setInputType(InputType.TYPE_NULL);
            binding.treatmentPlan.setLines(4);
            binding.treatmentPlan.setKeyListener(null);
            binding.treatmentPlan.setBackground(null);
            binding.treatmentPlan.setBackgroundColor(getResources().getColor(R.color.disabled_bg));
            binding.treatmentPlan.setTextColor(getResources().getColor(R.color.disabled_font));
        }
        if(medication_key_to_edit != null && !medication_key_to_edit.equals("")){
            binding.treatmentPlan.setText(solid_treatment_plan);
            binding.treatmentPlan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogNewTreatment dial_treat = new DialogNewTreatment(parent_case,
                            solid_treatment_plan, getApplication(), medication_key_to_edit);
                    dial_treat.show(getSupportFragmentManager(), "TREATMENT_DIALOG");
                }
            });
        }else if(medication_key_to_edit != null && medication_key_to_edit.equals("")){
            binding.treatmentPlan.setText(solid_treatment_plan);
        }
    }

    private boolean checkEditedMedicalPlan(){
        //check if any information has been added
        String new_treatment_plan = binding.treatmentPlan.getText().toString();
        return !original_treatment_plan.equals(new_treatment_plan) &&
                new_treatment_plan.length() > 1;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_complete_new_log) {
            story_ = sViewModel.getStory().getValue();
            story_.setStatusEntry(String.valueOf(binding.storyScoreBar.getProgress()));
            checkStoryCompletenessAndExit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (editability_condition && has_edited) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(SAVE_CHANGES)
                    .setTitle("...");

            builder.setPositiveButton(getString(R.string.savesave), (dialog, id) -> {
                story_ = sViewModel.getStory().getValue();
                assert story_ != null;
                story_.setStatusEntry(String.valueOf(binding.storyScoreBar.getProgress()));
                checkStoryCompletenessAndExit();
            });

            builder.setNegativeButton(EXIT_WITHOUT_SAVING, (dialog, id) -> {
                toContinueDeleting = true; // in case some others get synced into firestore later, we will delete them too
                for(FileRepo.insertUpdateAsyncTask tsk: insertFileTasks){//cancel all tasks that are running:
                    Log.d(TAG,"Canceling running Firebase upload tasks...");
                    tsk.cancel(true);
                }
                fileRepo.deleteListOfFiles(attFilesSavedInRoom.toArray(new Attachedfile[0])); // delete all files locally
                attFilesSavedInRoom.clear();
                fileRepo.deleteFilesFromFirestore(fileRepo.mFirestore, attFilesSavedInRoomAndFirebase.toArray(new Attachedfile[attFilesSavedInRoomAndFirebase.size()])); // the ones that got synched get deleted remotely; it is a subset of attFilesSavedInRoom
                Intent returnIntent = new Intent();
                returnIntent.putExtra(IntentConstants.NEW_STORY_INSTANCE, story_);
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            });
            builder.setCancelable(true);
            AlertDialog dialog = builder.create();
            dialog.show();
        }else{
            story_.setStatusEntry(String.valueOf(binding.storyScoreBar.getProgress()));
            Intent returnIntent = new Intent();
            returnIntent.putExtra(IntentConstants.NEW_STORY_INSTANCE, story_);
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        }
    }

    /*Attached files*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { //TODO: this is the final bridge
            updateAfAndInsertFile(Constant.IMG_FILE_TYPE);
        } else if (requestCode == REQUEST_CODE_PICK_FILE) {
            if (resultCode == RESULT_OK) {
                Uri newFileUri = ufw.getChosenFileURI(data);
                mCurrentFilePath = ufw.uriToString(newFileUri); //newFileUri.toString();
                mCurrentFileName = ufw.getFileNameFromUri(newFileUri);
                String type = ufw.setType(this, newFileUri);
                updateAfAndInsertFile(type);
                Toast.makeText(
                        this,
                        TextConstants.RECEIVED_FILE + ":\n" + mCurrentFilePath,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(
                    this,
                    TextConstants.RECEIVED_NONE,
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void updateRecordingResults(String fileName, String uriPath) {
        if(fileName != null) {
            Log.d(TAG,"In updating recording");
            mCurrentFileName = fileName;
            mCurrentFilePath = uriPath;
            updateAfAndInsertFile(Constant.AUDIO_FILE_TYPE);
        }
    }

    public void updateAfAndInsertFile(String filetype) {
        Attachedfile attachedFile = ufw.createFileByType(mCurrentFilePath, mCurrentFileName, sViewModel.getStory().getValue(), parent_case, filetype);
        FileRepo.insertUpdateAsyncTask task = fileRepo.insertWithProgressTask(
                callback_updated,
                true,
                getToCompressImg());
        insertFileTasks.add(task);
        insertFileTaskNames.add(mCurrentFileName);
        task.execute(attachedFile);
        has_edited = true;
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE_PERMISSION:
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(!mCurrentClicked.equals("")) {
                        clickedOnWhat(mCurrentClicked);
                    }
                    if (adapter.permissionChecked) {
                        adapter.checkAllFilesAgainAfterPermission();
                    }
                } else {
                    Toast.makeText(
                            this,
                            TextConstants.PERMISSION_DENIED,
                            Toast.LENGTH_LONG).show();
                }

                break;
            case REQUEST_RECORD_AUDIO_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(!mCurrentClicked.equals("")) {
                        clickedOnWhat(mCurrentClicked);
                    }
                } else {
                    Toast.makeText(
                            this,
                            TextConstants.PERMISSION_DENIED,
                            Toast.LENGTH_LONG).show();
                }
        }
    }

    private Uri createImageUri() {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mCurrentFileName = "JPEG_" + timeStamp;
        Uri imageUri =  ufw.createImageUri(mCurrentFileName);
        mCurrentFilePath = ufw.uriToString(imageUri); //imageUri.toString();
        return imageUri;
    }

    private boolean getToCompressImg(){
        return last_filetype_request.equals(GENERATED_FILE);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void clickedOnWhat(String what) {
        mCurrentClicked = what;
        switch (what) {
            case CLICK_WHAT_QUICKPHOTO:
                last_filetype_request = GENERATED_FILE;
                if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    mCurrentClicked = "";
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        Uri photoUri = createImageUri();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_STORAGE_PERMISSION);
                }
                break;
            case CLICK_WHAT_RECORDING:
                last_filetype_request = GENERATED_FILE;
                if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED &&
                        context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED) {
                    mCurrentClicked = "";
                    DialogView.getInstance().showRecording(this);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                            REQUEST_WRITE_STORAGE_PERMISSION);
                }
                break;
            case CLICK_WHAT_FROMDEVICE:
                last_filetype_request = EXTERNAL_FILE;
                if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    mCurrentClicked = "";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        Intent fileExploreIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        fileExploreIntent.addCategory(Intent.CATEGORY_OPENABLE);
                        fileExploreIntent.setType("*/*");
                        startActivityForResult(fileExploreIntent, REQUEST_CODE_PICK_FILE);
                    } else {
                        Intent fileExploreIntent = new Intent(
                                FileBrowserActivity.INTENT_ACTION_SELECT_FILE,
                                null,
                                context,
                                FileBrowserActivity.class
                        );
                        startActivityForResult(
                                fileExploreIntent,
                                REQUEST_CODE_PICK_FILE
                        );
                    }
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_STORAGE_PERMISSION);
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void launchFilesObserver(int storyId){
        mFileViewModel.getStoryFiles(storyId).observe(this, attachedFiles -> {
            adapter.setList(attachedFiles);
            attached_files_counter = attachedFiles.size();
            update_progress_bar(false);
            Log.d(TAG,"Detected files");
            for(Attachedfile af: attachedFiles){
                Log.d(TAG, af.toString());
            }
        });
    }

    float   log_weight = 0.2f,      date_weight = 0.2f,
            location_weight = 0.1f, witness_weight = 0.2f,
            file_weight = 0.3f,     total_score = 0f;
    double score_helper_num = 0;

    private void update_progress_bar(boolean setHasEdited) {
        if (times_progress_bar > 1 && setHasEdited) {
            has_edited = true;
            Log.d("Edited", String.valueOf(407));
        }
        times_progress_bar++;

        total_score = 0f;
        if(story_.getLog() != null && !story_.getLog().equals("")){
            score_helper_num = (double)story_.getLog().length();
            score_helper_num = sigmoid((score_helper_num - 100)/200);
            total_score += score_helper_num * log_weight;
        }
        if(story_.getOccurredAt() != null && !date_format_issue){
            total_score += date_weight;
        }
        if(story_.getLocation() != null && !story_.getLocation().equals("")){
            score_helper_num = (double)story_.getLocation().length();
            score_helper_num = sigmoid((score_helper_num - 5)/2);
            total_score += score_helper_num * location_weight;
        }
        if(story_.getWitnesses() != null && !story_.getWitnesses().equals("")){
            score_helper_num = (double)story_.getWitnesses().length();
            score_helper_num = sigmoid((score_helper_num - 5)/2);
            total_score += score_helper_num * witness_weight;
        }
        if (attached_files_counter > 0) {
            total_score +=  file_weight * Math.min(attached_files_counter, 2)/2;
        }
        binding.storyScoreBar.setProgress((int)(total_score* binding.storyScoreBar.getMax()));
    }

    private double sigmoid(double num){
        return (1/(1 + Math.exp(-num)));
    }

    @Override
    public void onObjectUpdatedLocally(ObjectContainer obj) {
        if(obj.getAfile_() != null){
            onFileInsertedInRoom(obj.getAfile_());
        }
    }

    @Override
    public void onObjectGotFireId(ObjectContainer obj, boolean was_internet_avail) {

    }


    public void onFileInsertedInRoom(Attachedfile af){
        if(af != null){
            attFilesSavedInRoom.add(af);
        }
    }

    public void onFileUpdatedInFirebase(Attachedfile af){
        if(af != null){
            attFilesSavedInRoomAndFirebase.add(af);
            String fname = af.getFileName();
            int pos = insertFileTaskNames.indexOf(fname);
            if(pos > -1) {
                insertFileTasks.remove(pos); //this is no longer being implemented
                insertFileTaskNames.remove(pos);
                if (toContinueDeleting) {
                    Log.d(TAG, "Need to delete");
                    fileRepo.deleteFilesFromFirestore(fileRepo.mFirestore, af);
                }
            }
        }
    }

    private void setLocationUI(){
        if(Objects.requireNonNull(sViewModel.getStory().getValue()).getLocation() != null && sViewModel.getStory().getValue().getLocation().length() > 6){
            binding.newlogLocation.setText(sViewModel.getStory().getValue().getLocation());
        }else{
            binding.newlogLocation.setText(LOCATION_PLACEHOLDER);
        }
    }

    @Override
    public void onLocationEdited(String location) {
        Objects.requireNonNull(sViewModel.getStory().getValue()).setLocation(location);
        update_progress_bar(true);
        setLocationUI();
    }

    @Override
    public void onTreatmentEdited(String treatment, String key_to_edit) {
        medication_was_edited = true;
        medication_key_to_edit = key_to_edit;
        solid_treatment_plan = treatment;
        handleMedication();
    }
}
