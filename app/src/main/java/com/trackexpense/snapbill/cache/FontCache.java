package com.trackexpense.snapbill.cache;

import android.content.Context;
import android.graphics.Typeface;

import com.trackexpense.snapbill.utils.Constants;

import java.util.Hashtable;

/**
 * Created by Johev on 15-03-2017.
 */

public class FontCache {

    private static Hashtable<String, Typeface> fontCache = new Hashtable<String, Typeface>();

    public static Typeface get(String name, Context context) {
        Typeface tf = fontCache.get(name);
        if (tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), Constants.FONT_PATH + name + ".ttf");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            fontCache.put(name, tf);
        }
        return tf;
    }
}
