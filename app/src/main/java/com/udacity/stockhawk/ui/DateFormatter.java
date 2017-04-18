package com.udacity.stockhawk.ui;

import android.content.Context;
import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateFormatter implements IAxisValueFormatter {

   private Context mContext;
    public DateFormatter(Context context) {
        super();
        mContext = context;
    }

    /**
     * Called when a value from an axis is to be formatted
     * before being drawn. For performance reasons, avoid excessive calculations
     * and memory allocations inside this method.
     *
     * @param value the value to be formatted
     * @param axis  the axis the value belongs to
     * @return
     */


    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long) value);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        String stockDate = mMonth + "-" + mDay + "-" + mYear;
        return stockDate;
    }
}
