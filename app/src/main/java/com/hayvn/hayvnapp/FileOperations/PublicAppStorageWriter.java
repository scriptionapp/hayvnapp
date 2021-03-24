package com.hayvn.hayvnapp.FileOperations;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

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

public class PublicAppStorageWriter extends ParentStorageWriter {
    private final static String TAG = "PUBLIC_WRITER";

    public PublicAppStorageWriter(Context context){
        super(context);
    }

    @Override
    public Uri createImageUri(String filename) {
        String customDir = getCustomDir();
        Uri imageUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver cr = getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures" + customDir);
            imageUri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()+ customDir); // getExternalStorageDirectory is deprecated in API 29
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File imageFile = new File(dir, filename + "." + Constant.EXTENSION_IMG_SAVE);
            imageUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName(), imageFile);
            sendTheBroadcast(imageUri);
        }
        return imageUri;
    }

    @Override
    public void updateValuesAfterDownload(Uri localUri){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver cr = getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.IS_PENDING, 0);
            cr.update(localUri, values, null, null);
        } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                scanIntent.setData(localUri);
                getContext().sendBroadcast(scanIntent);
            } else {
                final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
                getContext().sendBroadcast(intent);
            }
        }
    }

    @Override
    public Uri createAudioUri(String mFileName) {
        String customDir = getCustomDir();
        Uri audioUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver cr = getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, mFileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Music" + customDir);
            audioUri = cr.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + customDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String mFilePath = dir.getAbsolutePath();
            mFilePath += "/" + mFileName;
            File audioFile = new File(mFilePath);
            audioUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName(), audioFile);
            sendTheBroadcast(audioUri);
        }
        return audioUri;
    }

    @Override
    public Uri createUriWithoutCallback(Attachedfile af){
        ContentResolver cr = getContext().getContentResolver();
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = createValues(af);
            Uri parentUri = getParentURI(af);
            uri = cr.insert(parentUri, values);
        } else {
            File dir = getParentDir(af);
            if(dir != null) {
                File file = new File(dir, af.getFileName() + "." + af.getExtension());
                uri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName(), file);
                sendTheBroadcast(uri);
            }
        }
        return uri;
    }

    @Override
    public FileDescriptor getFileDescriptor(Uri uri) throws IOException {
        ParcelFileDescriptor fileDescriptor = getContext().getContentResolver().openFileDescriptor(uri, "w");
        sendTheBroadcast(uri);

        return fileDescriptor.getFileDescriptor();
    }

    //min API is 25
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private ContentValues createValues(Attachedfile af){
        String type = af.getType();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, af.getFileName());
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);

        if (type.equals(Constant.IMG_FILE_TYPE)) {
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures" + getCustomDir());
        } else if (type.equals(Constant.AUDIO_FILE_TYPE)) {
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Music" + getCustomDir());
        } else {
            //TODO
        }
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, af.getFileName());
        return values;
    }

    private Uri getParentURI(Attachedfile af){
        String type = af.getType();
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (type.equals(Constant.IMG_FILE_TYPE)) {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if (type.equals(Constant.AUDIO_FILE_TYPE)) {
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            } else {
                uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
            }
        }
        return uri;
    }

    private File getParentDir(Attachedfile af){
        String type = af.getType();
        File dir = null;
        if (type.equals(Constant.IMG_FILE_TYPE)) {
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + getCustomDir());
            // getExternalStorageDirectory is deprecated in API 29
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } else if (type.equals(Constant.AUDIO_FILE_TYPE)) {
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + getCustomDir());
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } else {
            //TODO after we add the ability to attach random files
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + getCustomDir());
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        return dir;
    }

    private void sendTheBroadcast(Uri contentUri){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scanIntent.setData(contentUri);
            getContext().sendBroadcast(scanIntent);
        } else {
            //final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, contentUri); //Uri.parse("file://" + Environment.getExternalStorageDirectory()));
            final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
            getContext().sendBroadcast(intent);
        }
    }




}
