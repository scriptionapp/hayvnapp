package com.hayvn.hayvnapp.FileOperations;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.Size;
import android.webkit.MimeTypeMap;

import com.google.firebase.auth.FirebaseAuth;
import com.hayvn.hayvnapp.Activities.FileBrowserActivity;
import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Helper.FileTypeHelper;
import com.hayvn.hayvnapp.Model.Attachedfile;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.Story;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class ParentStorageWriter {
    private static final String TAG = "URI_WRITER";
    private FirebaseAuth mAuth;
    private Context context;

    public ParentStorageWriter(){
        mAuth = FirebaseAuth.getInstance();
        this.context = null;
    }

    public ParentStorageWriter(Context context){
        mAuth = FirebaseAuth.getInstance();
        this.context = context;
    }

    public Context getContext(){
        return context;
    }

    public FirebaseAuth getMAuth(){
        return mAuth;
    }

    public Bitmap getBitMap(String path, int width, int height){
        Bitmap thumbnail = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                thumbnail = context.getContentResolver().loadThumbnail(
                        Uri.parse(path), new Size(width, height), null);
            } else {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(path));
                thumbnail = ThumbnailUtils.extractThumbnail(bitmap, width, height);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return thumbnail;
    }

    public Uri createImageUri(String filename) {
        return null;
    }

    public Uri createAudioUri(String mFileName) {
        return null;
    }

    public Uri createUriWithoutCallback(Attachedfile af){
        return null;
    }

    public FileDescriptor getFileDescriptor(Uri uri) throws IOException {
        return null;
    }


    public void updateValuesAfterDownload(Uri localUri){

    }

    public String uriToString(Uri uri){
        return uri.toString();
    }

    public String getFileNameFromUri(Uri uri) {
        ContentResolver cr = getContext().getContentResolver();
        Cursor mCursor = cr.query(uri, null, null, null, null);
        assert mCursor != null;
        int indexedName = mCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        mCursor.moveToFirst();
        String mCurrentFileName = mCursor.getString(Objects.requireNonNull(indexedName));
        mCursor.close();
        return mCurrentFileName;
    }

    protected String getCustomDir(){
        return "/Hayvn_"+ Objects.requireNonNull(
                        Objects.requireNonNull(getMAuth().getCurrentUser())
                        .getEmail()).replace('@', '-')
                                    .replace("+","-");
    }

    public Uri getChosenFileURI(Intent data){
        Uri newFileUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (data != null) {
                newFileUri = data.getData();
            }
            ContentResolver cr = getContext().getContentResolver();
            assert newFileUri != null;
            cr.takePersistableUriPermission(
                    newFileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        } else {
            String stringUri = data.getStringExtra(
                    FileBrowserActivity.returnFileParameter);
            newFileUri = Uri.parse(stringUri);
        }
        return newFileUri;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    protected int sizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else {
            return data.getByteCount();
        }
    }

    public void compressImageAtUri(String uriString){
        Uri uri = Uri.parse(uriString);

        try {
            Bitmap finalBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
            int sz = sizeOf(finalBitmap);
            if(Math.sqrt(sz) < 300){
                Log.d(TAG, "Too small of an image already: "+String.valueOf(Math.sqrt(sz)));
            }else{
                float aspect = (float)finalBitmap.getHeight() / (float)finalBitmap.getWidth();
                int destW = aspect < 1 ? 300 : Math.round(300/aspect);
                int destH = Math.round(destW*aspect);
                Bitmap newBitmap = Bitmap.createScaledBitmap(finalBitmap, destW, destH, true);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                    OutputStream out = getContext().getContentResolver().openOutputStream(uri); //.openInputStream(contentUri);
                    newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    assert out != null;
                    out.flush();
                    out.close();
                } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
                    //TODO not tested!!!!!

                    Cursor cursor = null;
                    try {
                        String[]  proj = { MediaStore.Images.Media.DATA };
                        cursor = getContext().getContentResolver().query(uri,  proj, null, null, null);
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        cursor.moveToFirst();
                        String uriS = cursor.getString(column_index);
                        File fl = new File(uriS);
                        if(fl.exists()){
                            Log.d(TAG, "File exists");
                        }else{Log.d(TAG, "File doesnt exist");}
                        FileOutputStream out = new FileOutputStream(fl);
                        newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally{
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Attachedfile createFileByType(String mCurrentFilePath, String mCurrentFileName, Story story_, Case case_, String filetype){
        ContentResolver cr = getContext().getContentResolver();
        String type = cr.getType(Uri.parse(mCurrentFilePath));
        String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);

        Attachedfile attachedFile = new Attachedfile();
        attachedFile.setFileName(mCurrentFileName);
        attachedFile.setLocalFilePath(mCurrentFilePath);
        attachedFile.setExtension(ext);

        if(story_ != null){
            attachedFile.setSid(story_.getSid());
            attachedFile.setCid(story_.getCid());
            attachedFile.setCaseFbId(story_.getCaseFbId());
            attachedFile.setStoryFbId(story_.getFbId());
        }
        if(case_ != null){
            attachedFile.setCid(case_.getCid());
            attachedFile.setCaseFbId(case_.getFbId());
        }

        if (FileTypeHelper.isImageFile(cr, type, mCurrentFilePath)) {
            attachedFile.setType(Constant.IMG_FILE_TYPE);
        } else if (FileTypeHelper.isAudioFile(type, mCurrentFilePath, getContext()) ||
                filetype.equals(Constant.AUDIO_FILE_TYPE)) {
            attachedFile.setType(Constant.AUDIO_FILE_TYPE);
        }else {
            attachedFile.setType(ext);
        }

        return attachedFile;
    }

    public int deleteByPath(String path){
        int rows = -1;
        if(path != null){
            try{
                Uri uri = Uri.parse(path);
                if(uri != null){
                    rows = context.getContentResolver().delete(uri, null, null);
                    if(rows == 0){
                        Log.e(TAG,"Could not delete "+path+" :(");
                    }else{
                        Log.d(TAG,"Deleted "+path+ " ^_^");
                    }
                }
            }catch(Exception e){

            }finally{
                return rows;
            }
        }else{
            return rows;
        }
    }

    public boolean contentUriExists(Uri uri, Context mContext) {
        ContentResolver cr = mContext.getContentResolver();
        try {
            AssetFileDescriptor afd = cr.openAssetFileDescriptor(uri, "r");
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {
                    Log.d(TAG, String.valueOf(e));
                    return false;
                }
            }
        }catch (FileNotFoundException e) {
            Log.d(TAG, String.valueOf(e));
            return checkFileExists(uri);
        } catch (IllegalArgumentException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return checkFileExists(uri);
        }
        return true;
    }

    private boolean checkFileExists(Uri uri){
        try {
            File file = new File(uri.toString());
            return file.exists();
        } catch (Exception e1) {
            e1.printStackTrace();
            return false;
        }
    }

}
