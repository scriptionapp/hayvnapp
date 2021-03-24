package com.hayvn.hayvnapp.Dialog;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;

import androidx.core.view.GravityCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;


import android.util.Log;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hayvn.hayvnapp.Activities.CreatePinActivity;
import com.hayvn.hayvnapp.Activities.StoryActivity;
import com.hayvn.hayvnapp.Activities.SummaryActivity;
import com.hayvn.hayvnapp.Adapter.FilesSyncProgressAdapter;
import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.IntentConstants;
import com.hayvn.hayvnapp.Constant.RoomConstants;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.Helper.GetStoriesForCaseType;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Interfaces.CallbackCaseInserted;
import com.hayvn.hayvnapp.Interfaces.CallbackCompoundStringInt;
import com.hayvn.hayvnapp.Interfaces.CallbackObjectUpdated;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.FileNameProgress;
import com.hayvn.hayvnapp.Model.ObjectContainer;
import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Model.User;
import com.hayvn.hayvnapp.Repository.UserRepo;
import com.hayvn.hayvnapp.Utilities.LocalFileWriter;
import com.hayvn.hayvnapp.ViewModel.UserViewModel;
import com.hayvn.hayvnapp.Repository.CaseRepo;
import com.hayvn.hayvnapp.Repository.StoryRepo;
import com.hayvn.hayvnapp.Utilities.Utilities;
import com.hayvn.hayvnapp.databinding.ChoosewhatBinding;
import com.hayvn.hayvnapp.databinding.DialogChoosewhatattachmentBinding;
import com.hayvn.hayvnapp.databinding.DialogDataProgressBinding;
import com.hayvn.hayvnapp.databinding.DialogMoveCaseBinding;
import com.hayvn.hayvnapp.databinding.DialogNewCaseBinding;
import com.hayvn.hayvnapp.databinding.DialogPlayAudioBinding;
import com.hayvn.hayvnapp.databinding.DialogRecordingBinding;
import com.hayvn.hayvnapp.databinding.DialogRevokeSubmissionBinding;

import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.hayvn.hayvnapp.Constant.Constant.MODIFY_SUMMARY;
import static com.hayvn.hayvnapp.Constant.IntentConstants.CLICK_WHAT_FROMDEVICE;
import static com.hayvn.hayvnapp.Constant.IntentConstants.CLICK_WHAT_NEWSTORY;
import static com.hayvn.hayvnapp.Constant.IntentConstants.CLICK_WHAT_QUICKPHOTO;
import static com.hayvn.hayvnapp.Constant.IntentConstants.CLICK_WHAT_RECORDING;
import static com.hayvn.hayvnapp.Constant.TextConstants.SEND_WAIT;
import static com.hayvn.hayvnapp.Constant.TextConstants.SURE_REVOKE;
import static com.hayvn.hayvnapp.Constant.TextConstants.SURE_REVOKE_2;


public class DialogView implements CallbackObjectUpdated {


    public interface ChooseWhatDialogListener {
        void clickedOnWhat(String what);
    }

    public interface ChoseRestoreType {
        void choseRestoreType(String what);
    }

    public interface EndedRecording {
        void updateRecordingResults(String inputText1, String InputText2);
    }

    private static final String TAG = "DIALOG_VIEW";
    private DialogView.EndedRecording mRecordingListener;
    private DialogView.ChooseWhatDialogListener mStoryListener;
    private static DialogView dialogView;
    private Dialog dialog;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private Runnable updater;
    private int seconds;
    private Handler timerHandler;
    private boolean checked = false;
    private MediaRecorder mRecorder;
    private MediaPlayer mp;
    private String mFileName;
    private String shortName;
    private List <FileNameProgress> files_;
    private RecyclerView rvList;
    private FilesSyncProgressAdapter adapter;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CallbackObjectUpdated callback_updated;

