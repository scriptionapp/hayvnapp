package com.hayvn.hayvnapp.Interfaces;

import android.net.Uri;

import com.hayvn.hayvnapp.Model.Attachedfile;

public interface CallbackUriCreated{
    void callbackUriCreated(Uri uri, Attachedfile af, int token);
}
