package com.hayvn.hayvnapp.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SharedPrefHelper {

    private static final String PREF_NAME = "com.hayvn.hayvnapp.PREFERENCE_FILE_KEY";

    private SharedPrefHelper() {
    }

    private static final int MODE = Context.MODE_PRIVATE;

    private static SharedPreferences getPreferences(Context context) {
        return (context.getApplicationContext()).getSharedPreferences(PREF_NAME, MODE);
    }

    private static void getAll(Context context){
        Map<String, ?> alle = getPreferences(context).getAll();
        for(Map.Entry<String, ?> entry: alle.entrySet()){
            Log.d("PREF", entry.getKey() + ":" + String.valueOf(entry.getValue()));
        }
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }

    public static void writeInteger(Context context, String key, int value) {
        getEditor(context).putInt(key, value).commit();
    }
    public static int readInteger(Context context, String key) {
        return getPreferences(context.getApplicationContext()).getInt(key, 0);
    }


    public static void writeString(Context context, String key, String value) {
        getEditor(context).putString(key, value).commit();
    }
    public static String readString(Context context, String key) {
        return getPreferences(context).getString(key, null);
    }

    public static void clear(Context context, String key) {
        getEditor(context).remove(key).clear().commit();
    }

    public static void clearAll(Context context) {
        getEditor(context).clear().commit();
    }


    public static void writeStringList(Context context, String key, ArrayList<String> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        writeString(context, key, json);
    }

    public static ArrayList<String> getStringList(Context context, String key) {
        String json = readString(context, key);
        ArrayList<String> arrayList = new ArrayList<String>();
        if(json != null && !json.equals("")){
            Gson gson = new Gson();
            Type type = new TypeToken<List<String>>() {}.getType();
            arrayList = gson.fromJson(json, type);
        }
        return arrayList;
    }
}