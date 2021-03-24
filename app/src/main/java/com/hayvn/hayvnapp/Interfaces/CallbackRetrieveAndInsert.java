package com.hayvn.hayvnapp.Interfaces;

import android.net.Uri;

public interface CallbackRetrieveAndInsert{
    void callbackRetrieveAndInsert(int finalI,
                                   float shareBytesReceived,
                                   String typeUpdate,
                                   Uri localUri);
}
