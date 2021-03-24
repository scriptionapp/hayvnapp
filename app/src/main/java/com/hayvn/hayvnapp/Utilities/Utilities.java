package com.hayvn.hayvnapp.Utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;

import com.hayvn.hayvnapp.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.hayvn.hayvnapp.Activities.MainAppActivity.TAG;

public class Utilities {
    private static Utilities utilities;

    private Utilities() {
    }

    public static Map<String, Integer> zipToMap(List<String> keys, List<Integer> vals){
        return IntStream.range(0, keys.size()).boxed()
                .collect(Collectors.toMap(keys::get, vals:: get));
    }

    public static synchronized Utilities getInstance() {
        if (utilities == null)
            utilities = new Utilities();
        return utilities;
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public String getString(EditText editText) {
        return editText.getText().toString().trim().trim();
    }

    public boolean checkEmailOk(String txt){
        String expression = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(txt);
        return matcher.matches();
    }

    public boolean isValid(EditText... editText) {
        boolean isValid = true;
        for (EditText temp : editText) {
            temp.setError(null);

            if (getString(temp).isEmpty()) {
                temp.setError("Field is required");
                isValid = false;
            }
        }
        return isValid;
    }

    public void Alert(Context context, String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    public void exportDB(Context context) {
        try {
            File internal = Environment.getDataDirectory();
            File external = Environment.getExternalStorageDirectory();

            if (external.canWrite()) {
                String currentDBPath = "/data/" + context.getPackageName() + "/databases/" + context.getString(R.string.app_name);
                String backupDBPath = "/Backup/" + context.getString(R.string.app_name) + ".db";
                File currentDB = new File(internal, currentDBPath);
                File backupDB = new File(external, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Log.d(TAG, "DB exported successfully");
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to export DB");
        }
    }


}