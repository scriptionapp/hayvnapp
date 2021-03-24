package com.hayvn.hayvnapp.Helper;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatEditText;

public class EditTextCursorWatcher extends AppCompatEditText {

    Context mContext;

    public EditTextCursorWatcher(Context context, AttributeSet attrs,
                                 int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public EditTextCursorWatcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public EditTextCursorWatcher(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (selEnd - selStart > 0) {
            //
        }
    }
}