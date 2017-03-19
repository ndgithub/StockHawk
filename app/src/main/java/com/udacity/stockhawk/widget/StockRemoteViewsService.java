package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.data.StockProvider;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Nicky on 3/7/17.
 */

//RemoteViewsService is responsible for creating the RemoteViewsFactory (subclass of this class).

public class StockRemoteViewsService extends RemoteViewsService {
    /**
     * To be implemented by the derived service to generate appropriate factories for
     * the data.
     *
     * @param intent
     */
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new StockRemoteViewsFactory(getApplicationContext(),intent);
    }
}


// The remote views factory is just the adaptor for the ListView in the Widget. It's like the cursor adaptor, but
// it returns remoteViews suitable for widgets.
class StockRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private int mWidgetId;
    private Cursor mCursor;
    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;


    public StockRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }
    /**
     * Called when your factory is first constructed. The same factory may be shared across
     * multiple RemoteViewAdapters depending on the intent passed.
     */
    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = mContext.getContentResolver().query(Contract.Quote.URI,
                new String[]{Contract.Quote._ID, Contract.Quote.COLUMN_SYMBOL, Contract.Quote.COLUMN_PRICE,
                        Contract.Quote.COLUMN_ABSOLUTE_CHANGE,Contract.Quote.COLUMN_PERCENTAGE_CHANGE},
                null,
                null,
                null);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);

        mCursor.moveToPosition(position);

        rv.setTextViewText(R.id.symbol,mCursor.getString(Contract.Quote.POSITION_SYMBOL));
        rv.setTextViewText(R.id.price,dollarFormat.format(mCursor.getFloat(Contract.Quote.POSITION_PRICE)));


        float rawAbsoluteChange = mCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
        Log.v("service","rawAbsoluteChange: " + rawAbsoluteChange + ", percentageChange: " + percentageChange);
        if (rawAbsoluteChange > 0) {
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red); }

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);

        Log.v("service","change: " + change + ", percentage: " + percentage + " display mode: " + PrefUtils.getDisplayMode(mContext) + " R.string.pref: " + R.string.pref_display_mode_absolute_key);
       // TODO: Fix this
        if (PrefUtils.getDisplayMode(mContext)
                .equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
            rv.setTextViewText(R.id.change,change);
            Log.v("service","1");
        } else {
            rv.setTextViewText(R.id.change,percentage);
            Log.v("service","2");
        }
        return rv;
    }


    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}
