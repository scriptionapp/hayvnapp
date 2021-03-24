package com.hayvn.hayvnapp.Adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hayvn.hayvnapp.Activities.PhotoshopActivity;
import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Dialog.DialogView;
import com.hayvn.hayvnapp.Interfaces.CallbackRetrieveAndInsert;
import com.hayvn.hayvnapp.Model.Attachedfile;
import com.hayvn.hayvnapp.Model.FileNameProgress;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.FileRepo;
import com.hayvn.hayvnapp.Utilities.LocalFileWriter;
import com.hayvn.hayvnapp.databinding.ItemFileListBinding;

import java.util.ArrayList;
import java.util.List;

import static com.hayvn.hayvnapp.Constant.IntentConstants.FILEPATH;
import static com.hayvn.hayvnapp.Constant.MethodConstants.FAIL_FILE_PROGRESS;
import static com.hayvn.hayvnapp.Constant.MethodConstants.PROGRESSUPDATE_FILE_PROGRESS;
import static com.hayvn.hayvnapp.Constant.MethodConstants.SUCCESS_FILE_PROGRESS;
import static com.hayvn.hayvnapp.Constant.TextConstants.FILE_REMAIN;


public class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyViewHolder>
        implements CallbackRetrieveAndInsert {

    private final String TAG = "FILE_ADAPTER";
    private Context mContext;
    private Activity act;
    private static final int EOF = -1;
    private List<Attachedfile> files;
    public static final int MODIFY_VISIBILITY = 111;
    public FileRepo fileRepo;
    List <FileNameProgress> fnps = new ArrayList<FileNameProgress>();
    private final static int WRITE_EXTERNAL_STORAGE = 300;
    public LocalFileWriter ufw;
    public boolean permissionChecked;
    private boolean editability_condition;
    private boolean dnld = false;
    private int sent_files = 0;
    private int received_files = 0;

    class MyViewHolder extends RecyclerView.ViewHolder {

        private ItemFileListBinding binding;


        MyViewHolder(ItemFileListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fa-solid-900.ttf");
            binding.filenameInTheList.setTypeface(font);
        }
    }

    public FileAdapter(Context context, Activity act, FileRepo fileRepo, boolean editability_condition) {
        this.mContext = context;
        this.fileRepo = fileRepo;
        fileRepo.myCallback = this;
        this.act = act;
        ufw = new LocalFileWriter(context);
        this.permissionChecked = false;
        this.editability_condition = editability_condition;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyViewHolder(ItemFileListBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkAndGet(){
        sent_files = 0;
        received_files = 0;
        dnld = false;
        for(Attachedfile af: files) {
            if (!ufw.contentUriExists(Uri.parse(af.getLocalFilePath()), mContext) ) {
                getFileFromFirestore(af, sent_files); //i = position
                dnld = true;
                sent_files++;
            }
        }
        if(dnld) DialogView.getInstance().dialogWithButton(mContext, mContext.getString(R.string.downloading_files) , false);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setList(List<Attachedfile> files) {
        this.files = files;
        notifyDataSetChanged();
        checkAndGet();
    }


    //check NewStoryActivity onpermission... once permission is granted
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkAllFilesAgainAfterPermission(){
        checkAndGet();
        permissionChecked = false;
    }

    public Attachedfile getItem(int pos) {
        return files.get(pos);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull FileAdapter.MyViewHolder holder, int position) {
        Attachedfile attachedFile = files.get(position);
        String type = attachedFile.getType();

        //holder.thumbnail
        holder.binding.filenameInTheList.setText(attachedFile.getFileName());
        holder.binding.filenameInTheList.setOnClickListener(view -> {
            preview(attachedFile);
        });
        holder.binding.thumbnailInList.setOnClickListener(view -> {
            preview(attachedFile);

        });

        if(!editability_condition){
            holder.binding.removeFile.setEnabled(false);
        }
        holder.binding.removeFile.setOnClickListener(view -> {
            if(editability_condition) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(FILE_REMAIN)
                        .setTitle(mContext.getString(R.string.delete));
                builder
                        .setPositiveButton(mContext.getString(R.string.delete), (dialog, id) -> {
                            Attachedfile af = getItem(position);
                            fileRepo.deleteFileCompletely(af);
                        })
                        .setNegativeButton(mContext.getString(R.string.cancel), (dialogInterface, i) -> {
                            // just closes here
                        });
                builder.setCancelable(true);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        if(type.equals(Constant.IMG_FILE_TYPE)){
            Bitmap thumbnail = ufw.getBitMap(attachedFile.getLocalFilePath());
            if(thumbnail != null) holder.binding.thumbnailInList.setImageBitmap(thumbnail);
        } else if (type.equals(Constant.AUDIO_FILE_TYPE)){
            holder.binding.thumbnailInList.setImageResource(R.drawable.ic_headset_black_24dp);
        } else {
            //TODO
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void preview(Attachedfile af){
        String type = af.getType();
        if (type.equals(Constant.IMG_FILE_TYPE)) {
            Intent myIntent = new Intent(mContext, PhotoshopActivity.class);
            myIntent.putExtra(FILEPATH, af.getLocalFilePath());
            act.startActivityForResult(myIntent, MODIFY_VISIBILITY);
        } else if(type.equals(Constant.AUDIO_FILE_TYPE)){
            DialogView.getInstance().playRecording(mContext, af.getLocalFilePath());
        } else {
            //TODO
        }
    }
    @Override
    public int getItemCount() {
        if (files != null)
            return files.size();
        else return 0;
    }

     @RequiresApi(api = Build.VERSION_CODES.M)
     private void getFileFromFirestore(Attachedfile af, int i) {
         if (mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                 != PackageManager.PERMISSION_GRANTED) {
             permissionChecked = true;
             ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                     WRITE_EXTERNAL_STORAGE);
         }else{
             Uri uri = ufw.createUriWithoutCallback(af);
             downloadFromUri(uri, af, i);
         }
    }

    private void downloadFromUri(Uri uri, Attachedfile af, int position_in_list){
        if(     uri != null &&
                !uri.getPath().equals("") &&
                af.getFireStorageFilePath() != null &&
                !af.getFireStorageFilePath().equals("")){
            //calls callbackRetrieveAndInsert
            boolean use_compressed_data = true;
            fileRepo.getFileFromFirebaseWithCallback(af, use_compressed_data,
                    uri, position_in_list, mContext);

        }
    }

    @Override
    public void callbackRetrieveAndInsert(int finalI, float shareBytesReceived,
                                          String typeUpdate, Uri localUri) {
        callbackRetrieveAndInsert_(finalI, shareBytesReceived, typeUpdate, localUri);
    }

    public void callbackRetrieveAndInsert_(int finalI,
                                          float bytesReceived,
                                          String typeUpdate,
                                          Uri localUri) {
        if(typeUpdate.equals(PROGRESSUPDATE_FILE_PROGRESS)){

        }else if(typeUpdate.equals(SUCCESS_FILE_PROGRESS)){
            received_files++;
        }else if(typeUpdate.equals(FAIL_FILE_PROGRESS)){
            received_files++;
        }else if(typeUpdate.equals("finally")){

        }
        if(sent_files==received_files && sent_files !=0){
            DialogView.getInstance().dismissDialog();
        }
    }

}


