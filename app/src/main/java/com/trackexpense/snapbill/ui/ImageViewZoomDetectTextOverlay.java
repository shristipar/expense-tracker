package com.trackexpense.snapbill.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;

import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.trackexpense.snapbill.R;

import butterknife.BindColor;
import butterknife.ButterKnife;

/**
 * Created by Johev on 28-03-2017.
 */

public class ImageViewZoomDetectTextOverlay extends ImageViewWithZoom {

    private static final String TAG = "ImageViewZoomText";

    SparseArray<TextBlock> textBlocks;

    @BindColor(R.color.colorAccent)
    int lineColor;

    @BindColor(R.color.word_underline)
    int wordColor;

    private boolean drawCanvas;

    public ImageViewZoomDetectTextOverlay(Context context, AttributeSet attr) {
        super(context, attr);

        ButterKnife.bind(this);
    }

    public void setTextBlocks(SparseArray<TextBlock> textBlocks) {
        this.textBlocks = textBlocks;
        drawCanvas = true;
        invalidate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                drawCanvas = false;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                drawCanvas = true;
                break;
        }

        invalidate();
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void setOnImageViewUpdateListener(final OnImageViewUpdatedListener imageViewUpdateListener) {

        super.setOnImageViewUpdateListener(new OnImageViewUpdatedListener() {
            @Override
            public void onUpdate(Bitmap updatedBitmap) {
                imageViewUpdateListener.onUpdate(updatedBitmap);
                drawCanvas = false;
                invalidate();
            }

            @Override
            public void onTouch(Point imageViewPoint) {
                imageViewUpdateListener.onTouch(imageViewPoint);
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (textBlocks == null || !drawCanvas)
            return;

        Paint linePaint = new Paint();
        linePaint.setColor(lineColor);
        linePaint.setStrokeWidth(5);

        Paint wordPaint = new Paint();
        wordPaint.setColor(wordColor);

        for (int i = 0; i < textBlocks.size(); ++i) {
            TextBlock tb = textBlocks.valueAt(i);
            if (tb == null)
                continue;

            for (Text t : tb.getComponents()) {
//                for (Text w : t.getComponents()) {
//                    Rect wr = w.getBoundingBox();
//                    canvas.drawRect(wr.left, wr.top, wr.right, wr.bottom, wordPaint);
//                }

                Rect r = t.getBoundingBox();
                canvas.drawLine(r.left, r.bottom, r.right, r.bottom, linePaint);
            }
        }
    }
}
