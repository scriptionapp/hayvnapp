package com.hayvn.hayvnapp.Dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hayvn.hayvnapp.Adapter.FilesSyncProgressAdapter;
import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.IntentConstants;
import com.hayvn.hayvnapp.Constant.MethodConstants;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.Helper.NetworkStateChangeBroadcaster;
import com.hayvn.hayvnapp.Helper.PullData;
import com.hayvn.hayvnapp.Helper.PushData;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.Interfaces.CallbackEmptyAction;
import com.hayvn.hayvnapp.Model.FileNameProgress;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.databinding.DialogPushDataBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.hayvn.hayvnapp.Constant.TextConstants.FINISH_SEND_START_PULL;
import static com.hayvn.hayvnapp.Constant.TextConstants.FINISH_SYNC;
import static com.hayvn.hayvnapp.Constant.TextConstants.NO_CANCEL;
import static com.hayvn.hayvnapp.Constant.TextConstants.OK_PROCEED;
import static com.hayvn.hayvnapp.Constant.TextConstants.PREPARING_TO_SEND;
import static com.hayvn.hayvnapp.Constant.TextConstants.PROBLEM_PULL;
import static com.hayvn.hayvnapp.Constant.TextConstants.PROGRESS_UPDATE;
import static com.hayvn.hayvnapp.Constant.TextConstants.YOU_CAN_NOW_SYNC;

public class DialogPushPullData {

    private static DialogPushPullData dialogView;
    private Activity act;
    private BroadcastReceiver br;
    private Dialog dialog;
    private LinearLayout list_of_files_view;
    private final static String TAG = "DIALOG_PUSHDATA";
    private List<FileNameProgress> files_list = new ArrayList<FileNameProgress>();
    private FilesSyncProgressAdapter adapter = new FilesSyncProgressAdapter();
    private RecyclerView rvList;
    private PullData pd = null;

    private DialogPushDataBinding binding;

    private DialogPushPullData() {
    }

    public synchronized static DialogPushPullData getInstance() {
        if (dialogView == null) {
            dialogView = new DialogPushPullData();
        }
        return dialogView;
    }

