package com.hayvn.hayvnapp.Helper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.DatePicker;

public class DatePickerExt extends DatePicker {
    public DatePickerExt(Context context) {
        super(context);
    }

    public DatePickerExt(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DatePickerExt(Context context, AttributeSet attrs, int
            defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            ViewParent p = getParent();
            if (p != null)
                p.requestDisallowInterceptTouchEvent(true);
        }

        return false;
    }
}