    private DialogDataProgressBinding dialogDataProgressBinding;
    private DialogNewCaseBinding dialogNewCaseBinding;
    private DialogMoveCaseBinding dialogMoveCaseBinding;
    private DialogRevokeSubmissionBinding dialogRevokeSubmissionBinding;
    private ChoosewhatBinding choosewhatBinding;
    private DialogChoosewhatattachmentBinding choosewhatattachmentBinding;
    private DialogPlayAudioBinding dialogPlayAudioBinding;
    private DialogRecordingBinding dialogRecordingBinding;

    private DialogView() {
        this.callback_updated = this;
    }

    public synchronized static DialogView getInstance() {
        if (dialogView == null) {
            dialogView = new DialogView();
        }
        return dialogView;
    }

    public void dialogWithButton(Context context, String text, boolean show_button){
        dialogWithButtonCallback(context, text, show_button, null);
    }
    private void dialogWithButtonCallback(Context context, String text, boolean show_button, DialogPushPullData dppd){
        dismissDialog();
        dialogDataProgressBinding = DialogDataProgressBinding.inflate(LayoutInflater.from(context));
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(dialogDataProgressBinding.getRoot());
        dialogDataProgressBinding.textMessage.setText(text);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        if(show_button) {

            dialogDataProgressBinding.btnOkPulldata.setVisibility(View.VISIBLE);
            dialogDataProgressBinding.btnOkPulldata.setOnClickListener(v -> {
                dialog.dismiss();
            });
        }
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        Objects.requireNonNull(dialog.getWindow()).setLayout((6 * width)/7, height/3);
    }

