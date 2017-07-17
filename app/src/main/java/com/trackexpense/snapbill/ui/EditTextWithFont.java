package com.trackexpense.snapbill.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.trackexpense.snapbill.R;
import com.trackexpense.snapbill.cache.FontCache;

/**
 * TODO: document your custom view class.
 */
public class EditTextWithFont extends android.support.v7.widget.AppCompatEditText {

    public final static int TYPE_BOLD = 1;
    public final static int TYPE_ITALIC = 2;
    private final static int defaultDimension = 0;

    private String fontName;
    private int fontType;
    private Typeface font;

    public EditTextWithFont(Context context) {
        super(context);
        init(null, 0);
    }

    public EditTextWithFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public EditTextWithFont(Context context, AttributeSet attrs, int defstyle) {
        super(context, attrs, defstyle);
        init(attrs, defstyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ViewWithFont, defStyle, 0);

        if (a.hasValue(R.styleable.ViewWithFont_font_name)) {
            String fn = a.getString(R.styleable.ViewWithFont_font_name);
            setFont(fn);
        }

        int ft = a.getInt(R.styleable.ViewWithFont_font_type, defaultDimension);
        setFontType(ft);

        a.recycle();
    }

    public void setFont(String fontName) {
        this.fontName = fontName;
        this.font = FontCache.get(this.fontName, getContext());
        if (this.font == null)
            return;

        setTypeface(this.font);
    }

    private void setFontType(int fontType) {
        if (this.font == null)
            return;

        this.fontType = fontType;

        if (fontType == TYPE_BOLD) {
            setTypeface(this.font, Typeface.BOLD);
        } else if (fontType == TYPE_ITALIC) {
            setTypeface(this.font, Typeface.ITALIC);
        }
    }
}
