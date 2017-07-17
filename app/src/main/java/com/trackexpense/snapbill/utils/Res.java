package com.trackexpense.snapbill.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.trackexpense.snapbill.R;

import java.util.HashMap;

/**
 * Created by sjjhohe on 19-May-17.
 */

public class Res {
    private static Context context;
    private static HashMap<Integer, Animation> animCache = new HashMap<>();

    public static void setContext(Context ctx) {
        context = ctx;
    }

    public static String getString(int resId) {
        return context.getString(resId);
    }

    public static Animation getAnimFromCache(int id) {
        if (animCache.containsKey(id)) {
            return animCache.get(id);
        }

        Animation animation = getAnim(id);
        animCache.put(id, animation);
        return animation;
    }

    public static Animation getAnim(int id) {
        return AnimationUtils.loadAnimation(context, id);
    }

    public static Integer getColor(int colorId) {
        return ContextCompat.getColor(context, colorId);
    }

    public static Drawable getDrawable(int categoryImageResId) {
        return ContextCompat.getDrawable(context, categoryImageResId);
    }
}
