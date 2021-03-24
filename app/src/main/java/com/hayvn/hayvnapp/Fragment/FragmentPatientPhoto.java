package com.hayvn.hayvnapp.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.hayvn.hayvnapp.Activities.PhotoshopActivity;
import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.IntentConstants;
import com.hayvn.hayvnapp.Constant.RoomConstants;
import com.hayvn.hayvnapp.Interfaces.CallbackGetFilesForStory;
import com.hayvn.hayvnapp.Interfaces.CallbackLocalStories;
import com.hayvn.hayvnapp.Interfaces.CallbackObjectUpdated;
import com.hayvn.hayvnapp.Model.Attachedfile;
import com.hayvn.hayvnapp.Model.ObjectContainer;
import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.Model.StoryFileCount;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.CaseRepo;
import com.hayvn.hayvnapp.Repository.FileRepo;
import com.hayvn.hayvnapp.Repository.StoryRepo;
import com.hayvn.hayvnapp.Utilities.LocalFileWriter;
import com.hayvn.hayvnapp.ViewModel.FileViewModel;
import com.hayvn.hayvnapp.ViewModel.StoryViewModel;
import com.hayvn.hayvnapp.databinding.FragmentPatientPhotoBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.hayvn.hayvnapp.Constant.Constant.REQUEST_IMAGE_CAPTURE;
import static com.hayvn.hayvnapp.Constant.Constant.REQUEST_WRITE_STORAGE_PERMISSION;
import static com.hayvn.hayvnapp.Constant.IntentConstants.FILEPATH;

public class FragmentPatientPhoto extends Fragment implements CallbackObjectUpdated, CallbackGetFilesForStory, CallbackLocalStories {
    private static final String TAG = "PATIENT_PHOTO";

    private int cid;
    private String fbid;
    private StoryViewModel s_viewmodel;
    private FileViewModel f_viewmodel;
    private Story profile_story = new Story();
    private ArrayList<Attachedfile> afs = new ArrayList<>();
    private Attachedfile photo_file = new Attachedfile();
    private LocalFileWriter lfw;
    private Context context;
    private Bitmap prof_bmp;
    private StoryRepo story_repo;
    private FileRepo file_repo;
    private CaseRepo case_repo;
    private CallbackObjectUpdated callback_updated;
    private String photo_name = "";
    private Uri photo_uri;
    private boolean file_observer = false;
    private boolean updated_photo = false;
    private boolean created_story = false;
    private Intent takePictureIntent;

    //for filtering operations with multiple profile pictures
    private String fbid_to_keep;
    private List<Story> stories_to_delete = new ArrayList<>();

    private FragmentPatientPhotoBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPatientPhotoBinding.inflate(inflater,container,false);

