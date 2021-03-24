package com.hayvn.hayvnapp.Utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.EditText;

import com.hayvn.hayvnapp.R;


public class PasswordView extends EditText {

    private final static float ALPHA_ENABLED_DARK = .54f;
    private final static float ALPHA_DISABLED_DARK = .38f;
    private final static float ALPHA_ENABLED_LIGHT = 1f;
    private final static float ALPHA_DISABLED_LIGHT = .5f;
    private Drawable eye;
    private Drawable eyeWithStrike;
    private int alphaEnabled;
    private int alphaDisabled;
    private boolean visible = false;
    private boolean useStrikeThrough = false;
    private boolean drawableClick;

    public PasswordView(Context context) {
        super(context);
        init(null);
    }

    public PasswordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PasswordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.PasswordView,
                    0, 0);
            try {
                useStrikeThrough = a.getBoolean(R.styleable.PasswordView_useStrikeThrough, false);
            } finally {
                a.recycle();
            }
        }

        int enabledColor = resolveAttr();
        boolean isIconDark = isDark(enabledColor);
        alphaEnabled = (int) (255 * (isIconDark ? ALPHA_ENABLED_DARK : ALPHA_ENABLED_LIGHT));
        alphaDisabled = (int) (255 * (isIconDark ? ALPHA_DISABLED_DARK : ALPHA_DISABLED_LIGHT));

        eye = getToggleDrawable(getContext(), R.drawable.ic_eye, enabledColor);
        eyeWithStrike = getToggleDrawable(getContext(), R.drawable.ic_eye_strike, enabledColor);
        eyeWithStrike.setAlpha(alphaEnabled);
        setup();
    }

    private @ColorInt
    int resolveAttr() {
        TypedValue ta = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, ta, true);
        return ContextCompat.getColor(getContext(), ta.resourceId);
    }

    private Drawable getToggleDrawable(Context context, @DrawableRes int drawableResId, @ColorInt int tint) {
        // Make sure to mutate so that if there are multiple password fields, they can have
        // different visibilities.
        Drawable drawable = getVectorDrawableWithIntrinsicBounds(context, drawableResId).mutate();
        DrawableCompat.setTint(drawable, tint);
        return drawable;
    }

    private Drawable getVectorDrawableWithIntrinsicBounds(Context context, @DrawableRes int drawableResId) {
        Drawable drawable = getVectorDrawable(context, drawableResId);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        return drawable;
    }

    private Drawable getVectorDrawable(Context context, @DrawableRes int drawableResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getDrawable(drawableResId);
        } else {
            return VectorDrawableCompat.create(context.getResources(), drawableResId, context.getTheme());
        }
    }

    protected void setup() {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        setInputType(InputType.TYPE_CLASS_TEXT | (visible ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_TEXT_VARIATION_PASSWORD));
        setSelection(start, end);
        Drawable drawable = useStrikeThrough && !visible ? eyeWithStrike : eye;
        Drawable[] drawables = getCompoundDrawables();
        setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawable, drawables[3]);
        eye.setAlpha(visible && !useStrikeThrough ? alphaEnabled : alphaDisabled);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int drawableRightX = getWidth() - getPaddingRight();
        int drawableLeftX = drawableRightX - getCompoundDrawables()[2].getBounds().width();
        boolean eyeClicked = event.getX() >= drawableLeftX && event.getX() <= drawableRightX;

        if (event.getAction() == MotionEvent.ACTION_DOWN && eyeClicked) {
            drawableClick = true;
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (eyeClicked && drawableClick) {
                drawableClick = false;
                visible = !visible;
                setup();
                invalidate();
                return true;
            } else {
                drawableClick = false;
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void setInputType(int type) {
        Typeface typeface = getTypeface();
        super.setInputType(type);
        setTypeface(typeface);
    }

    public void setUseStrikeThrough(boolean useStrikeThrough) {
        this.useStrikeThrough = useStrikeThrough;
    }

    private static boolean isDark(float[] hsl) {
        return hsl[2] < 0.5f;
    }

    public static boolean isDark(@ColorInt int color) {
        float[] hsl = new float[3];
        androidx.core.graphics.ColorUtils.colorToHSL(color, hsl);
        return isDark(hsl);
    }
}