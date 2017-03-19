package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Created by Nicky on 3/7/17.
 */

public class StockWidgetProvider extends AppWidgetProvider {


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {


        for (int i = 0; i < appWidgetIds.length; ++i) {

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
            Intent intent = new Intent(context, StockRemoteViewsService.class);
            rv.setRemoteAdapter(appWidgetIds[i],R.id.list_view,intent);

            Intent mainActivityIntent = new Intent(context, MainActivity.class);
            PendingIntent mainActivityPendingIntent = PendingIntent.getActivity
                    (context, 1, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.widget_title, mainActivityPendingIntent);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            rv.setOnClickPendingIntent(R.id.widget_title, pendingIntent);


//
//
//
//
//            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
//            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
//
//
//
//
//
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                setRemoteAdapter(context, views);
//            } else {
//                setRemoteAdapterV11(context, views);
//            }
//
//
//            rv.setRemoteAdapter(appWidgetIds[i], R.id.list_view, intent);
//            rv.setEmptyView(R.id.list_view, R.id.empty_view);
//
//            Intent appIntent = new Intent(context, MainActivity.class);
//            PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, 0);
//            rv.setOnClickPendingIntent(R.id.list_view, appPendingIntent);


            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
//            appWidgetManager.notifyAppWidgetViewDataChanged(i,
//                    R.id.list_view);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}

