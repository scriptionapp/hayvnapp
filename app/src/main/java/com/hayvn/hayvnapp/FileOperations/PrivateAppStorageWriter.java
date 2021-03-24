package com.hayvn.hayvnapp.FileOperations;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.core.content.FileProvider;

import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.FileConstants;
import com.hayvn.hayvnapp.Model.Attachedfile;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

public class PrivateAppStorageWriter extends ParentStorageWriter {
    private final static String TAG = "PRIVATE_WRITER";

    public PrivateAppStorageWriter(Context context){
        super(context);
    }

    @Override
    public Uri createImageUri(String filename) {
        String customDir = getCustomDir();
        File dir = new File(getContext().getFilesDir(), FileConstants.IMG_PRIVATE_FOLDER + customDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File imageFile = new File(dir, filename + "." + Constant.EXTENSION_IMG_SAVE);
        return FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName(), imageFile);
    }

    @Override
    public void updateValuesAfterDownload(Uri localUri){

    }

    @Override
    public Uri createAudioUri(String mFileName) {
        String customDir = getCustomDir();
        File dir = new File(getContext().getFilesDir(), FileConstants.AUDIO_PRIVATE_FOLDER+ customDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File audioFile = new File(dir, mFileName);
        return FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName(), audioFile);
    }

    @Override
    public Uri createUriWithoutCallback(Attachedfile af){
        File dir = getParentDir(af);
        Uri uri = null;
        if(dir != null) {
            File file = new File(dir, af.getFileName() + "." + af.getExtension());
            uri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName(), file);
        }
        return uri;
    }

    private File getParentDir(Attachedfile af){
        String type = af.getType();
        File dir;
        if (type.equals(Constant.IMG_FILE_TYPE)) {
            dir = new File(getContext().getFilesDir(), FileConstants.IMG_PRIVATE_FOLDER + getCustomDir());
            // getExternalStorageDirectory is deprecated in API 29
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } else if (type.equals(Constant.AUDIO_FILE_TYPE)) {
            dir = new File(getContext().getFilesDir(), FileConstants.AUDIO_PRIVATE_FOLDER + getCustomDir());
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } else {
            //TODO
            dir = new File(getContext().getFilesDir(), FileConstants.OTHER_PRIVATE_FOLDER + getCustomDir());
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        return dir;
    }

    @Override
    public FileDescriptor getFileDescriptor(Uri uri) throws IOException {
        ParcelFileDescriptor fileDescriptor = getContext().getContentResolver().openFileDescriptor(uri, "w");
        return fileDescriptor.getFileDescriptor();
    }
}
