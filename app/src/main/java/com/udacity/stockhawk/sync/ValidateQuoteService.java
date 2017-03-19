package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Nicky on 3/1/17.
 */

public class ValidateQuoteService extends IntentService {


    public ValidateQuoteService() {
        super(QuoteIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
