package com.trackexpense.snapbill.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.DatePicker;

import java.util.Calendar;


/**
 * Created by Johev on 30-04-2017.
 */

public class DatePickerHack extends DatePicker implements DatePicker.OnDateChangedListener, Runnable{

    private OnDateSelectedListener onDateSelectedListener;

    public DatePickerHack(Context context) {
        super(context);
        initDatePickerHack();
    }

    public DatePickerHack(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDatePickerHack();
    }

    public DatePickerHack(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDatePickerHack();
    }

    private void initDatePickerHack() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        this.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                this.post(this);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
        if(this.onDateSelectedListener !=null) {
            this.onDateSelectedListener.onDateSelected(datePicker,i,i1,i2);
        }
    }

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.onDateSelectedListener = listener;
    }

    @Override
    public void run() {
        onDateChanged(this, this.getYear(), this.getMonth(), this.getDayOfMonth());
    }

    public interface OnDateSelectedListener {
        void onDateSelected(DatePicker datePicker,int year, int month, int dayOfMonth );
    }
}
