package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.common.collect.Lists;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class DetailsActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String stockSymbol = getIntent().getStringExtra("symbol");

        String[] projection = {Contract.Quote.COLUMN_SYMBOL,
                Contract.Quote.COLUMN_PRICE,
                Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
                Contract.Quote.COLUMN_PERCENTAGE_CHANGE, Contract.Quote.COLUMN_HISTORY};

        Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(stockSymbol),projection,Contract.Quote.COLUMN_SYMBOL + " = " + stockSymbol,null,null);
        String price;
        String history = "";
        TextView priceView =  (TextView) findViewById(R.id.price);

        if (cursor.moveToFirst()){
            do {
                price = cursor.getString(0);
                history = cursor.getString(4);
                priceView.setText(price + "\n" + cursor.getString(1));
            } while(cursor.moveToNext());
        }
        cursor.close();

        Log.v("sdf","" + history);

        LineChart chart = (LineChart) findViewById(R.id.chart);

        List<Entry> stockHistory = new ArrayList<>();

        String[] historyList = history.split("\n");
        List<String> list = Arrays.asList(historyList);
        Collections.reverse(list);
        historyList = (String[]) list.toArray();


        for (String string: historyList) {
            String[] splitString = string.split(", ");
            stockHistory.add(new Entry(Float.valueOf(splitString[0]),Float.valueOf(splitString[1])));
            Log.v("##",Float.valueOf(splitString[0]) + " --- " + Float.valueOf(splitString[1]));
        }

        LineDataSet dataSet = new LineDataSet(stockHistory,"historyLabel");
        LineData lineData = new LineData(dataSet);
        lineData.setDrawValues(false);
        chart.setData(lineData);
        formatChart(chart);
        chart.invalidate();
    }

    private void formatChart(LineChart chart) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setValueFormatter(new DateFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);
    }


}