        View view = binding.getRoot();
        Objects.requireNonNull(getActivity()).setTitle(getString(R.string.app_name));
        context = this.getContext();
        lfw = new LocalFileWriter(getActivity().getApplicationContext());
        story_repo = new StoryRepo(getActivity().getApplication());
        file_repo = new FileRepo(getActivity().getApplication());
        case_repo = new CaseRepo(getActivity().getApplication());
        callback_updated = this;
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            cid = bundle.getInt(IntentConstants.CASE_ID_FOR_FRAG);
            fbid = bundle.getString(IntentConstants.CASE_FBID_FOR_FRAG);
        }
        checkProfilePictures();
        retrievePhotoStory();
        setClickListeners();
        setViews();
        return view;
    }

    private void setClickListeners(){
        binding.changePatientPhoto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        binding.patientPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(afs!=null && afs.size()>0){
                    if(photo_file != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        preview(photo_file);
                    }
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void takePhoto(){
        if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                if(profile_story.getSid() == 0){
                    profile_story.setType(RoomConstants.STORY_TYPE_PROFILE_PHOTO);
                    profile_story.setCid(cid);
                    profile_story.setCaseFbId(fbid);
                    profile_story = story_repo.setUpdateCreate(profile_story, true, true);
                    story_repo.updateInsert(profile_story, callback_updated, false, true);
                }else{
                    createUriTakePic();
                }
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE_PERMISSION);
        }
    }

    private void createImageUri() {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        photo_name = "JPEG_" + timeStamp;
        photo_uri =  lfw.createImageUri(photo_name);
    }

    public void updateAfAndInsertFile(String filetype) {
        file_repo.deleteFileCompletely(photo_file);
        Attachedfile attachedFile = lfw.createFileByType(photo_uri.toString(), photo_name, profile_story, null, filetype);
        attachedFile.setCid(cid);
        attachedFile.setFbId(fbid);
        FileRepo.insertUpdateAsyncTask task = file_repo.insertWithProgressTask(
                callback_updated,
                true,
                true);
        task.execute(attachedFile);
    }

    private void retrievePhotoStory(){
        s_viewmodel = ViewModelProviders.of(this).get(StoryViewModel.class);
        s_viewmodel.getProfilePhotoStory(cid).observe(this, story_temp -> {
            if(story_temp != null){
                profile_story = story_temp;
                if(!file_observer) launchFilesObserver(profile_story.getSid());
            }
        });
    }

    private void launchFilesObserver(int storyId){
        f_viewmodel = ViewModelProviders.of(this).get(FileViewModel.class);
        f_viewmodel.getStoryFiles(storyId).observe(this, attachedFiles -> {
            file_observer = true;
            afs = new ArrayList<>(attachedFiles);
            if(afs.size() > 0){
                photo_file = getLatest(afs);
                prof_bmp = lfw.getBitMap(photo_file.getLocalFilePath(), context, 200, 200);
            }
            setViews();
        });
    }

    private Attachedfile getLatest(List<Attachedfile> afs){
        Attachedfile target = new Attachedfile();
        Long updatedAt = 0l;
        for(Attachedfile af:afs){
            if(af.getUpdatedAt() > updatedAt){
                updatedAt = af.getUpdatedAt();
                target = af;
            }
        }
        return target;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void preview(Attachedfile af){
        String type = af.getType();
        if (type.equals(Constant.IMG_FILE_TYPE)) {
            Intent myIntent = new Intent(this.getContext(), PhotoshopActivity.class);
            myIntent.putExtra(FILEPATH, af.getLocalFilePath());
            startActivity(myIntent);
        }
    }

    private void setViews(){
        if(prof_bmp != null){
            binding.patientPhoto.setImageBitmap(prof_bmp);
            binding.changePatientPhoto.setText(getResources().getString(R.string.change_patient_photo));
        }else{
            binding.patientPhoto.setImageResource(R.drawable.ic_person_outline_24dp);
            binding.changePatientPhoto.setText( getResources().getString(R.string.add_patient_photo) );
        }
    }

    @Override
    public void onObjectUpdatedLocally(ObjectContainer obj) {
        if(obj.getStory_() != null && !file_observer){
            profile_story = obj.getStory_();
            launchFilesObserver(profile_story.getSid());
            created_story = true;
            createUriTakePic();
        }else if(obj.getAfile_() != null){
            updated_photo = true;
            story_repo.updateInsert(profile_story, callback_updated,
                    true, true);
        }
    }

    private void createUriTakePic(){
        if(photo_uri==null) createImageUri();
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photo_uri);
        Objects.requireNonNull(getActivity()).startActivityForResult(
                takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onObjectGotFireId(ObjectContainer obj, boolean internet_was_avail) {

    }

    public void closeIt(){
        if(created_story && !updated_photo){
            story_repo.deleteWithFiles(profile_story, false, false);
            f_viewmodel.getStoryFiles(profile_story.getSid()).removeObservers(this);
            s_viewmodel.getProfilePhotoStory(cid).removeObservers(this);
        }
    }


    private void checkProfilePictures(){
        StoryRepo sr = new StoryRepo(getActivity().getApplication());
        //pull stories that are of the right type, once
        sr.getProfilePhotoCID(cid);
        //calls callbackGetLocalStories
    }

    @Override
    public void callbackGetLocalStories(List<StoryFileCount> result1, List<Story> result2) {
        if(result2 != null && result2.size() >1){
            List<Integer>sids = new ArrayList<Integer>();
            Long max_created = 0L;
            stories_to_delete = new ArrayList<>();
            Story story_to_keep = new Story();
            for(Story s: result2){
                if(s.getCreatedAt() > max_created){
                    max_created = s.getCreatedAt();
                    fbid_to_keep = s.getFbId();
                    story_to_keep = s;
                }
            }
            story_to_keep = story_repo.setUpdateCreate(story_to_keep, true, true);
            story_repo.updateInsert(story_to_keep, null, false, true);
            for(Story s: result2){
                if(!s.getFbId().equals(fbid_to_keep)){
                    stories_to_delete.add(s);
                    sids.add(s.getSid());
                }
            }
            FileRepo fr = new FileRepo(getActivity().getApplication());
            fr.getFilesForStories(this, sids);
        }
    }

    @Override
    public void callbackGetFilesForStory(List<Attachedfile> result) {
        if(result!=null && result.size()>0){
            List<Attachedfile> changed = new ArrayList<>();
            for(Attachedfile af: result){
                    af.setStoryFbId(fbid_to_keep);
                    changed.add(af);
            }
            FileRepo fr = new FileRepo(getActivity().getApplication());
            for(Attachedfile af: changed){
                fr.insertUpdate(af, null, true, false);
            }

            StoryRepo sr = new StoryRepo(getActivity().getApplication());
            for(Story s: stories_to_delete){
                sr.deleteWithFiles(s, false, false);
            }
        }
    }
}