    public void updateDataProgress(Context context, String text){
        dismissDialog();
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(dialogDataProgressBinding.getRoot());
        dialogDataProgressBinding.textMessage.setText(text);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        Objects.requireNonNull(dialog.getWindow())
                .setLayout((6 * width)/7, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void checkSyncOnInternet(Context context, String text){
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_data_progress);
    }

    public void dismissDialog(){
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    public void showCreateDialog(Activity context, String title) {
        dialogNewCaseBinding = DialogNewCaseBinding.inflate(LayoutInflater.from(context));
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(dialogNewCaseBinding.getRoot());
        dialog.setTitle(title);
        dialogNewCaseBinding.edtNewcase.requestFocus();
        InputMethodManager lManager;
        lManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert lManager != null;
        lManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        InputMethodManager finalLManager = lManager;
        dialogNewCaseBinding.btnCreate.setOnClickListener(v -> {
            finalLManager.hideSoftInputFromWindow(dialogNewCaseBinding.edtNewcase.getWindowToken(), 0);
            dialog.dismiss();
            createCase(RoomConstants.CASE_GENERAL_TYPE, context, Utilities.getInstance().getString(dialogNewCaseBinding.edtNewcase));
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private String preparePatientId(int doctorId, int patients, String prefix){
        if(doctorId == 0){

        }
        return prefix + String.valueOf(1000+doctorId+1) + String.valueOf(10000+patients+1);
    }

    private void createCase(String caseType, Activity act, String caseName) {
        class getCaseCountForDoctor implements CallbackCompoundStringInt, CallbackCaseInserted {
            UserRepo userRepo;
            CaseRepo mRepository;
            Case newCase;
            public getCaseCountForDoctor(Case newCase){
                this.newCase = newCase;
                mRepository = new CaseRepo(act.getApplication());
                userRepo = new UserRepo(act.getApplication());
                mRepository.getCountCases(this, mAuth.getUid());
            }

            @Override
            public void callbackGetCompoundStringInt(String name, int counter) {
                int patient_count = counter;
                int doctorId = Integer.parseInt(name);
                String current_prefix = SharedPrefHelper.readString(act.getBaseContext(), SharedprefConstants.PATIENT_PREFIX);
                if(current_prefix==null || current_prefix.equals("")){
                    current_prefix = SharedprefConstants.DEFAULT_PATIENT_PREFIX;
                }
                String patientId = preparePatientId(doctorId, patient_count, current_prefix);
                newCase.setPatientId(patientId);
                newCase.setStringThree(RoomConstants.IS_FAVOURITE_ED); //IF YOU CREATE IT IS AUTO FAVOURITE
                newCase = mRepository.setUpdateCreate(newCase, true, true);
                mRepository.insertWithCallback(newCase, this);
            }

            @Override
            public void callbackCaseWasInserted(Case case_) {
                Intent myIntent = new Intent(act.getBaseContext(), StoryActivity.class);
                myIntent.putExtra(IntentConstants.STORY_ACT_CASE, newCase);
                myIntent.putExtra(IntentConstants.STORY_ACT_JUST_CREATED, true);
                act.startActivityForResult(myIntent, 1);
            }
        }

        Case newCase = new Case(mAuth.getUid());
        newCase.setSubmitted(false);
        newCase.setName(caseName);
        newCase.setType(caseType);
        new getCaseCountForDoctor(newCase);
    }


    private void updateAndSaveCase(Case case_, Activity act){
        CaseRepo mRepository = new CaseRepo(act.getApplication());
        case_ = mRepository.setUpdateCreate(case_, true, true);
        mRepository.update(case_);
        dialog.dismiss();
    }

    public void showRenameCaseDialog(Activity act, String title, Case case_) {

        dialogNewCaseBinding = DialogNewCaseBinding.inflate(LayoutInflater.from(act));
        dialog = new Dialog(act);
        dialog.setCancelable(false);
        dialog.setContentView(dialogNewCaseBinding.getRoot());
        dialog.setTitle(title);
        dialogNewCaseBinding.edtNewcase.setText(case_.getName());
        dialogNewCaseBinding.edtNewcase.setSelection(0, case_.getName().length());
        dialogNewCaseBinding.btnCreate.setText(act.getString(R.string.change));
        dialogNewCaseBinding.edtNewcase.requestFocus();

        InputMethodManager lManager;
        lManager = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert lManager != null;
        lManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        InputMethodManager finalLManager = lManager;
        dialogNewCaseBinding.btnCreate.setOnClickListener(v -> {
            finalLManager.hideSoftInputFromWindow(dialogNewCaseBinding.edtNewcase.getWindowToken(), 0);
            case_.setName(Utilities.getInstance().getString(dialogNewCaseBinding.edtNewcase));
            updateAndSaveCase(case_, act);
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }


    public void showMoveStoryDialog(Activity context, Story st, List<String> allCases, int[] allCaseIds) {

        dialogMoveCaseBinding = DialogMoveCaseBinding.inflate(LayoutInflater.from(context));
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(dialogMoveCaseBinding.getRoot());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context.getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, allCases);
        dialogMoveCaseBinding.listCases.setAdapter(adapter);
        dialogMoveCaseBinding.btnMoveStory.setOnClickListener(v -> {
            int newCid = allCaseIds[dialogMoveCaseBinding.listCases.getSelectedItemPosition()];

            CaseRepo caseRepo = new CaseRepo(context.getApplication());
            StoryRepo storyRepo = new StoryRepo(context.getApplication());
            caseRepo.findCaseByCid(newCid).observe((LifecycleOwner) context, case_ -> {
                String  newCaseFbId = case_.getFbId();
                moveStoryAndSave(st, newCid, newCaseFbId, storyRepo);
                dialog.dismiss();
            });
        });
        dialogMoveCaseBinding.btnCancelMoveStory.setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void moveStoryAndSave(Story st, int newCid, String caseFbId, StoryRepo storyRepo){
        st.setCid(newCid);
        st.setCaseFbId(caseFbId);
        st = storyRepo.setUpdateCreate(st, true, true);
        storyRepo.updateInsert(st, callback_updated, true, true);
    }

    @Override
    public void onObjectUpdatedLocally(ObjectContainer obj) {
        //not needed
    }

    @Override
    public void onObjectGotFireId(ObjectContainer obj, boolean internet_was_avail) {
        //not needed
    }


    public void showRevokeSubmission(Activity context, Case case_) {

        dialogRevokeSubmissionBinding = DialogRevokeSubmissionBinding.inflate(LayoutInflater.from(context));

        dialog = new Dialog(context);
        dialog.setCancelable(true);
        dialog.setContentView(dialogRevokeSubmissionBinding.getRoot());


        dialogRevokeSubmissionBinding.btnRevoke.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(SURE_REVOKE)
                    .setMessage(SURE_REVOKE_2);
            builder.setPositiveButton(context.getString(R.string.cancel), (dialog1, id) -> {
                dialog.dismiss();
            });
            builder.setNegativeButton(context.getString(R.string.okok), (dialog1, id) -> {
                CaseRepo caseRepo = new CaseRepo(context.getApplication());
                ProgressDialog mWaitingDialog;
                mWaitingDialog = ProgressDialog.show(context, "",
                        SEND_WAIT, true);
                case_.setSubmitted(false);
                caseRepo.update(case_);
                mWaitingDialog.dismiss();
                dialog.dismiss();
            });
            AlertDialog dialog1 = builder.create();
            dialog1.show();
        });
       dialogRevokeSubmissionBinding.btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public void showChooseWhat(Context context) {
        choosewhatBinding = ChoosewhatBinding.inflate(LayoutInflater.from(context));
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(choosewhatBinding.getRoot());

        mStoryListener = (DialogView.ChooseWhatDialogListener) context;

        choosewhatBinding.buttonNewstory.setOnClickListener(v -> {
            dialog.dismiss();
            mStoryListener.clickedOnWhat(CLICK_WHAT_NEWSTORY);
        });
        choosewhatBinding.buttonQuickphoto.setOnClickListener(v -> {
            dialog.dismiss();
            mStoryListener.clickedOnWhat(CLICK_WHAT_QUICKPHOTO);
        });
        choosewhatBinding.buttonRecording.setOnClickListener(v -> {
            dialog.dismiss();
            mStoryListener.clickedOnWhat(CLICK_WHAT_RECORDING);
        });
        choosewhatBinding.buttonFromDevice.setOnClickListener(v -> {
            dialog.dismiss();
            mStoryListener.clickedOnWhat(CLICK_WHAT_FROMDEVICE);
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public void showChooseWhatAttachment(Context context) {

        choosewhatattachmentBinding = DialogChoosewhatattachmentBinding.inflate(LayoutInflater.from(context));
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(choosewhatattachmentBinding.getRoot());


        mStoryListener = (DialogView.ChooseWhatDialogListener) context;

      choosewhatattachmentBinding.buttonQuickphotoAtt.setOnClickListener(v -> {
            dialog.dismiss();
            mStoryListener.clickedOnWhat(CLICK_WHAT_QUICKPHOTO);
        });
        choosewhatattachmentBinding.buttonRecordingAtt.setOnClickListener(v -> {
            dialog.dismiss();
            mStoryListener.clickedOnWhat(CLICK_WHAT_RECORDING);
        });
        choosewhatattachmentBinding.buttonUploadAtt.setOnClickListener(v -> {
            dialog.dismiss();
            mStoryListener.clickedOnWhat(CLICK_WHAT_FROMDEVICE);
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void playRecording(Context context, String path){

        dialogPlayAudioBinding = DialogPlayAudioBinding.inflate(LayoutInflater.from(context));

        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(dialogPlayAudioBinding.getRoot());

        dialogPlayAudioBinding.pausePlay.setBackgroundResource(R.drawable.ic_play_arrow_black);

        final boolean[] startedRecording = {false};
        final boolean[] audioEnded = {false};

        mp = new MediaPlayer();

        try {
            mp.setDataSource(context, Uri.parse(path));
            mp.setLooping(false);
            mp.prepare();
            seconds = 0;

            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer mp) {
                    checked = !checked;
                    audioEnded[0] = true;
                    timerHandler.removeCallbacks(updater);
                    dialogPlayAudioBinding.pausePlay.setBackgroundResource(checked ? R.drawable.ic_pause_black : R.drawable.ic_play_arrow_black);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        dialogPlayAudioBinding.closePlay.setOnClickListener(view -> {
            mp.stop();
            mp.release();
            mp = null;
            timerHandler.removeCallbacks(updater);
            dialog.dismiss();
        });

        dialogPlayAudioBinding.pausePlay.setOnClickListener(view -> {
            if(audioEnded[0]) {
                seconds = 0;
                audioEnded[0] = false;
            }
            checked = !checked;
            startedRecording[0] = true;
            dialogPlayAudioBinding.pausePlay.setBackgroundResource(checked ? R.drawable.ic_pause_black : R.drawable.ic_play_arrow_black);
            if (checked) {
                try {
                    mp.start();
                    updateTime(dialogPlayAudioBinding.commentPlay);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if(mp.isPlaying()) {
                    mp.pause();
                    timerHandler.removeCallbacks(updater);
                }
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showRecording(Context context) {
        //ActivityCompat.requestPermissions((Activity) context, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        dialogRecordingBinding = DialogRecordingBinding.inflate(LayoutInflater.from(context));
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(dialogRecordingBinding.getRoot());


        dialogRecordingBinding.pauseRecording.setBackgroundResource(R.drawable.ic_play_arrow_black);

        dialogRecordingBinding.saveRecording.setVisibility(View.INVISIBLE);
        final boolean[] startedRecording = {false};
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        LocalFileWriter lfw = new LocalFileWriter(context);
        mRecordingListener = (DialogView.EndedRecording) context;

        dialogRecordingBinding.closeRecording.setOnClickListener(view -> {
            if (!startedRecording[0]) {
                dialog.dismiss();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(context.getString(R.string.save_recording))
                        .setTitle(context.getString(R.string.not_saved_yet));

                builder.setPositiveButton(context.getString(R.string.oksave), (dialog1, id) -> {
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                    timerHandler.removeCallbacks(updater);
                    mRecordingListener.updateRecordingResults(shortName, mFileName);//interface to pass info
                    dialog.dismiss();
                });
                builder.setNegativeButton(context.getString(R.string.exitwithoutsaving), (dialog1, id) -> {
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                    timerHandler.removeCallbacks(updater);
                    dialog.dismiss();
                });
                AlertDialog dialog1 = builder.create();
                dialog1.show();
            }
        });

        dialogRecordingBinding.pauseRecording.setOnClickListener(view -> {
            checked = !checked;
            startedRecording[0] = true;
            dialogRecordingBinding.saveRecording.setVisibility(View.VISIBLE);
            dialogRecordingBinding.pauseRecording.setBackgroundResource(checked ? R.drawable.ic_pause_black : R.drawable.ic_play_arrow_black);
            if (checked) {
                if (mRecorder == null) {
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    mRecorder.setAudioChannels(2);

                    @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    shortName = "record_" + timeStamp;
                    FileDescriptor fd = null;
                    String audio_name = shortName + "." + Constant.EXTENSION_AUDIO_SAVE;
                    Uri audio_uri = lfw.createAudioUri(audio_name);
                    String filePath = lfw.uriToString(audio_uri);
                    try {
                        fd = lfw.getFileDescriptor(audio_uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mFileName = filePath;

                    //mRecorder.setOutputFile(fd);
                    if(fd == null) {
                        mRecorder.setOutputFile(audio_uri.toString());
                    } else {
                        mRecorder.setOutputFile(fd);
                    }

                    try {
                        mRecorder.prepare();
                    } catch (IOException e) {
                        Log.e(TAG, "prepare() failed");
                    }

                    mRecorder.start();
                    seconds = 0;
                    updateTime(dialogRecordingBinding.recordingForPause);
                } else {
                    mRecorder.resume();
                    dialogRecordingBinding.recordingForPause.clearAnimation();
                    updateTime(dialogRecordingBinding.recordingForPause);
                }
            } else {
                timerHandler.removeCallbacks(updater);
                mRecorder.pause();
                anim.setDuration(500);
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                dialogRecordingBinding.recordingForPause.startAnimation(anim);
            }
        });

        dialogRecordingBinding.saveRecording.setOnClickListener(view -> {
            dialogRecordingBinding.recordingForPause.clearAnimation();
            mRecorder.resume();
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            timerHandler.removeCallbacks(updater);
            mRecordingListener.updateRecordingResults(shortName, mFileName);
            dialog.dismiss();
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void updateTime(TextView txt) {
        timerHandler = new Handler();
        updater = () -> {
            seconds++;
            txt.setText(convertToTime(seconds));
            timerHandler.postDelayed(updater, 1000);
        };
        timerHandler.post(updater);
    }

    @SuppressLint("SetTextI18n")
    public void showEmailChangeDialog(Activity context, User usr) {
        dialogNewCaseBinding = DialogNewCaseBinding.inflate(LayoutInflater.from(context));
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(dialogNewCaseBinding.getRoot());
        dialog.setTitle(context.getString(R.string.email));
        dialogNewCaseBinding.edtNewcase.requestFocus();
        dialogNewCaseBinding.edtNewcase.setHint(context.getString(R.string.email));
        dialogNewCaseBinding.btnCreate.setText(context.getString(R.string.change));

        InputMethodManager lManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert lManager != null;
        lManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        dialogNewCaseBinding.btnCreate.setOnClickListener(v -> {
            lManager.hideSoftInputFromWindow(dialogNewCaseBinding.edtNewcase.getWindowToken(), 0);
            dialog.dismiss();
            usr.setEmail(dialogNewCaseBinding.edtNewcase.getText().toString());
            new UserRepo(context.getApplication()).update(usr);

        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    public void showEmailCheckDialog(Activity context) {
        dialogNewCaseBinding = DialogNewCaseBinding.inflate(LayoutInflater.from(context));
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(dialogNewCaseBinding.getRoot());
        dialog.setTitle(context.getString(R.string.email));
        dialogNewCaseBinding.edtNewcase.requestFocus();
        dialogNewCaseBinding.edtNewcase.setHint(context.getString(R.string.email));
        dialogNewCaseBinding.btnCreate.setText(context.getString(R.string.okok));
        UserViewModel mViewModel;
        mViewModel = ViewModelProviders.of((FragmentActivity) context).get(UserViewModel.class);

        InputMethodManager lManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert lManager != null;
        lManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        dialogNewCaseBinding.btnCreate.setOnClickListener(v -> {
            lManager.hideSoftInputFromWindow(dialogNewCaseBinding.edtNewcase.getWindowToken(), 0);
            dialog.dismiss();
            mViewModel.getSettings().observe((LifecycleOwner) context, setting -> {
                assert setting != null;
                if (setting.getEmail().equals(dialogNewCaseBinding.edtNewcase.getText().toString())){
                    SharedPrefHelper.writeString(context, Constant.PIN,null);
                    context.startActivity(new Intent(context, CreatePinActivity.class));
                    context.finish();
                }else {
                    //
                }
            });
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private String convertToTime(int i) {
        int hh = Math.round(i / 360);
        int mm = Math.round(i / 60) - hh * 60;
        int ss = i - 360 * hh - 60 * mm;
        String hh1, mm1, ss1;
        if (hh < 10) {
            hh1 = "0" + String.valueOf(hh);
        } else {
            hh1 = String.valueOf(hh);
        }
        if (mm < 10) {
            mm1 = "0" + String.valueOf(mm);
        } else {
            mm1 = String.valueOf(mm);
        }
        if (ss < 10) {
            ss1 = "0" + String.valueOf(ss);
        } else {
            ss1 = String.valueOf(ss);
        }
        return (hh1 + ":" + mm1 + ":" + ss1);
    }
}