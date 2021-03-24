package com.hayvn.hayvnapp.Utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;

import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.FileOperations.ParentStorageWriter;
import com.hayvn.hayvnapp.FileOperations.PrivateAppStorageWriter;
import com.hayvn.hayvnapp.FileOperations.PublicAppStorageWriter;
import com.hayvn.hayvnapp.Model.Attachedfile;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.Story;

import java.io.FileDescriptor;
import java.io.IOException;


public class LocalFileWriter {
    private final static String WRITER_TYPE_PRIVATE = "PRIVATE";
    private final static String WRITER_TYPE_PUBLIC = "PUBLIC";
    private final static String WRITER_TYPE = WRITER_TYPE_PRIVATE;

    ParentStorageWriter writer;
    Context context;

    public LocalFileWriter(Context context){
        this.context = context;
        if(WRITER_TYPE.equals(WRITER_TYPE_PRIVATE)){
            writer = new PrivateAppStorageWriter(context);
            writer = (PrivateAppStorageWriter) writer;
        }else if(WRITER_TYPE.equals(WRITER_TYPE_PUBLIC)){
            writer = new PublicAppStorageWriter(context);
            writer = (PublicAppStorageWriter) writer;
        }
    }

    public Uri createImageUri(String filename) {
        return writer.createImageUri(filename);
    }

    public Uri getChosenFileURI(Intent data){
        return writer.getChosenFileURI(data);
    }

    public void compressImageAtUri(String uriString){
        writer.compressImageAtUri(uriString);
    }

    public Uri createUriWithoutCallback(Attachedfile af){
        return writer.createUriWithoutCallback(af);
    }

    public void updateValuesAfterDownload(Uri localUri){
        writer.updateValuesAfterDownload(localUri);
    }

    public String getFileNameFromUri(Uri uri) {
        return writer.getFileNameFromUri(uri);
    }

    public Uri createAudioUri(String mFileName) {
        return writer.createAudioUri(mFileName);
    }

    public String uriToString(Uri uri){
        return uri.toString();
    }

    public FileDescriptor getFileDescriptor(Uri uri) throws IOException{
        return writer.getFileDescriptor(uri);
    }

    public Attachedfile createFileByType(String mCurrentFilePath, String mCurrentFileName, Story story_, Case case_, String filetype){
        return writer.createFileByType(mCurrentFilePath, mCurrentFileName, story_, case_, filetype);
    }

    public boolean contentUriExists(Uri uri, Context mContext){
        return writer.contentUriExists(uri, mContext);
    }

    public int deleteByPath(String path){
        return writer.deleteByPath(path);
    }

    public String setType(Context context, Uri uri){
        ContentResolver cr = context.getContentResolver();
        String mime = cr.getType(uri);
        if(mime != null && mime.toLowerCase().contains("image")) {
            return Constant.IMG_FILE_TYPE;
        } else{
            return Constant.OTHER_FILE_TYPE;
        }
    }


    public Bitmap getBitMap(String path){
        return writer.getBitMap(path, 40, 40);
    }

    public Bitmap getBitMap(String path, Context context, int width, int height){
        //width and height in dp
        Resources r = context.getResources();

        float px_wd = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                (float)width,
                r.getDisplayMetrics()
        );
        float px_hg = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                (float)height,
                r.getDisplayMetrics()
        );
        return writer.getBitMap(path, Math.round(px_wd), Math.round(px_hg));
    }

}
