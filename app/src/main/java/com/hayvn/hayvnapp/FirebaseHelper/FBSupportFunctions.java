package com.hayvn.hayvnapp.FirebaseHelper;

import android.util.Log;

import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.FirebaseConstants;
import com.hayvn.hayvnapp.Model.Attachedfile;

public class FBSupportFunctions {

    public static String getCompressedFirebaseStoragePath(Attachedfile af, boolean use_compressed_data){
        String storage_path = af.getFireStorageFilePath();
        if(use_compressed_data && af.getType().equals(Constant.IMG_FILE_TYPE)) storage_path = storage_path.replace("/", FirebaseConstants.COMPRESSED_IMAGE_REPLACE);
        Log.d("COMPRESS", storage_path);
        return storage_path;
    }
}
