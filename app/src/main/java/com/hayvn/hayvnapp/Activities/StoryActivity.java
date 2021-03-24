package com.hayvn.hayvnapp.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hayvn.hayvnapp.Adapter.StoriesAdapter;
import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.FirebaseConstants;
import com.hayvn.hayvnapp.Constant.IntentConstants;
import com.hayvn.hayvnapp.Constant.MethodConstants;
import com.hayvn.hayvnapp.Constant.RoomConstants;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.Constant.TextConstants;
import com.hayvn.hayvnapp.Dialog.DialogPushPullData;
import com.hayvn.hayvnapp.Dialog.DialogView;
import com.hayvn.hayvnapp.Helper.NetworkStateChangeBroadcaster;
import com.hayvn.hayvnapp.Helper.PullData;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Interfaces.CallbackObjectUpdated;
import com.hayvn.hayvnapp.Interfaces.CallbackReceivedDataGeneral;
import com.hayvn.hayvnapp.Model.Attachedfile;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.ObjectContainer;
import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.Model.StoryFileCount;
import com.hayvn.hayvnapp.ProdDev.Modes;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.CaseRepo;
import com.hayvn.hayvnapp.Repository.FileRepo;
import com.hayvn.hayvnapp.Repository.RepoUtils;
import com.hayvn.hayvnapp.Repository.StoryRepo;
import com.hayvn.hayvnapp.Utilities.DateUtils;
import com.hayvn.hayvnapp.Utilities.LocalFileWriter;
import com.hayvn.hayvnapp.ViewModel.CaseViewModel;
import com.hayvn.hayvnapp.ViewModel.StoryViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.hayvn.hayvnapp.databinding.ActivityStoryBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.threeten.bp.OffsetDateTime;

import static com.hayvn.hayvnapp.Constant.Constant.CREATE_STORY_LOG;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.hayvn.hayvnapp.Constant.Constant.MODIFY_STORY;
import static com.hayvn.hayvnapp.Constant.Constant.MODIFY_SUMMARY;
import static com.hayvn.hayvnapp.Constant.Constant.REQUEST_CODE_PICK_FILE;
import static com.hayvn.hayvnapp.Constant.Constant.REQUEST_IMAGE_CAPTURE;
import static com.hayvn.hayvnapp.Constant.Constant.REQUEST_RECORD_AUDIO_PERMISSION;
import static com.hayvn.hayvnapp.Constant.Constant.REQUEST_WRITE_STORAGE_PERMISSION;
import static com.hayvn.hayvnapp.Constant.IntentConstants.CASE_TO_SUBMIT;
import static com.hayvn.hayvnapp.Constant.IntentConstants.CLICK_WHAT_FROMDEVICE;
import static com.hayvn.hayvnapp.Constant.IntentConstants.CLICK_WHAT_NEWSTORY;
import static com.hayvn.hayvnapp.Constant.IntentConstants.CLICK_WHAT_QUICKPHOTO;
import static com.hayvn.hayvnapp.Constant.IntentConstants.CLICK_WHAT_RECORDING;
import static com.hayvn.hayvnapp.Constant.TextConstants.PATIENT_PROFILE_IS_OLD;
import static com.hayvn.hayvnapp.Constant.TextConstants.SURE_DELETE;


