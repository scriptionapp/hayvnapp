package com.hayvn.hayvnapp;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

/* This file was created to satisfy the needs of androidthreeten,
    and determined how to implement from the official documentation.
   android docs: https://developer.android.com/reference/android/app/Application
   threenten docs: https://github.com/JakeWharton/ThreeTenABP
 */
public class HayvnApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
