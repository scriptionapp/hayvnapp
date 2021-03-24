package com.hayvn.hayvnapp.Helper;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class FileTypeHelper {

    private static final String TAG = "FILETYPE";

    public static boolean isImageFile(ContentResolver cr, String mimeType, String uri) {
        if (mimeType != null) {
            return mimeType.startsWith("image/");
        } else {
            try (InputStream is = cr.openInputStream(Uri.parse(uri))) {
                if (is != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(is, null, options);
                    return (options.outWidth > 0 && options.outHeight > 0);
                }
            } catch (IOException e) {
                //ignore
            }
        }
        return false;
    }

    public static boolean isAudioFile(String mimeType, String uri, Context context) {
        if (mimeType != null) {
            return mimeType.startsWith("audio/");
        } else {
            MediaPlayer mp = MediaPlayer.create(context, Uri.parse(uri));
            if(mp != null) {
                mp.release();
                return true;
            }
        }
        return false;
    }
}
