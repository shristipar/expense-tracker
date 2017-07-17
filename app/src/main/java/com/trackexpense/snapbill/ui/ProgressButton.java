package com.trackexpense.snapbill.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.trackexpense.snapbill.R;
import com.trackexpense.snapbill.utils.GUIUtils;

/**
 * Created by Joe on 06-05-2017.
 */

public class ProgressButton extends LinearLayout {

    private ProgressBar progressBar;
    private FrameLayout rootView;
    private TextView textView;

    private boolean inProgress = false;

    ViewDataBinding viewDataBinding;

    private final static String TAG = ProgressButton.class.getSimpleName();

    public ProgressButton(Context context) {
        super(context);
        init(context);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        init(attrs);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs == null)
            return;

        TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.ProgressButton);

        //android:text
        if(a.hasValue(R.styleable.ProgressButton_android_text)) {
            CharSequence text = a.getText(R.styleable.ProgressButton_android_text);
            textView.setText(text);
        }

        //android:background
        if(a.hasValue(R.styleable.ProgressButton_android_background)) {
            Drawable background = a.getDrawable(R.styleable.ProgressButton_android_background);
            if (background != null) {
                setButtonBackground(background);
            } else {
                Integer backgroundColor = a.getColor(R.styleable.ProgressButton_android_background,Integer.MAX_VALUE);
                if(backgroundColor!=Integer.MAX_VALUE) {
                    setButtonBackground(backgroundColor);
                }
            }
        }

        //android:textColor
        if(a.hasValue(R.styleable.ProgressButton_android_textColor)) {
            Integer textColor = a.getColor(R.styleable.ProgressButton_android_textColor, Integer.MAX_VALUE);
            if(textColor!=Integer.MAX_VALUE) {
                setTextColor(textColor);
            }
        }

        a.recycle();
    }

    private void setButtonBackground(Drawable drawable) {
        rootView.setBackground(drawable);
    }
    private void setButtonBackground(Integer color) {
        rootView.setBackgroundColor(color);
    }

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.ui_progress_button, this);

        rootView = (FrameLayout) findViewById(R.id.root_view);
        textView = (TextView) findViewById(R.id.text_view);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    public void startProgress() {
        if (progressBar != null) {
            progressBar.setVisibility(VISIBLE);
            inProgress = true;
        }
    }

    public void stopProgress() {
        if (progressBar != null ) {
            progressBar.setVisibility(GONE);
            inProgress = false;
        }
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setTextColor(Integer color) {
        textView.setTextColor(color);
        setProgressColor(color);
    }

    public void setProgressColor(Integer color) {
        progressBar.getIndeterminateDrawable().setColorFilter(
                color,
                android.graphics.PorterDuff.Mode.SRC_IN);
    }

    public void animateTextColor(Integer color, Integer duration) {
        setProgressColor(color);
        GUIUtils.animateColorOfView(textView, "textColor",
                textView.getCurrentTextColor(), color, duration);
    }
}
