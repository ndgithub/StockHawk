package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DetailsActivity extends AppCompatActivity {


    @BindView(R.id.price)
    TextView priceView;
    @BindView(R.id.chart)
    LineChart chartView;
    @BindView(R.id.full_name)
    TextView fullNameView;
    @BindView(R.id.percent_change)
    TextView percentView;
    @BindView(R.id.absolute_change)
    TextView absoluteView;
    @BindView(R.id.more_labels)
    TextView moreLabelsView;
    @BindView(R.id.more_values)
    TextView moreValuesView;
    @BindView(R.id.range_labels)
    TextView rangeLabelsView;
    @BindView(R.id.ranges)
    TextView rangeValuesView;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DecimalFormat dollarFormatWithPlus;
        DecimalFormat dollarFormat;
        DecimalFormat percentageFormat;

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");

        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String stockSymbol = getIntent().getStringExtra("symbol");

        String[] projection = {Contract.Quote.COLUMN_SYMBOL,
                Contract.Quote.COLUMN_PRICE,
                Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
                Contract.Quote.COLUMN_PERCENTAGE_CHANGE, Contract.Quote.COLUMN_HISTORY, Contract.Quote.COLUMN_FULL_NAME, Contract.Quote.COLUMN_MARKETCAP
                , Contract.Quote.COLUMN_AVGVOLUME, Contract.Quote.COLUMN_VOLUME, Contract.Quote.COLUMN_LOW, Contract.Quote.COLUMN_HIGH
                ,Contract.Quote.COLUMN_OPEN};

        Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(stockSymbol), projection, Contract.Quote.COLUMN_SYMBOL + " = " + stockSymbol, null, null);
        float price;
        String history = null;
        String symbol;
        String fullName;
        Float percentChange;
        Float absoluteChange;
        float marketCap;
        float volume;
        float avgVolume;
        float open;
        float high;
        float low;


        if (cursor.moveToFirst()) {
            do {
                symbol = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
                price = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PRICE));
                history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
                fullName = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_FULL_NAME));
                percentChange = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));
                absoluteChange = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));
                open = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_OPEN));
                high = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_HIGH));
                low = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_LOW));
                marketCap = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_MARKETCAP));
                volume = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_VOLUME));
                avgVolume = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_AVGVOLUME));

                String change = dollarFormat.format(absoluteChange);
                String percentage = percentageFormat.format(percentChange / 100);

                String priceFormatted = dollarFormat.format(price);
                priceView.setText(priceFormatted);
                fullNameView.setText(fullName);
                percentView.setText(percentage);
                absoluteView.setText("(" + change + ")");

                rangeLabelsView.setText(getString(R.string.open) + "\n" + getString(R.string.high) + "\n" + getString(R.string.low));
                rangeValuesView.setText(open + "\n" + high + "\n" + low);
                moreLabelsView.setText(getString(R.string.market_cap) + "\n" + getString(R.string.volume) + "\n" + getString(R.string.avg_volume));
                moreValuesView.setText(marketCap + "\n" + volume + "\n" + avgVolume);

                if (percentChange > 0) {
                    percentView.setTextColor(getResources().getColor(R.color.material_green_700));
                    absoluteView.setTextColor(getResources().getColor(R.color.material_green_700));
                } else {
                    percentView.setTextColor(getResources().getColor(R.color.material_red_700));
                    absoluteView.setTextColor(getResources().getColor(R.color.material_red_700));
                }


                getSupportActionBar().setTitle(symbol);

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
        }

        LineDataSet dataSet = new LineDataSet(stockHistory, "historyLabel");
        dataSet.setDrawCircles(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setColors(new int[]{R.color.colorPrimary}, getApplicationContext());
        dataSet.setLineWidth(2);
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

        chart.setDrawGridBackground(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);
    }
}

