package com.trackexpense.snapbill.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;

import com.trackexpense.snapbill.ui.detectors.RotationGestureDetector;

import static android.media.ThumbnailUtils.extractThumbnail;
import static com.trackexpense.snapbill.utils.GUIUtils.getViewSnapshot;

/**
 * Created by Johev on 28-03-2017.
 */

public class ImageViewWithZoom extends android.support.v7.widget.AppCompatImageView
        implements View.OnTouchListener, RotationGestureDetector.OnRotationGestureListener {

    private static final String TAG = "ImageViewWithZoom";
    Matrix matrix = new Matrix();

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    static final int CLICK = 3;
    int mode = NONE;

    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1f;
    float maxScale = 4f;
    float[] m;

    float redundantXSpace, redundantYSpace;
    float width, height;
    float saveScale = 1f;
    float right, bottom, origWidth, origHeight, bmWidth, bmHeight;

    ScaleGestureDetector mScaleDetector;
    Context context;

    private OnImageViewUpdatedListener onImageViewUpdatedListener;

    Bitmap originalBitmap;

    private RotationGestureDetector mRotationDetector;

    public ImageViewWithZoom(Context context, AttributeSet attr) {
        super(context, attr);
        super.setClickable(true);
        this.setDrawingCacheEnabled(true);

        this.context = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        matrix.setTranslate(1f, 1f);
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);

        setOnTouchListener(this);
        mRotationDetector = new RotationGestureDetector(this);
    }

    public void setOnImageViewUpdateListener(OnImageViewUpdatedListener imageViewUpdateListener) {
        this.onImageViewUpdatedListener = imageViewUpdateListener;
    }

    private void callUpdateListener() {
        if (this.onImageViewUpdatedListener != null) {
            try {
                Bitmap resizedBitmap = getViewSnapshot(this, (int) width, (int) height);
                this.onImageViewUpdatedListener.onUpdate(resizedBitmap);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);

        this.originalBitmap = bm;
        bmWidth = bm.getWidth();
        bmHeight = bm.getHeight();

//        this.post(new Runnable() {
//            @Override
//            public void run() {
//                callUpdateListener();
//            }
//        });

        final ImageViewWithZoom view = this;
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                callUpdateListener();
            }
        });
    }

    public void setMaxZoom(float x) {
        maxScale = x;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mRotationDetector.onTouchEvent(event);

        matrix.getValues(m);
        float x = m[Matrix.MTRANS_X];
        float y = m[Matrix.MTRANS_Y];
        PointF curr = new PointF(event.getX(), event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                last.set(event.getX(), event.getY());
                start.set(last);
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                last.set(event.getX(), event.getY());
                start.set(last);
                mode = ZOOM;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM || (mode == DRAG && saveScale > minScale)) {
                    float deltaX = curr.x - last.x;
                    float deltaY = curr.y - last.y;
                    float scaleWidth = Math.round(origWidth * saveScale);
                    float scaleHeight = Math.round(origHeight * saveScale);
                    if (scaleWidth < width) {
                        deltaX = 0;
                        if (y + deltaY > 0)
                            deltaY = -y;
                        else if (y + deltaY < -bottom)
                            deltaY = -(y + bottom);
                    } else if (scaleHeight < height) {
                        deltaY = 0;
                        if (x + deltaX > 0)
                            deltaX = -x;
                        else if (x + deltaX < -right)
                            deltaX = -(x + right);
                    } else {
                        if (x + deltaX > 0)
                            deltaX = -x;
                        else if (x + deltaX < -right)
                            deltaX = -(x + right);

                        if (y + deltaY > 0)
                            deltaY = -y;
                        else if (y + deltaY < -bottom)
                            deltaY = -(y + bottom);
                    }
                    matrix.postTranslate(deltaX, deltaY);

                    last.set(curr.x, curr.y);
                }
                break;

            case MotionEvent.ACTION_UP:
                mode = NONE;
                int xDiff = (int) Math.abs(curr.x - start.x);
                int yDiff = (int) Math.abs(curr.y - start.y);
                if (xDiff < CLICK && yDiff < CLICK) {
                    performClick();
                    callOnTouchListner(curr);
                } else {
                    callUpdateListener();
                }

                break;

            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                rotationAngle += deltaRotationAngle;
                break;
        }
        setImageMatrix(matrix);
        invalidate();