public class StoryActivity extends BaseParentActivity implements
        DialogView.ChooseWhatDialogListener,
        DialogView.EndedRecording,
        CallbackObjectUpdated,
        CallbackReceivedDataGeneral,View.OnClickListener{

    private final String TAG = "STORY_ACTIVITY";
    private final String GENERATED_FILE = "own_file";
    private final String EXTERNAL_FILE = "external_file";
    Context context;
    FirebaseAuth mAuth;
    StoriesAdapter adapter;
    Case caseName;
    List<String> allCases;
    int[] allCaseIds;
    boolean just_created;
    String mCurrentFilePath;
    String mCurrentFileName;
    String mCurrentClicked;
    FileRepo fileRepo;
    CaseRepo caseRepo = null;
    CallbackObjectUpdated callback_updated;
    LocalFileWriter lfw;
    StoryViewModel mViewModel;
    CaseViewModel caseViewModel;
    List<StoryFileCount> mList;
    Button footer_button;
    boolean toSearch = false;
    boolean requested_latest = false;
    Attachedfile to_upload_file = null;
    String last_filetype_request = "";
    FirebaseFirestore m_firestore;
    long oldest = (new Date()).getTime();
    private ActivityStoryBinding binding;
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    OffsetDateTime occurredAt, previousOccurredAt;
    StoryRepo storyRepo;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        mAuth = FirebaseAuth.getInstance();
        callback_updated = this;
        caseRepo = new CaseRepo(getApplication());
        storyRepo = new StoryRepo(getApplication());
        fileRepo = new FileRepo(getApplication());
        lfw = new LocalFileWriter(this);
        m_firestore = FirebaseFirestore.getInstance();
        footer_button = new Button(this);
        footer_button.setText(getResources().getString(R.string.check_load_more_stories));
        footer_button.setMaxWidth(200);
        Drawable img = getResources().getDrawable(R.drawable.ic_sync_24dp);
        img.setBounds(new Rect(20, 0, 80, 60));
        footer_button.setCompoundDrawablePadding(20);//works via android:drawablePadding="20dp"
        footer_button.setCompoundDrawables(img, null, null, null);
        footer_button.setBackground(getResources().getDrawable(R.drawable.ic_btn_nobg_palegreen));

        footer_button.setOnClickListener(v->{
            requestOlderStories();
        });

       binding.mStories.addFooterView(footer_button);

        mViewModel = ViewModelProviders.of(this).get(StoryViewModel.class);
        caseViewModel = ViewModelProviders.of(this).get(CaseViewModel.class);
        mCurrentClicked = "";
        caseName = (Case) Objects.requireNonNull(getIntent().getExtras()).getSerializable(IntentConstants.STORY_ACT_CASE);
        allCases = Objects.requireNonNull(getIntent()).getStringArrayListExtra("allCases");
        allCaseIds = Objects.requireNonNull(getIntent().getExtras()).getIntArray("caseIds");
        just_created = Objects.requireNonNull(getIntent().getExtras()).getBoolean(IntentConstants.STORY_ACT_JUST_CREATED);

        caseViewModel.getCaseById(caseName.getCid()).observe(Objects.requireNonNull(this), case_ -> {
            assert case_ != null;
            caseName = case_;
            setUIValues();
        });
        occurredAt = OffsetDateTime.now();
        previousOccurredAt = occurredAt;
        adapter = new StoriesAdapter(context);
        binding.mStories.setAdapter(adapter);
        setSupportActionBar(binding.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout,
                binding.toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.fab.setOnClickListener(this);
        binding.drawerStory.navAddCase.setOnClickListener(this);
        binding.drawerStory.navReviewSubmit.setOnClickListener(this);
        binding.drawerStory.navRevokeSubmit.setOnClickListener(this);
        binding.drawerStory.navDashboard.setOnClickListener(this);
        binding.drawerStory.navAccountSettings.setOnClickListener(this);
        binding.drawerStory.navFaq.setOnClickListener(this);
        binding.drawerStory.navSignOut.setOnClickListener(this);
        binding.drawerStory.syncDataStory.setOnClickListener(this);


        if(Modes.getSubmission().equals("disabled")){
            binding.drawerStory.navRevokeSubmit.setVisibility(View.GONE);
            binding.drawerStory.navReviewSubmit.setVisibility(View.GONE);
        }else{
            if (caseName.getSubmitted()) {
                binding.drawerStory.navReviewSubmit.setVisibility(View.GONE);
            } else {
                binding.drawerStory.navRevokeSubmit.setVisibility(View.GONE);
            }
        }
        if(Modes.getMedical().equals("medical")){
            binding.drawerStory.gettingLegalHelpSection2.setVisibility(View.GONE);
        }
        if(Modes.getDashboard().equals("disabled")){
           binding.drawerStory.navDashboard.setVisibility(View.GONE);
        }
        if(Modes.getFaq().equals("disabled")){
            binding.drawerStory.navFaq.setVisibility(View.GONE);
        }

        if(just_created){
            startSummaryActivity(false);
            just_created = false;
        }

        mViewModel.getStoriesFileCountByCid(caseName.getCid()).observe(this, stories_files -> {
            assert stories_files != null;
            mList = stories_files;
            adapter.setList(stories_files);
            oldest = getOldestStoryFBTime(stories_files);
            if(!requested_latest) findLatestFbUpdAndRequest(stories_files);
        });

       binding.mStories.setOnItemClickListener((parent, view, position, id) -> {
            launchNewStoryActivity(((StoryFileCount) adapter.getItem(position)).getStory(),MODIFY_STORY);
        });

       binding.addToFavCase.setOnClickListener(v->{
            toggleFavourite();
        });

       binding.mStories.setOnCreateContextMenuListener(this);

       binding.edtSearch.setVisibility(View.GONE);
       binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s == "") {
                    adapter.setList(mList);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        binding.drawerStory.hamburgerDrawer.setOnClickListener(v -> {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });
    }

    private long getLatestStoryFBTime(List<StoryFileCount> lst){
        long latest = 0;
        if(lst != null){
            long temp = 0;
            for(StoryFileCount sf: lst){
                if(sf.getStory() != null && sf.getStory().getUpdatedFirebaseAt() != null){
                    temp = sf.getStory().getUpdatedFirebaseAt();
                    latest = Math.max(latest, temp);
                }else if(sf.getStory() != null && sf.getStory().getUpdatedFirebaseAt() == null){
                    //
                }
            }
        }
        return latest;
    }
    private long getOldestStoryFBTime(List<StoryFileCount> lst){
        long oldest = (new Date()).getTime();
        if(lst != null && lst.size()>0){
            long temp;
            for(StoryFileCount sf: lst){
                if(sf.getStory() != null && sf.getStory().getUpdatedFirebaseAt() != null){
                    temp = sf.getStory().getUpdatedFirebaseAt();
                    if(temp != 0) oldest = Math.min(oldest, temp);
                }else if(sf.getStory() != null && sf.getStory().getUpdatedFirebaseAt() == null){
                    //
                }
            }
        }
        return oldest;
    }

    private void findLatestFbUpdAndRequest(List<StoryFileCount> lst){
        requested_latest = true;
        long latest = getLatestStoryFBTime(lst);
        String case_fbid = caseName.getFbId();
        RepoUtils.getLatestorOldestStoriesAttachedFilesFire(latest, case_fbid, m_firestore, storyRepo, fileRepo, MethodConstants.FIRESTORE_GET_LATEST, null);
    }

    private void requestOlderStories(){
        footer_button.setEnabled(false);
        footer_button.setText(getResources().getString(R.string.check_loading_more_stories));
        footer_button.setMaxWidth(200);
        Drawable img = getResources().getDrawable(R.drawable.ic_sync_attn_24dp);
        img.setBounds(new Rect(20, 0, 80, 60));
        footer_button.setCompoundDrawablePadding(20);
        footer_button.setCompoundDrawables(img, null, null, null);
        String case_fbid = caseName.getFbId();
        RepoUtils.getLatestorOldestStoriesAttachedFilesFire(oldest, case_fbid, m_firestore, storyRepo, fileRepo, MethodConstants.FIRESTORE_GET_OLDER, this);
    }


    @Override
    public void callbackReceivedData(List<Case> cases_, List<Story> stories_, List<Attachedfile> afiles_) {
        if(stories_ == null){
            Toast.makeText(
                    this,
                    TextConstants.PROBLEM_PULL,
                    Toast.LENGTH_LONG).show();
        }else{
            if(stories_.size()>= FirebaseConstants.PAGINATION_STEP){
                footer_button.setEnabled(false);
                footer_button.setText(getResources().getString(R.string.check_load_more_stories));
                footer_button.setMaxWidth(200);
                Drawable img = getResources().getDrawable(R.drawable.ic_sync_24dp);
                img.setBounds(new Rect(20, 0, 80, 60));
                footer_button.setCompoundDrawablePadding(20);//works via android:drawablePadding="20dp"
                footer_button.setCompoundDrawables(img, null, null, null);
            }else{
                footer_button.setEnabled(true);
                footer_button.setText(getResources().getString(R.string.no_more_stories));
                footer_button.setTextColor(getResources().getColor(R.color.disabled_font));
                footer_button.setMaxWidth(200);
                footer_button.setCompoundDrawables(null, null, null, null);
                footer_button.setBackground(getResources().getDrawable(R.drawable.ic_btn_bg_white));
            }
        }
    }


    boolean change_into_favourite = false;
    boolean change_from_favourite = false;
    private void toggleFavourite(){
        String st = caseName.getStringThree();
        if(st == null || st.equals(RoomConstants.IS_NOT_FAVOURITE_ED)){
            caseName.setStringThree(RoomConstants.IS_FAVOURITE_ED);
            change_into_favourite = true;
            change_from_favourite = false;
        }else{
            caseName.setStringThree(RoomConstants.IS_NOT_FAVOURITE_ED);
            change_into_favourite = false;
            change_from_favourite = true;
        }
        setFavouritesUI();
    }


    private void setFavouritesUI(){
        String st = caseName.getStringThree();
        Drawable fav = ContextCompat.getDrawable(this, R.drawable.ic_star_favorite_24dp);
        Drawable notfav = ContextCompat.getDrawable(this, R.drawable.ic_star_greyfavorite_24dp);
        assert fav != null;
        fav.setBounds(0, 0, 60, 60);
        notfav.setBounds(0, 0, 60, 60);

        if(st == null || !st.equals(RoomConstants.IS_FAVOURITE_ED)){
            binding.addToFavCase.setCompoundDrawables(notfav, null, null, null);
            binding.addToFavCase.setText(TextConstants.BUTTON_NOT_FAV_TEXT);
        }else{
            binding.addToFavCase.setCompoundDrawables(fav, null, null, null);
            binding.addToFavCase.setText(TextConstants.BUTTON_FAV_TEXT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void launchNewStoryActivity(Story story, int what){
        Intent myIntent = new Intent(getBaseContext(), NewStoryActivity.class); //getBaseContext
        myIntent.putExtra(IntentConstants.STORY_TO_MODIFY, story);
        myIntent.putExtra(IntentConstants.PARENT_CASE, caseName);
        startActivityForResult(myIntent, what);
    }

    private String concatWithPref(String prefix, String text){
        return prefix + ": " + text;
    }
    private void setUIValues(){
        if(caseName != null) {
            String allerg = emptyOrNull(caseName.getStringOne());
            String summary = emptyOrNull(caseName.getSummary());
            String allerg_show = "";
            if (allerg.equals("")) {
               binding.allergiesView.setText(TextConstants.FILL_THE_PROFILE_PLS);
            } else if (allerg.equals(RoomConstants.NO_KNOWN_ALLERGIES)) {
                binding.allergiesView.setText(TextConstants.NO_KNOWN_ALLERGIES_TXT);
            } else {
                binding.allergiesView.setText(concatWithPref(getString(R.string.allergies), allerg));
            }

            if(!summary.equals("")){
                ArrayList<String> summ_parts = new ArrayList<String>();
                summ_parts = new ArrayList<String>( Arrays.asList(summary.split(";", -1)) );
                if(summ_parts.size() > 1){
                    binding.medHist.setText(concatWithPref(TextConstants.MED_HIST, summ_parts.get(0)));
                    binding.drugHist.setText(concatWithPref(TextConstants.DRUG_HIST, summ_parts.get(1)));
                }else if(summ_parts.size() == 1){
                    binding.medHist.setText(concatWithPref(TextConstants.MED_HIST, summ_parts.get(0)));
                }
            }

            String profile = TextConstants.PATIENT_PROFILE_ID + caseName.getPatientId();
           binding.profileButton.setText(profile);
            binding.profileButton.setOnClickListener(view -> {
                startSummaryActivity(false);
            });

            binding.allergiesView.setOnClickListener(view -> {
                startSummaryActivity(false);
            });

            binding.medHist.setOnClickListener(view -> {
                startSummaryActivity(false);
            });

            binding.drugHist.setOnClickListener(view -> {
                startSummaryActivity(false);
            });

            if(caseName.getName() != null){
                Objects.requireNonNull(getSupportActionBar()).setTitle(caseName.getName());
            }
            setFavouritesUI();
        }
    }

    private String emptyOrNull(String s){
        if(s==null){return "";}
        return s;
    }

    private void filter(String text) {
        List<StoryFileCount> filteredList = new ArrayList<>();
        if (mList != null) {
            for (StoryFileCount item : mList) {
                if (item.getStory().getTitle() != null && item.getStory().getTitle().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(item);
                }
            }
            adapter.setList(filteredList);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.mStories) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.story_action, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = menuInfo.position;
        switch (item.getItemId()) {
            case R.id.copy_story_action:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("copy1", ((StoryFileCount)adapter.getItem(position)).getStory().toClip());
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
                return true;
            case R.id.delete_story_action:
                AlertDel(position);
                return true;
            case R.id.move_story_action:
                moveCases(position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private boolean getToCompressImg(){
        if(last_filetype_request.equals(GENERATED_FILE)){
            return true;
        }else{
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void clickedOnWhat(String what) {
        mCurrentClicked = what;
        switch (what) {
            case CLICK_WHAT_NEWSTORY:
                mCurrentClicked = "";
                Story story_ = new Story(true);
                story_.setCid(caseName.getCid());
                story_.setCaseFbId(caseName.getFbId());
                story_ = storyRepo.setUpdateCreate(story_, true, true);
                storyRepo.updateInsert(story_, callback_updated, false, false);
                break;
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
                    DialogView.getInstance().showRecording(context);
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

    private Uri createImageUri() {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mCurrentFileName = "JPEG_" + timeStamp;
        Uri imageUri =  lfw.createImageUri(mCurrentFileName);
        mCurrentFilePath = imageUri.toString();
        return imageUri;
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_WRITE_STORAGE_PERMISSION ||
                requestCode==REQUEST_RECORD_AUDIO_PERMISSION ) {
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

    @Override
    public void onObjectUpdatedLocally(ObjectContainer obj) {
        if(obj.getStory_() != null){
            if(to_upload_file != null){
                onStoryInsertedAndSavedLOCALLY(obj.getStory_());
            }
            onStoryInserted(obj.getStory_());
        }else if(obj.getAfile_() != null){
            //do nothing
        }
    }



    public void onStoryInserted(Story _story){
        if(_story.getSid() >= 0){
            launchNewStoryActivity(_story, CREATE_STORY_LOG);
        }else{
            Log.d(TAG,"write operation did not conclude");
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    private void suggestUpdateCase(){
        int hrs = DateUtils.hoursToNow( caseName.getUpdatedAt() );
        if(hrs > 24*14){
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("!");
            alertDialog.setMessage(PATIENT_PROFILE_IS_OLD);
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.nono), (dialogInterface, i) -> alertDialog.dismiss());
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.okok), (dialog, which) -> {
                alertDialog.dismiss();
                startSummaryActivity(false);
            });
            alertDialog.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            updateAfAndInsertFile(Constant.IMG_FILE_TYPE);
        } else if (requestCode == MODIFY_SUMMARY && resultCode == RESULT_OK) {
            caseName = (Case) Objects.requireNonNull(data.getExtras()).getSerializable("NEW_SUMMARY_INSTANCE");
            //caseRepo.update(caseName);
            setUIValues();
        } else if ((requestCode == MODIFY_STORY || requestCode == CREATE_STORY_LOG) && resultCode == RESULT_OK) {
            Story latest_story = (Story) data.getSerializableExtra(IntentConstants.NEW_STORY_INSTANCE);
            latest_story = storyRepo.setUpdateCreate(latest_story, true, true);
            mViewModel.update(latest_story, null, true, true);
            caseName.setLastEntry(latest_story.getTitle() + "\n" + latest_story.getLog());
            suggestUpdateCase();
            //caseRepo.update(caseName);
        } else if (requestCode == CREATE_STORY_LOG && resultCode == RESULT_CANCELED) {
            Story newStory = (Story) data.getSerializableExtra(IntentConstants.NEW_STORY_INSTANCE);
            mViewModel.delete(newStory);
        } else if (requestCode == REQUEST_CODE_PICK_FILE) {
            if (resultCode == RESULT_OK) {
                Uri newFileUri = lfw.getChosenFileURI(data);
                mCurrentFilePath = newFileUri.toString();
                mCurrentFileName = lfw.getFileNameFromUri(newFileUri);
                String type = lfw.setType(this, newFileUri);
                updateAfAndInsertFile(type);
                Log.d(TAG, "Received FILE path from file browser:\n" + mCurrentFilePath);
            } else {
                Log.d(TAG, "No file selected");
            }
        }
    }

    private void broadcastNewFile(Uri contentUri){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scanIntent.setData(contentUri);
            sendBroadcast(scanIntent);
        } else {
            final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
            sendBroadcast(intent);
        }
    }

    @Override
    public void updateRecordingResults(String inputText1, String uriPath) {
        mCurrentFileName = inputText1;
        mCurrentFilePath = uriPath;
        updateAfAndInsertFile(Constant.AUDIO_FILE_TYPE);
    }

    public void updateAfAndInsertFile(String filetype) {
        to_upload_file = lfw.createFileByType(mCurrentFilePath, mCurrentFileName, null, caseName, filetype);
        Story story_ = new Story();
        story_.setCid(caseName.getCid());
        story_.setCaseFbId(caseName.getFbId());
        story_.setTitle(mCurrentFileName);
        story_ = storyRepo.setUpdateCreate(story_, true, true);
        caseName.setLastEntry(story_.getTitle());
        storyRepo.updateInsert(story_, callback_updated, false, false);
    }

    @Override
    public void onObjectGotFireId(ObjectContainer obj, boolean internet_was_avail) {
        if(obj.getStory_() != null)  onStoryInsertedAndSavedToFire(obj.getStory_());
    }

    public void onStoryInsertedAndSavedLOCALLY(Story _story){
        if(_story.getSid() >= 0){
            caseName.setLastEntry(_story.getTitle() + "\n" + _story.getLog());

            if(to_upload_file != null){
                to_upload_file.setSid(_story.getSid());
                to_upload_file.setStoryFbId(_story.getFbId());
                fileRepo.insertUpdate(
                        to_upload_file,
                        null,
                        true,
                        getToCompressImg());
                to_upload_file = null;
            }
        }else{
            Log.d(TAG,"write operation did not conclude");
        }
    }

    public void onStoryInsertedAndSavedToFire(Story _story){
        if(_story.getSid() >= 0){
            caseName.setLastEntry(_story.getTitle() + "\n" + _story.getLog());
            if(to_upload_file != null){
                to_upload_file.setSid(_story.getSid());
                to_upload_file.setStoryFbId(_story.getFbId());
                fileRepo.insertUpdate(
                        to_upload_file,
                        null,
                        true,
                        getToCompressImg());
                to_upload_file = null;
            }
        }else{
            Log.d(TAG,"write operation did not conclude");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            if (toSearch) {
                toSearch = false;
                binding.edtSearch.setText("");
                binding.edtSearch.setVisibility(View.GONE);
                item.setIcon(R.drawable.ic_search_black);
            } else {
                toSearch = true;
                binding.edtSearch.setVisibility(View.VISIBLE);
                item.setIcon(R.drawable.ic_clear_black);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        if(change_into_favourite || change_from_favourite){
            caseRepo.updateWithoutTime(caseName);
            if(change_into_favourite){
                ArrayList<String> curr = SharedPrefHelper.getStringList(context, SharedprefConstants.FAVOURITES_LIST);
                if(curr == null){
                    curr = new ArrayList<>();
                }
                curr.add(String.valueOf(caseName.getCid()));
                SharedPrefHelper.writeStringList(context, SharedprefConstants.FAVOURITES_LIST, curr);
                syncFavsFire(caseName, true);
            }else{
                ArrayList<String> curr = SharedPrefHelper.getStringList(context, SharedprefConstants.REMOVED_FROM_FAVOURITES_LIST);
                if(curr == null){
                    curr = new ArrayList<>();
                }
                curr.add(String.valueOf(caseName.getCid()));
                SharedPrefHelper.writeStringList(context, SharedprefConstants.REMOVED_FROM_FAVOURITES_LIST, curr);
                syncFavsFire(caseName, false);
            }
        }
        finish();
    }

    private void syncFavsFire(Case cs, boolean is_fav){
        if(cs.getFbId()==null || cs.getFbId().equals("")) return;

        ArrayList<String> empty1 = new ArrayList<>();
        ArrayList<String> empty2 = new ArrayList<>();
        empty1.add(String.valueOf(caseName.getFbId()));
        if(is_fav)caseRepo.storeFavourites(empty1, empty2);
        else caseRepo.storeFavourites(empty2, empty1);
    }

    private void AlertDel(int pos) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("!");
        alertDialog.setMessage(SURE_DELETE);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.nono), (dialogInterface, i) -> alertDialog.dismiss());
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.okok), (dialog, which) -> {
            alertDialog.dismiss();
            Story sc = ((StoryFileCount) adapter.getItem(pos)).getStory();
            storyRepo.deleteWithFiles(sc, false, false);
        });
        alertDialog.show();
    }

    private void moveCases(int pos) {
        Story s = ((StoryFileCount) adapter.getItem(pos)).getStory();
        if(allCases!=null && allCaseIds != null){
            DialogView.getInstance().showMoveStoryDialog(this, s, allCases, allCaseIds);
        }
    }

    private void startSummaryActivity(boolean from_drawer){
        startActivityForResult(new Intent(context, SummaryActivity.class).putExtra("MODIFY", caseName), MODIFY_SUMMARY);
        if(from_drawer) binding.drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onClick(View view) {

        if (view == binding.fab){
            DialogView.getInstance().showChooseWhat(context);
        }else if (view == binding.drawerStory.navCaseSummary){
            startSummaryActivity(true);
        }else if (view == binding.drawerStory.navAddCase){
            DialogView.getInstance().showCreateDialog(this, getString(R.string.add_the_case));
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }else if (view == binding.drawerStory.navReviewSubmit){
            startActivity(new Intent(context, ReviewSubmitActivity.class).putExtra(CASE_TO_SUBMIT, caseName));
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }else if (view == binding.drawerStory.navRevokeSubmit){
            DialogView.getInstance().showRevokeSubmission(this, caseName);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }else if (view == binding.drawerStory.navDashboard){
            startActivity(new Intent(context, DashboardActivity.class));
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }else if (view == binding.drawerStory.navAccountSettings){
            Intent myIntent = new Intent(this, SettingActivity.class);
            myIntent.putExtra("case_name", caseName.getName());
            myIntent.putExtra("case_id", caseName.getCid());
            startActivity(myIntent);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }else if (view == binding.drawerStory.navFaq) {
            startActivity(new Intent(context, FaqActivity.class));
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }else if (view == binding.drawerStory.navSignOut) {
            PullData pd = new PullData();
            pd.eraseDB(context, "StoryAct");
        }else if (view == binding.drawerStory.syncDataStory){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                DialogPushPullData.getInstance().launchPushPullDataDialog(this, this.getApplication(), context, true, MethodConstants.PULL_WHAT_OTHERS);
            }
        }


    }

    public void onDBErased(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(context, LoginActivity.class));
    }
}
