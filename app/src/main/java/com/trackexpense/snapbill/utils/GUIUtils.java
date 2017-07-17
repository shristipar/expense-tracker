package com.trackexpense.snapbill.utils;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.trackexpense.snapbill.R;
import com.trackexpense.snapbill.cache.FontCache;

import java.lang.reflect.Method;

/**
 * Created by Johev on 07-04-2017.
 */

public class GUIUtils {

    public static Bitmap getViewSnapshot(View v, int width, int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
//        v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
        v.draw(c);
        return b;
    }

    public static Point translateScreenPointRelativeToView(View v, Point screenPoint) {
        Rect rect = new Rect();
        v.getGlobalVisibleRect(rect);
        return new Point(screenPoint.x - rect.left, screenPoint.y - rect.top);
    }

    public static boolean isXYWithinView(float x, float y, View view) {
        if (x >= view.getTop() && x <= view.getBottom() && y >= view.getLeft() && y <= view.getRight()) {
            return true;
        }

        return false;
    }

    /*public static void startCircularRevealAnimation(final CardView cardView, View view, final OnCircularRevealAnimationListener listener) {
        cardView.setVisibility(View.VISIBLE);

        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);

        // getAllOrderBy the center for the clipping circle
        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;

        // getAllOrderBy the final radius for the clipping circle
        int dx = Math.max(cx, view.getWidth() - cx);
        int dy = Math.max(cy, view.getHeight() - cy);
        float finalRadius = (float) Math.hypot(dx, dy);
        float startRadius = view.getWidth() / 2;

        SupportAnimator animator =
                ViewAnimationUtils.createCircularReveal(cardView, cx, cy, startRadius, finalRadius);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(500);
        animator.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {
                cardView.animate()
                        .alpha(0)
                        .setDuration(300)
                        .setInterpolator(new DecelerateInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                cardView.setVisibility(View.INVISIBLE);
                                cardView.setAlpha(1);

                                if (listener != null) {
                                    listener.onAnimationEnd();
                                }
                            }
                        });
            }

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationRepeat() {

            }
        });
        animator.start();
    }*/

    public interface OnCircularRevealAnimationListener {
        public void onAnimationEnd();
    }

    public static final void setAppFont(View mView, String mFontName, boolean reflect) {
        Typeface mFont = FontCache.get(mFontName, mView.getContext());

        if (mView == null || mFont == null) return;

        if (mView instanceof TextView) {
            // Set the font if it is a TextView.
            ((TextView) mView).setTypeface(mFont);
        } else if (mView instanceof ViewGroup) {
            // Recursively attempt another ViewGroup.
            setAppFont((ViewGroup) mView, mFont, reflect);
        } else if (reflect) {
            try {
                Method mSetTypeface = mView.getClass().getMethod("setTypeface", Typeface.class);
                mSetTypeface.invoke(mView, mFont);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static final void setAppFont(ViewGroup mContainer, Typeface mFont, boolean reflect) {
        if (mContainer == null || mFont == null) return;

        final int mCount = mContainer.getChildCount();

        // Loop through all of the children.
        for (int i = 0; i < mCount; ++i) {
            final View mChild = mContainer.getChildAt(i);
            if (mChild instanceof TextView) {
                // Set the font if it is a TextView.
                ((TextView) mChild).setTypeface(mFont);
            } else if (mChild instanceof ViewGroup) {
                // Recursively attempt another ViewGroup.
                setAppFont((ViewGroup) mChild, mFont, reflect);
            } else if (reflect) {
                try {
                    Method mSetTypeface = mChild.getClass().getMethod("setTypeface", Typeface.class);
                    mSetTypeface.invoke(mChild, mFont);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void animateColorOfView(View view, String property, Integer fromColor, Integer toColor, long duration) {
        ObjectAnimator colorAnim = ObjectAnimator.ofInt(view, property, fromColor, toColor);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setDuration(duration);
        colorAnim.start();
    }

    public static void showInputError(EditText editText, String message) {
        editText.setError(message);
    }

    public static void showSnackBar(View view, String message) {
        Snackbar mSnackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        // getAllOrderBy snackbar view
        View mView = mSnackbar.getView();
        // getAllOrderBy textview inside snackbar view
        TextView mTextView = (TextView) mView.findViewById(android.support.design.R.id.snackbar_text);
        // set text to center
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        else
            mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        // show the snackbar
        mSnackbar.show();
    }

    public static String getText(EditText editText) {
        return editText.getText().toString().trim();
    }

    public static <T> T getView(View view, int viewId) {
        return (T) view.findViewById(viewId);
    }

    public static <T> T getView(Dialog dialog, int viewId) {
        return (T) dialog.findViewById(viewId);
    }

    public static void showKeyboard(Activity activity, EditText view) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static void hideKeyboard(Activity activity) {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        hideKeyboard(activity, view);
    }

    public static void hideKeyboard(Activity activity, View view) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void roundedCornerDialog(Dialog dialog) {
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // clipping for rounded corner background
        View rootView = GUIUtils.getView(dialog, R.id.root_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rootView.setClipToOutline(true);
        } else {
            rootView.setBackground(null);
            rootView.setBackgroundColor(Res.getColor(R.color.bg_urobilin));
        }
    }
}