//        return super.onTouchEvent(event);
        return true;
    }

    protected void callOnTouchListner(PointF pointF) {
        if (this.onImageViewUpdatedListener != null) {
            this.onImageViewUpdatedListener.onTouch(
                    new Point((int) pointF.x, (int) pointF.y));
        }
    }

    private float rotationAngle = 0f;
    private float deltaRotationAngle = 0f;

    @Override
    public void OnRotation(RotationGestureDetector rotationDetector) {
       /* float scaleWidth = Math.round(origWidth * saveScale);
        float scaleHeight = Math.round(origHeight * saveScale);

        matrix.getValues(m);
        float x = m[Matrix.MTRANS_X] > 0? m[Matrix.MTRANS_X]: 0;
        float y = m[Matrix.MTRANS_Y]> 0? m[Matrix.MTRANS_Y]: 0;

        deltaRotationAngle = -mRotationDetector.getAngle();
        matrix.postRotate(deltaRotationAngle, x + scaleWidth/2, y + scaleHeight/2);
        setImageMatrix(matrix);
        invalidate();

        Log.e(TAG, x + " " + y);*/
    }

    protected class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = saveScale;
            saveScale *= mScaleFactor;
            if (saveScale > maxScale) {
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            } else if (saveScale < minScale) {
                saveScale = minScale;
                mScaleFactor = minScale / origScale;
            }
            right = width * saveScale - width - (2 * redundantXSpace * saveScale);
            bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
            if (origWidth * saveScale <= width || origHeight * saveScale <= height) {
                matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2);
                if (mScaleFactor < 1) {
                    matrix.getValues(m);
                    float x = m[Matrix.MTRANS_X];
                    float y = m[Matrix.MTRANS_Y];
                    if (mScaleFactor < 1) {
                        if (Math.round(origWidth * saveScale) < width) {
                            if (y < -bottom)
                                matrix.postTranslate(0, -(y + bottom));
                            else if (y > 0)
                                matrix.postTranslate(0, -y);
                        } else {
                            if (x < -right)
                                matrix.postTranslate(-(x + right), 0);
                            else if (x > 0)
                                matrix.postTranslate(-x, 0);
                        }
                    }
                }
            } else {
                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
                matrix.getValues(m);
                float x = m[Matrix.MTRANS_X];
                float y = m[Matrix.MTRANS_Y];
                if (mScaleFactor < 1) {
                    if (x < -right)
                        matrix.postTranslate(-(x + right), 0);
                    else if (x > 0)
                        matrix.postTranslate(-x, 0);
                    if (y < -bottom)
                        matrix.postTranslate(0, -(y + bottom));
                    else if (y > 0)
                        matrix.postTranslate(0, -y);
                }
            }
            return true;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        //Fit to screen.
        float scale;
        float scaleX = width / bmWidth;
        float scaleY = height / bmHeight;
        scale = Math.min(scaleX, scaleY);
        matrix.setScale(scale, scale);
        setImageMatrix(matrix);
        saveScale = 1f;

        // Center the image
        redundantYSpace = height - (scale * bmHeight);
        redundantXSpace = width - (scale * bmWidth);
        redundantYSpace /= 2;
        redundantXSpace /= 2;

        matrix.postTranslate(redundantXSpace, redundantYSpace);

        origWidth = width - 2 * redundantXSpace;
        origHeight = height - 2 * redundantYSpace;
        right = width * saveScale - width - (2 * redundantXSpace * saveScale);
        bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
        setImageMatrix(matrix);
    }

    public interface OnImageViewUpdatedListener {
        void onUpdate(Bitmap updatedBitmap);

        void onTouch(Point imageViewPoint);
    }

}