    public void filesProgress(Context context, List<FileNameProgress> files){
        files_list = files;
        list_of_files_view = dialog.findViewById(R.id.list_of_files_view);
        list_of_files_view.setVisibility(View.VISIBLE);

        rvList = dialog.findViewById(R.id.rv_files);
        rvList.setLayoutManager(new LinearLayoutManager(context));
        rvList.setVisibility(View.VISIBLE);
        rvList.setAdapter(adapter);
        adapter.setList(files_list);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        Objects.requireNonNull(dialog.getWindow()).setLayout((6 * width)/7, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void adapterChangeAt(int position, FileNameProgress newval){
        if(files_list != null && rvList != null && adapter != null && position<files_list.size()) {
            files_list.set(position, newval);
            adapter.notifyDataSetChanged();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showAlertAndProceed(Activity act, Application app, Context context_, boolean populate_media, String pull_what,
                                     String what_was_called){
        AlertDialog.Builder builder = new AlertDialog.Builder(context_);
        this.act = act;
        builder.setMessage(YOU_CAN_NOW_SYNC)
                .setTitle("!");

        builder.setPositiveButton(OK_PROCEED, (dialog, id) -> {
            proceedToOperations(app, context_, populate_media, pull_what, what_was_called);
        });

        builder.setNegativeButton(NO_CANCEL, (dialog, id) -> {
        });
        builder.setCancelable(false);
        AlertDialog dialog1 = builder.create();
        dialog1.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void proceedToOperations(Application app, Context context_, boolean populate_media, String pull_what,
                                     String what_was_called){
        if(what_was_called.equals("both")){
            handlingPushPullDataDialog(app, context_, what_was_called, populate_media, pull_what);
        }else if(what_was_called.equals("push")){
            handlingPushPullDataDialog(app, context_, what_was_called, populate_media, "");
        }else if(what_was_called.equals("pull")){
            handlingPushPullDataDialog(app, context_, what_was_called, populate_media, pull_what);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void launchPushPullDataDialog(Activity act, Application app, Context context_, boolean populate_media, String pull_what){
        showAlertAndProceed(act, app, context_, populate_media, pull_what, "both");
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void launchPushDataDialog(Activity act, Application app, Context context_, boolean populate_media){
        showAlertAndProceed(act, app, context_, populate_media, "", "push");
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void launchPullDataDialog(Activity act, Application app, Context context_, boolean populate_media, String pull_what){
        showAlertAndProceed(act, app, context_, populate_media, pull_what, "pull");
    }

    public void dismissPullPushDialog(){
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)

    private void handlingPushPullDataDialog(Application app, Context context_, String type, boolean populate_media, String pull_what) {
        binding = DialogPushDataBinding.inflate(LayoutInflater.from(context_));
        dialog = new Dialog(act);
        dialog.setCancelable(false);
        dialog.setContentView(binding.getRoot());
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle(PROGRESS_UPDATE);
        dialog.show();
        binding.btnOkPushdata.setActivated(false);
        binding.btnOkPushdata.setVisibility(View.INVISIBLE);
        binding.txtProgress.setText(PREPARING_TO_SEND);

        boolean isNetworkAvailable = NetworkStateChangeBroadcaster.getIsConnected();
        if(!isNetworkAvailable) {
            binding.txtProgress.setText(PROBLEM_PULL);
            binding.btnOkPushdata.setActivated(true);
            binding.btnOkPushdata.setVisibility(View.VISIBLE);
            binding.btnOkPushdata.setOnClickListener(v -> {
                dialog.dismiss();
            });
        }else{
            PullData pd = new PullData(context_, app, act, app.getContentResolver(), "DialogPushData", populate_media, pull_what);
            if(type.equals("push") || type.equals("both")){
                PushData push_data = new PushData(app);
                push_data.launchSync();
            }else if(type.equals("pull")){
                pd.populateDB();
            }

            binding.btnOkPushdata.setActivated(false);
            binding.btnOkPushdata.setVisibility(View.INVISIBLE);

            br = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra(IntentConstants.SYNC_BROADCAST_MESSAGE)) {
                    String message = intent.getStringExtra(IntentConstants.SYNC_BROADCAST_MESSAGE);
                    assert message != null;
                    if(message.equals(IntentConstants.PUSH_TO_FIRESTORE_FINISHED)){
                        if(type.equals("both")){
                            binding.txtProgress.setText(FINISH_SEND_START_PULL);
                            pd.populateDB();
                        }else{
                            binding.btnOkPushdata.setActivated(true);
                            binding.btnOkPushdata.setVisibility(View.VISIBLE);
                            if(list_of_files_view != null) list_of_files_view.setVisibility(View.GONE);
                            binding.txtProgress.setText(FINISH_SYNC);
                        }
                    }else if(message.equals(IntentConstants.PULL_LAUNCH_ADAPTER)) {
                        ArrayList<FileNameProgress> files =  intent.getParcelableArrayListExtra(IntentConstants.LIST_FILENAMEPROGRESS);
                        assert files != null;
                        if ((files != null) && (files.size() > 0)) {
                            for(Parcelable plb: files){
                                files_list.add((FileNameProgress)plb);
                            }
                        }
                        filesProgress(context_, files_list);
                    }else if(message.equals(IntentConstants.RESTART_FILES)) {
                        pd.cleanUpAndPopulateFiles();
                    } else if(message.equals(IntentConstants.PULL_UPDATE_ADAPTER)) {
                        FileNameProgress ff = intent.getParcelableExtra(IntentConstants.INDIVIDUAL_FILENAMEPROGRESS);
                        int ii = intent.getIntExtra(IntentConstants.LIST_FILENAMEPROGRESS_INDEX, 0);
                        adapterChangeAt(ii, ff);
                    }else if(message.equals(IntentConstants.PULL_FROM_FIRESTORE_FINISHED)) {
                        binding.btnOkPushdata.setActivated(true);
                        binding.btnOkPushdata.setVisibility(View.VISIBLE);
                        if(list_of_files_view != null) list_of_files_view.setVisibility(View.GONE);
                        binding.txtProgress.setText(FINISH_SYNC);
                    }else{
                        binding.txtProgress.setText(message);
                    }
                }
                }
            };


            binding.btnOkPushdata.setOnClickListener(v -> {
                if(br != null) act.unregisterReceiver(br);
                finishSync(context_, pull_what);
            });
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(IntentConstants.SYNC_TO_FIRESTORE_ACTION);
            act.registerReceiver(br, intentFilter);
        }
    }

    private void finishSync(Context context, String pull_what){
        Long now = (new Date()).getTime();
        SharedPrefHelper.writeString(context, SharedprefConstants.TIME_LAST_SYNC, String.valueOf(now));
        SharedPrefHelper.writeString(context, Constant.FIREBASESYNCSTATUS, null);
        if(pull_what.equals(MethodConstants.PULL_WHAT_ALL) || pull_what.equals(MethodConstants.PULL_WHAT_ALL_RECENT)){
            SharedPrefHelper.writeString(context, SharedprefConstants.SYNCED_ALL_ALREADY, SharedprefConstants.SYNCED_ALL_ALREADY);
        }
        if(act!=null && br != null )   try{act.unregisterReceiver(br);}catch(Exception e){}
        dialog.dismiss();
    }

}
