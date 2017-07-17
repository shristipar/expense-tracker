package com.trackexpense.snapbill.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by sjjhohe on 17-Jun-17.
 */

public class InternetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that which service class will handle the intent.
        if(isConnected(context)) {
            Intent serviceIntent = new Intent(context, PendingUploadService.class);
            serviceIntent.putExtra("isNetworkConnected",isConnected(context));
            context.startService(serviceIntent);
        }
    }



    public  boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }
}
