package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.BinderThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.common.collect.Lists;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;


public class DetailsActivity extends AppCompatActivity {


    @BindView(R.id.price)
    TextView priceView;
    @BindView(R.id.symbol)
    TextView symbolView;
    @BindView(R.id.exchange)
    TextView exchangeView;
    @BindView(R.id.chart)
    LineChart chartView;
    @BindView(R.id.full_name)
    TextView fullNameView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String stockSymbol = getIntent().getStringExtra("symbol");

        String[] projection = {Contract.Quote.COLUMN_SYMBOL,
                Contract.Quote.COLUMN_PRICE,
                Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
                Contract.Quote.COLUMN_PERCENTAGE_CHANGE, Contract.Quote.COLUMN_HISTORY, Contract.Quote.COLUMN_EXCHANGE, Contract.Quote.COLUMN_FULL_NAME};

        Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(stockSymbol), projection, Contract.Quote.COLUMN_SYMBOL + " = " + stockSymbol, null, null);
        String price;
        String history = null;
        String exchange;
        String symbol;
        String fullName;

        if (cursor.moveToFirst()) {
            do {
                symbol = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
                price = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_PRICE));
                history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
                exchange = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_EXCHANGE));
                fullName = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_FULL_NAME));


                symbolView.setText(symbol);
                exchangeView.setText(exchange);
                priceView.setText(price);
                fullNameView.setText(fullName);

                Log.v("!!","Symbol: " + symbol + ",Price: " + price + ", Exchange: " + exchange);
            } while (cursor.moveToNext());
        }
        cursor.close();

        String[] historyList = history.split("\n");
        List<String> list = Arrays.asList(historyList);
        Collections.reverse(list);
        historyList = (String[]) list.toArray();

        List<Entry> stockHistory = new ArrayList<>();
        for (String string : historyList) {
            String[] splitString = string.split(", ");
            stockHistory.add(new Entry(Float.valueOf(splitString[0]), Float.valueOf(splitString[1])));
            Log.v("##", Float.valueOf(splitString[0]) + " --- " + Float.valueOf(splitString[1]));
        }

        LineDataSet dataSet = new LineDataSet(stockHistory, "historyLabel");
        dataSet.setDrawCircles(false);
        dataSet.setColors(new int[] {R.color.colorAccent},getApplicationContext());
        dataSet.setLineWidth(3);
        LineData lineData = new LineData(dataSet);
        chartView.setData(lineData);
        formatChart(chartView);
        chartView.invalidate();
    }

    private void formatChart(LineChart chart) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setValueFormatter(new DateFormatter(getApplicationContext()));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setTextColor(Color.WHITE);

        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setTextColor(Color.WHITE);



        Legend legend = chart.getLegend();
        legend.setEnabled(false);
    }
}

