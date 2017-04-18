package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.DetailsActivity;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.widget.StockWidgetProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RunnableFuture;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

import static android.os.Looper.getMainLooper;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;

    private QuoteSyncJob() {
    }

    static void getQuotes(Context context) {

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -YEARS_OF_HISTORY);
        final Context context1 = context;
        try {
            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            if (stockArray.length == 0) {
                return;
            }


            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();


            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                final String symbol = iterator.next();

                float price;
                String fullName;
                float change;
                float percentChange;
                float volume;
                float avgVolume;
                float marketCap;
                float open;
                float high;
                float low;
                StringBuilder historyBuilder;
                try {
                    Stock stock = quotes.get(symbol);
                    StockQuote quote = stock.getQuote();

                    fullName = stock.getName();
                    price = quote.getPrice().floatValue();
                    change = quote.getChange().floatValue();
                    percentChange = quote.getChangeInPercent().floatValue();
                    volume = quote.getVolume();
                    avgVolume = quote.getAvgVolume();
                    marketCap = stock.getStats().getMarketCap().floatValue();
                    open = quote.getOpen().floatValue();
                    high = quote.getDayHigh().floatValue();
                    low = quote.getDayLow().floatValue();

                    List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);
                    historyBuilder = new StringBuilder();
                    for (HistoricalQuote it : history) {
                        historyBuilder.append(it.getDate().getTimeInMillis());
                        historyBuilder.append(", ");
                        historyBuilder.append(it.getClose());
                        historyBuilder.append("\n");
                    }

                } catch (NullPointerException e) {
                    PrefUtils.removeStock(context, symbol);
                    Handler handler = new Handler(getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context1, context1.getString(R.string.toast_invalid_symbol) + symbol, Toast.LENGTH_LONG).show();
                        }
                    });

                    continue;
                }

                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_FULL_NAME, fullName);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
                quoteCV.put(Contract.Quote.COLUMN_MARKETCAP, marketCap);
                quoteCV.put(Contract.Quote.COLUMN_AVGVOLUME, avgVolume);
                quoteCV.put(Contract.Quote.COLUMN_VOLUME, volume);
                quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());

                quoteCV.put(Contract.Quote.COLUMN_OPEN, open);
                quoteCV.put(Contract.Quote.COLUMN_LOW, low);
                quoteCV.put(Contract.Quote.COLUMN_HIGH, high);

                quoteCVs.add(quoteCV);
                Log.v("!! - QuoteSyncJob", symbol + " - " + price + " - " + percentChange);
            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            updateWidget(context);

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    private static void schedulePeriodic(Context context) {

        //JobInfo is a data container for sending work to the job scheduler. It encapsulates the parameters of the job.
        //Contstruct builder with joh number and component name
        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));

        //set jobinfo parameters
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

        //Get the job scheduler, it does not need to be instantiated by me
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        //Pass the jobinfo to the schedler. The JobInfo contains the info on which job service to use. (QuoteJobService.class in this case)
        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    public static synchronized void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {

            //InentService class handles aysnchrouns job on demand. it handles each intent in turn on demand on worker thread.
            Intent nowIntent = new Intent(context, QuoteIntentService.class);

            context.startService(nowIntent);
        } else {
            //this is same as schedulePeriodic, but with different job ID (ONE_OFF_ID), to do if no internet
            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());
        }
    }

    public static void updateWidget(Context context) {

        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
        context.sendBroadcast(dataUpdatedIntent);

    }


}
